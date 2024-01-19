package my.dictionary.free.view.user.dictionary.words

import android.annotation.SuppressLint
import android.app.SearchManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.AlphabetSort
import my.dictionary.free.domain.models.navigation.AddDictionaryWordScreen
import my.dictionary.free.domain.models.navigation.EditDictionaryWordScreen
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.words.DictionaryWordsViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.SwipeDictionaryItem
import my.dictionary.free.view.widget.ListItemDecoration
import my.dictionary.free.view.widget.OnItemSwipedListener
import my.dictionary.free.view.widget.OnListItemClickListener
import my.dictionary.free.view.widget.OnListTouchListener

@AndroidEntryPoint
class DictionaryWordsFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = DictionaryWordsFragment::class.simpleName
        const val BUNDLE_DICTIONARY_ID =
            "my.dictionary.free.view.user.dictionary.words.DictionaryWordsFragment.BUNDLE_DICTIONARY_ID"
        const val UNDO_SNACKBAR_DURATION: Int = 5000
        const val UNDO_SNACKBAR_DURATION_TIMER = 6000L
        const val UNDO_SNACKBAR_INTERVAL_TIMER = 1000L
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: DictionaryWordsViewModel by viewModels()

    private lateinit var wordsRecyclerView: RecyclerView
    private var wordsAdapter: DictionaryWordsAdapter? = null
    private var undoRemoveWordSnackbar: Snackbar? = null
    private var menuInflater: MenuInflater? = null
    private var menuEdit: MenuItem? = null
    private var actionMode: ActionMode? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var dictionaryId: String? = null

    private val undoRemoveWordTimer =
        object : CountDownTimer(
            UNDO_SNACKBAR_DURATION_TIMER,
            UNDO_SNACKBAR_INTERVAL_TIMER
        ) {
            @SuppressLint("RestrictedApi", "SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                undoRemoveWordSnackbar?.let {
                    ((it.view as ViewGroup).getChildAt(0) as SnackbarContentLayout?)?.messageView?.text =
                        "$seconds ${getString(R.string.seconds)}"
                }
            }

            override fun onFinish() {
                undoRemoveWordSnackbar = null
                wordsAdapter?.getRemoveWordByTimer()?.let { word ->
                    viewModel.deleteWords(
                        context,
                        listOf(word)
                    )
                }
                wordsAdapter?.finallyRemoveItem()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dictionary_words, null)
        view.findViewById<RadioGroup>(R.id.sorting_radio_group).setOnCheckedChangeListener(onSortingGroupListener)
        wordsRecyclerView = view.findViewById(R.id.recycler_view)
        wordsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemTouchHelper =
            ItemTouchHelper(SwipeDictionaryItem(requireContext(), onItemSwipedListener))
        itemTouchHelper.attachToRecyclerView(wordsRecyclerView)
        wordsRecyclerView.addItemDecoration(ListItemDecoration(context = requireContext()))
        wordsRecyclerView.addOnItemTouchListener(
            OnListTouchListener(
                requireContext(),
                wordsRecyclerView,
                onWordsClickListener
            )
        )
        swipeRefreshLayout =
            view.findViewById<SwipeRefreshLayout?>(R.id.swipe_refresh_layout)?.also {
                it.setOnRefreshListener {
                    Log.d(TAG, "onRefresh")
                    refreshWords()
                }
                it.setColorSchemeResources(
                    R.color.main,
                    R.color.main_light,
                    R.color.main_dark
                )
            }
        wordsAdapter = DictionaryWordsAdapter(mutableListOf(), mutableListOf())
        wordsRecyclerView.adapter = wordsAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.clearActionModeUIState.drop(1).collect { clear ->
                        if (clear) {
                            actionMode?.finish()
                        }
                    }
                }
                launch {
                    viewModel.displayErrorUIState.drop(1).collect { errorMessage ->
                        displayError(errorMessage, wordsRecyclerView)
                    }
                }
                launch {
                    viewModel.loadingUIState.collect { visible ->
                        swipeRefreshLayout?.isRefreshing = visible
                        sharedViewModel.loading(visible)
                    }
                }
                launch {
                    viewModel.shouldClearWordsUIState.collect { clear ->
                        if (clear) {
                            Log.d(TAG, "clear words")
                            wordsAdapter?.clearData()
                        }
                    }
                }
                launch {
                    viewModel.wordsUIState.drop(1).collect { word ->
                        Log.d(TAG, "word updated: $word")
                        wordsAdapter?.add(word)
                    }
                }
                launch {
                    viewModel.titleUIState.collect { title ->
                        Log.d(TAG, "set title: $title")
                        sharedViewModel.setTitle(title)
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_dictionary_words, { menu, mi ->
            run {
                this.menuInflater = mi
                val searchManager =
                    ContextCompat.getSystemService(
                        requireContext(),
                        SearchManager::class.java
                    ) as SearchManager
                (menu.findItem(R.id.search).actionView as SearchView).let { searchView ->
                    searchView.setSearchableInfo(
                        searchManager.getSearchableInfo(
                            activity?.componentName
                        )
                    )
                    searchView.setOnQueryTextListener(onWordsQueryListener)
                }
            }
        }, {
            when (it) {
                R.id.nav_add_word -> {
                    sharedViewModel.navigateTo(AddDictionaryWordScreen(dictionaryId ?: ""))
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        dictionaryId = arguments?.getString(BUNDLE_DICTIONARY_ID, null)
        refreshWords()
    }

    private fun refreshWords() {
        wordsAdapter?.clearData()
        viewModel.loadWords(context, dictionaryId)
    }

    private val onWordsQueryListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            wordsAdapter?.filter?.filter(newText)
            return false
        }

    }

    private val onItemSwipedListener = object : OnItemSwipedListener {
        override fun onSwiped(position: Int) {
            Log.d(TAG, "swipe item by position $position")
            wordsAdapter?.temporaryRemoveItem(position)
            undoRemoveWordSnackbar = Snackbar.make(
                wordsRecyclerView,
                R.string.seconds,
                Snackbar.LENGTH_INDEFINITE
            )
                .setDuration(UNDO_SNACKBAR_DURATION)
                .setAction(R.string.undo) {
                    wordsAdapter?.undoRemovedItem()
                }
            undoRemoveWordSnackbar?.show()
            undoRemoveWordTimer.start()
        }
    }

    private val actionModeCallBack = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater?.inflate(R.menu.menu_edit_list_item, menu)
            menuEdit = menu?.findItem(R.id.menu_edit)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.menu_edit -> {
                    wordsAdapter?.getSelectedWords()?.firstOrNull()?.let {
                        actionMode?.finish()
                        sharedViewModel.navigateTo(EditDictionaryWordScreen(it))
                    }
                    true
                }

                R.id.menu_delete -> {
                    viewModel.deleteWords(
                        context,
                        wordsAdapter?.getSelectedWords()
                    )
                    true
                }

                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            wordsAdapter?.clearSelectedWords()
            actionMode = null
        }
    }

    private val onWordsClickListener = object : OnListItemClickListener {
        override fun onListItemClick(childView: View) {
            wordsAdapter?.getItemByPosition(
                wordsRecyclerView.getChildAdapterPosition(childView)
            )?.let { word ->
                if (actionMode == null) {
                    sharedViewModel.navigateTo(EditDictionaryWordScreen(word))
                } else {
                    selectWord(word)
                }
            }
        }

        override fun onListItemLongClick(childView: View) {
            wordsAdapter?.getItemByPosition(
                wordsRecyclerView.getChildAdapterPosition(childView)
            )?.let { word ->
                if (activity != null && activity is AppCompatActivity) {
                    if (actionMode == null) {
                        actionMode =
                            (activity as AppCompatActivity).startSupportActionMode(
                                actionModeCallBack
                            )
                    }
                }
                selectWord(word)
            }
        }
    }

    private fun selectWord(word: Word) {
        wordsAdapter?.selectWord(word)
        val selectedDictionaryCount = wordsAdapter?.getSelectedWordsCount() ?: 0
        actionMode?.title =
            "$selectedDictionaryCount ${getString(R.string.selected).uppercase()}"
        menuEdit?.isVisible = selectedDictionaryCount <= 1
        if (selectedDictionaryCount < 1) {
            actionMode?.finish()
            actionMode = null
            menuEdit = null
            wordsAdapter?.clearSelectedWords()
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop()")
        undoRemoveWordSnackbar?.dismiss()
        super.onStop()
    }

    private val onSortingGroupListener = RadioGroup.OnCheckedChangeListener { p0, checkedId ->
        when(checkedId) {
            R.id.sorting_a_z_button -> {
                wordsAdapter?.sortByAlphabet(AlphabetSort.A_Z)
            }
            R.id.sorting_z_a_button -> {
                wordsAdapter?.sortByAlphabet(AlphabetSort.Z_A)
            }
        }
    }
}
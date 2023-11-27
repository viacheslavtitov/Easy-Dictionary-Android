package my.dictionary.free.view.user.dictionary

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.navigation.AddUserDictionaryScreen
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.add.AddUserDictionaryFragment
import my.dictionary.free.view.widget.ListItemDecoration
import my.dictionary.free.view.widget.OnItemSwipedListener


@AndroidEntryPoint
class UserDictionaryFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = UserDictionaryFragment::class.simpleName
        const val UNDO_SNACKBAR_DURATION: Int = 5000
        const val UNDO_SNACKBAR_DURATION_TIMER = 6000L
        const val UNDO_SNACKBAR_INTERVAL_TIMER = 1000L
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: UserDictionaryViewModel by viewModels()

    private lateinit var dictionariesRecyclerView: RecyclerView
    private var dictionariesAdapter: UserDictionaryAdapter? = null
    private var undoRemoveDictionarySnackbar: Snackbar? = null
    private var menuInflater: MenuInflater? = null
    private var menuEdit: MenuItem? = null
    private var actionMode: ActionMode? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private val undoRemoveDictionaryTimer =
        object : CountDownTimer(UNDO_SNACKBAR_DURATION_TIMER, UNDO_SNACKBAR_INTERVAL_TIMER) {
            @SuppressLint("RestrictedApi", "SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                undoRemoveDictionarySnackbar?.let {
                    ((it.view as ViewGroup).getChildAt(0) as SnackbarContentLayout?)?.messageView?.text =
                        "$seconds ${getString(R.string.seconds)}"
                }
            }

            override fun onFinish() {
                undoRemoveDictionarySnackbar = null
                dictionariesAdapter?.finallyRemoveItem()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_dictionary, null)
        dictionariesRecyclerView = view.findViewById(R.id.recycler_view)
        dictionariesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemTouchHelper =
            ItemTouchHelper(SwipeDictionaryItem(requireContext(), onItemSwipedListener))
        itemTouchHelper.attachToRecyclerView(dictionariesRecyclerView)
        dictionariesRecyclerView.addItemDecoration(ListItemDecoration(context = requireContext()))
        dictionariesRecyclerView.addOnItemTouchListener(
            OnDictionaryTouchListener(
                requireContext(),
                dictionariesRecyclerView,
                onDictionaryClickListener
            )
        )
        swipeRefreshLayout =
            view.findViewById<SwipeRefreshLayout?>(R.id.swipe_refresh_layout)?.also {
                it.setOnRefreshListener {
                    Log.d(TAG, "onRefresh")
                    refreshDictionaries()
                }
                it.setColorSchemeResources(
                    R.color.main,
                    R.color.main_light,
                    R.color.main_dark
                )
            }
        dictionariesAdapter = UserDictionaryAdapter(mutableListOf())
        dictionariesRecyclerView.adapter = dictionariesAdapter
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
                        displayError(errorMessage, dictionariesRecyclerView)
                    }
                }
                launch {
                    viewModel.loadingUIState.collect { visible ->
                        swipeRefreshLayout?.isRefreshing = visible
                    }
                }
                launch {
                    viewModel.dictionariesUIState.drop(1).collectLatest { dict ->
                        Log.d(TAG, "dictionary updated: $dict")
                        dictionariesAdapter?.add(dict)
                    }
                }
            }
        }

        addMenuProvider(R.menu.menu_user_dictionary, { menu, mi ->
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
                    searchView.setOnQueryTextListener(onDictionariesQueryListener)
                }
            }
        }, {
            when (it) {
                R.id.nav_add_dictionary -> {
                    sharedViewModel.navigateTo(AddUserDictionaryScreen())
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        refreshDictionaries()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AddUserDictionaryFragment.BUNDLE_DICTIONARY_CREATED_RESULT) { requestKey, bundle ->
            val needUpdate =
                bundle.getBoolean(AddUserDictionaryFragment.BUNDLE_DICTIONARY_CREATED_KEY, false)
            if (needUpdate) {
                refreshDictionaries()
            }
        }
    }

    private fun refreshDictionaries() {
        dictionariesAdapter?.clearData()
        viewModel.loadDictionaries(context)
    }

    private val onDictionariesQueryListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            return true
        }

    }

    private val onDictionaryClickListener = object : OnDictionaryClickListener {
        override fun onDictionaryClick(dictionary: Dictionary) {
            if (actionMode == null) {
                //navigate to dictionary detail screen
            } else {
                selectDictionary(dictionary)
            }
        }

        override fun onDictionaryLongClick(dictionary: Dictionary) {
            if (activity != null && activity is AppCompatActivity) {
                if (actionMode == null) {
                    actionMode =
                        (activity as AppCompatActivity).startSupportActionMode(actionModeCallBack)
                }
            }
            selectDictionary(dictionary)
        }
    }

    private fun selectDictionary(dictionary: Dictionary) {
        dictionariesAdapter?.selectDictionary(dictionary)
        val selectedDictionaryCount = dictionariesAdapter?.getSelectedDictionariesCount() ?: 0
        actionMode?.title =
            "$selectedDictionaryCount ${getString(R.string.selected).uppercase()}"
        menuEdit?.isVisible = selectedDictionaryCount <= 1
        if (selectedDictionaryCount < 1) {
            actionMode?.finish()
            actionMode = null
            menuEdit = null
            dictionariesAdapter?.clearSelectedDevices()
        }
    }

    private val onItemSwipedListener = object : OnItemSwipedListener {
        override fun onSwiped(position: Int) {
            dictionariesAdapter?.temporaryRemoveItem(position)
            undoRemoveDictionarySnackbar = Snackbar.make(
                dictionariesRecyclerView,
                R.string.seconds,
                Snackbar.LENGTH_INDEFINITE
            )
                .setDuration(UNDO_SNACKBAR_DURATION)
                .setAction(R.string.undo) {
                    dictionariesAdapter?.undoRemovedItem()
                }
            undoRemoveDictionarySnackbar?.show()
            undoRemoveDictionaryTimer.start()
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
                    true
                }

                R.id.menu_delete -> {
                    viewModel.deleteDictionaries(
                        context,
                        dictionariesAdapter?.getSelectedDictionaries()
                    )
                    true
                }

                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            dictionariesAdapter?.clearSelectedDevices()
            actionMode = null
        }
    }

    private open inner class OnDictionaryTouchListener(
        context: Context,
        private val recyclerView: RecyclerView,
        private val onDictionaryClickListener: OnDictionaryClickListener
    ) : RecyclerView.OnItemTouchListener {

        private val gestureDetector: GestureDetector =
            GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recyclerView.findChildViewUnder(e.x, e.y)
                    child?.let {
                        dictionariesAdapter?.getItemByPosition(
                            recyclerView.getChildAdapterPosition(child)
                        )?.let { dictionary ->
                            onDictionaryClickListener.onDictionaryLongClick(dictionary)
                        }
                    }
                }
            })

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val child = rv.findChildViewUnder(e.x, e.y)
            child?.let {
                if (gestureDetector.onTouchEvent(e)) {
                    dictionariesAdapter?.getItemByPosition(
                        recyclerView.getChildAdapterPosition(child)
                    )?.let { dictionary ->
                        onDictionaryClickListener.onDictionaryClick(dictionary)
                    }
                }
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }

    }

    override fun onStop() {
        Log.d(TAG, "onStop()")
        undoRemoveDictionarySnackbar?.dismiss()
        super.onStop()
    }
}

interface OnDictionaryClickListener {
    fun onDictionaryClick(dictionary: Dictionary)
    fun onDictionaryLongClick(dictionary: Dictionary)
}
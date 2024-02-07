package my.dictionary.free.view.quiz

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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.navigation.AddUserQuizScreen
import my.dictionary.free.domain.models.navigation.EditQuizScreenFromQuizList
import my.dictionary.free.domain.models.navigation.UserQuizScreen
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.quiz.UserQuizzesViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.FetchDataState
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.SwipeDictionaryItem
import my.dictionary.free.view.widget.ListItemDecoration
import my.dictionary.free.view.widget.OnItemSwipedListener
import my.dictionary.free.view.widget.OnListItemClickListener
import my.dictionary.free.view.widget.OnListTouchListener

@AndroidEntryPoint
class UserQuizzesFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = UserQuizzesFragment::class.simpleName
        const val UNDO_SNACKBAR_DURATION: Int = 5000
        const val UNDO_SNACKBAR_DURATION_TIMER = 6000L
        const val UNDO_SNACKBAR_INTERVAL_TIMER = 1000L
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: UserQuizzesViewModel by viewModels()

    private lateinit var quizzesRecyclerView: RecyclerView
    private var undoRemoveQuizeSnackbar: Snackbar? = null
    private var menuInflater: MenuInflater? = null
    private var menuEdit: MenuItem? = null
    private var actionMode: ActionMode? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var quizzesAdapter: UserQuizzesAdapter? = null

    private val undoRemoveQuizTimer =
        object : CountDownTimer(
            UNDO_SNACKBAR_DURATION_TIMER, UNDO_SNACKBAR_INTERVAL_TIMER
        ) {
            @SuppressLint("RestrictedApi", "SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                undoRemoveQuizeSnackbar?.let {
                    ((it.view as ViewGroup).getChildAt(0) as SnackbarContentLayout?)?.messageView?.text =
                        "$seconds ${getString(R.string.seconds)}"
                }
            }

            override fun onFinish() {
                undoRemoveQuizeSnackbar = null
                quizzesAdapter?.getRemoveQuizeByTimer()?.let { quize ->
                    viewModel.deleteQuizzes(
                        context,
                        listOf(quize)
                    )
                }
                quizzesAdapter?.finallyRemoveItem()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_quizzes, null)
        quizzesRecyclerView = view.findViewById(R.id.recycler_view)
        quizzesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemTouchHelper =
            ItemTouchHelper(SwipeDictionaryItem(requireContext(), onItemSwipedListener))
        itemTouchHelper.attachToRecyclerView(quizzesRecyclerView)
        quizzesRecyclerView.addItemDecoration(ListItemDecoration(context = requireContext()))
        quizzesRecyclerView.addOnItemTouchListener(
            OnListTouchListener(
                requireContext(),
                quizzesRecyclerView,
                onQuizClickListener
            )
        )
        swipeRefreshLayout =
            view.findViewById<SwipeRefreshLayout?>(R.id.swipe_refresh_layout)?.also {
                it.setOnRefreshListener {
                    Log.d(TAG, "onRefresh")
                    refreshQuizzes()
                }
                it.setColorSchemeResources(
                    R.color.main,
                    R.color.main_light,
                    R.color.main_dark
                )
            }
        quizzesAdapter = UserQuizzesAdapter(mutableListOf(), mutableListOf())
        quizzesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        quizzesRecyclerView.adapter = quizzesAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        addMenuProvider(R.menu.menu_user_quizzes, { menu, mi ->
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
                    searchView.setOnQueryTextListener(onQuizzesQueryListener)
                }
            }
        }, {
            when (it) {
                R.id.nav_add_quize -> {
                    sharedViewModel.navigateTo(AddUserQuizScreen())
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        refreshQuizzes()
    }

    private fun refreshQuizzes() {
        lifecycleScope.launch {
            viewModel.loadQuizzes(context).collect {
                when (it) {
                    is FetchDataState.StartLoadingState -> {
                        quizzesAdapter?.clearData()
                        swipeRefreshLayout?.isRefreshing = true
                        sharedViewModel.loading(true)
                    }

                    is FetchDataState.FinishLoadingState -> {
                        swipeRefreshLayout?.isRefreshing = false
                        sharedViewModel.loading(false)
                        actionMode?.finish()
                    }

                    is FetchDataState.ErrorState -> {
                        displayError(
                            it.exception.message ?: context?.getString(R.string.unknown_error),
                            quizzesRecyclerView
                        )
                    }

                    is FetchDataState.DataState -> {
                        Log.d(TAG, "quiz updated: ${it.data}")
                        quizzesAdapter?.add(it.data)
                    }

                    is FetchDataState.ErrorStateString -> {
                        displayError(it.error, quizzesRecyclerView)
                    }
                }
            }
        }
    }

    private val onQuizzesQueryListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            quizzesAdapter?.filter?.filter(newText)
            return false
        }

    }

    private val onQuizClickListener = object : OnListItemClickListener {
        override fun onListItemClick(childView: View) {
            quizzesAdapter?.getItemByPosition(
                quizzesRecyclerView.getChildAdapterPosition(childView)
            )?.let { quiz ->
                if (actionMode == null) {
                    sharedViewModel.navigateTo(UserQuizScreen(quiz))
                } else {
                    selectQuiz(quiz)
                }
            }
        }

        override fun onListItemLongClick(childView: View) {
            quizzesAdapter?.getItemByPosition(
                quizzesRecyclerView.getChildAdapterPosition(childView)
            )?.let { quiz ->
                if (activity != null && activity is AppCompatActivity) {
                    if (actionMode == null) {
                        actionMode =
                            (activity as AppCompatActivity).startSupportActionMode(
                                actionModeCallBack
                            )
                    }
                }
                selectQuiz(quiz)
            }
        }
    }

    private fun selectQuiz(quiz: Quiz) {
        quizzesAdapter?.selectQuiz(quiz)
        val selectedQuizCount = quizzesAdapter?.getSelectedQuizzesCount() ?: 0
        actionMode?.title =
            "$selectedQuizCount ${getString(R.string.selected).uppercase()}"
        menuEdit?.isVisible = selectedQuizCount <= 1
        if (selectedQuizCount < 1) {
            actionMode?.finish()
            actionMode = null
            menuEdit = null
            quizzesAdapter?.clearSelectedQuizzes()
        }
    }

    private val onItemSwipedListener = object : OnItemSwipedListener {
        override fun onSwiped(position: Int) {
            Log.d(TAG, "onSwiped=$position")
            quizzesAdapter?.temporaryRemoveItem(position)
            undoRemoveQuizeSnackbar = Snackbar.make(
                quizzesRecyclerView,
                R.string.seconds,
                Snackbar.LENGTH_INDEFINITE
            )
                .setDuration(UNDO_SNACKBAR_DURATION)
                .setAction(R.string.undo) {
                    quizzesAdapter?.undoRemovedItem()
                }
            undoRemoveQuizeSnackbar?.show()
            undoRemoveQuizTimer.start()
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
                    quizzesAdapter?.getSelectedQuizzes()?.firstOrNull()?.let { quiz ->
                        actionMode?.finish()
                        sharedViewModel.navigateTo(EditQuizScreenFromQuizList(quiz))
                    }
                    true
                }

                R.id.menu_delete -> {
                    lifecycleScope.launch {
                        viewModel.deleteQuizzes(
                            context,
                            quizzesAdapter?.getSelectedQuizzes()
                        ).collect {
                            when (it) {
                                is FetchDataState.StartLoadingState -> {
                                    swipeRefreshLayout?.isRefreshing = true
                                    sharedViewModel.loading(true)
                                }

                                is FetchDataState.FinishLoadingState -> {
                                    swipeRefreshLayout?.isRefreshing = false
                                    sharedViewModel.loading(false)
                                    actionMode?.finish()
                                    refreshQuizzes()
                                }

                                is FetchDataState.ErrorState -> {
                                    displayError(
                                        it.exception.message
                                            ?: context?.getString(R.string.unknown_error),
                                        quizzesRecyclerView
                                    )
                                }

                                is FetchDataState.DataState -> {
                                    //skip
                                }

                                is FetchDataState.ErrorStateString -> {
                                    displayError(it.error, quizzesRecyclerView)
                                }
                            }
                        }
                    }
                    true
                }

                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            quizzesAdapter?.clearSelectedQuizzes()
            actionMode = null
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop()")
        undoRemoveQuizeSnackbar?.dismiss()
        super.onStop()
    }

}
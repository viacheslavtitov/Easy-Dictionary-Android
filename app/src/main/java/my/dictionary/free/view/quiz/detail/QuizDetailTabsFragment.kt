package my.dictionary.free.view.quiz.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.navigation.EditQuizScreenFromDetail
import my.dictionary.free.domain.models.navigation.RunQuizScreen
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.quiz.detail.QuizDetailTabsViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.FetchDataState
import my.dictionary.free.view.ext.addMenuProvider

@AndroidEntryPoint
class QuizDetailTabsFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = QuizDetailTabsFragment::class.simpleName
        const val BUNDLE_QUIZ_ID =
            "my.dictionary.free.view.quiz.detail.QuizDetailTabsFragment.BUNDLE_QUIZ_ID"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: QuizDetailTabsViewModel by viewModels()

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null

    private var quizId: String? = null
    private var adapter: QuizDetailTabsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz_detail_tabs, null)
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        addMenuProvider(R.menu.menu_quiz_detail, { menu, mi ->
        }, {
            when (it) {
                R.id.edit -> {
                    viewModel.getQuiz()?.let {
                        sharedViewModel.navigateTo(EditQuizScreenFromDetail(it))
                    }
                    return@addMenuProvider true
                }

                R.id.run_quiz -> {
                    viewModel.getQuiz()?.let {
                        Log.d(TAG, "run quiz")
                        sharedViewModel.navigateTo(RunQuizScreen(it))
                    }
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        quizId = arguments?.getString(BUNDLE_QUIZ_ID, null)
        loadQuiz()
    }

    private fun loadQuiz() {
        lifecycleScope.launch {
            viewModel.loadQuiz(context, quizId).collect {
                when (it) {
                    is FetchDataState.StartLoadingState -> {
                        sharedViewModel.loading(true)
                    }

                    is FetchDataState.FinishLoadingState -> {
                        sharedViewModel.loading(false)
                    }

                    is FetchDataState.ErrorState -> {
                        displayError(
                            it.exception.message ?: context?.getString(R.string.unknown_error),
                            viewPager
                        )
                    }

                    is FetchDataState.DataState -> {
                        adapter = QuizDetailTabsAdapter(this@QuizDetailTabsFragment, it.data)
                        viewPager?.adapter = adapter
                        TabLayoutMediator(tabLayout!!, viewPager!!) { tab, position ->
                            when (position) {
                                0 -> {
                                    tab.text = getString(R.string.quiz)
                                    tab.setIcon(R.drawable.ic_quiz)
                                }

                                1 -> {
                                    tab.text = getString(R.string.history)
                                    tab.setIcon(R.drawable.ic_quiz_history)
                                }
                            }
                        }.attach()
                    }

                    is FetchDataState.ErrorStateString -> {
                        displayError(it.error, viewPager)
                    }
                }
            }
        }
    }

}
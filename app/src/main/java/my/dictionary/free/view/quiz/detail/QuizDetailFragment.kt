package my.dictionary.free.view.quiz.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.quiz.detail.QuizDetailViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.words.DictionaryWordsAdapter
import my.dictionary.free.view.widget.ListItemDecoration

@AndroidEntryPoint
class QuizDetailFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = QuizDetailFragment::class.simpleName
        const val BUNDLE_QUIZ_ID =
            "my.dictionary.free.view.quiz.detail.QuizDetailFragment.BUNDLE_QUIZ_ID"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: QuizDetailViewModel by viewModels()

    private lateinit var wordsRecyclerView: RecyclerView
    private var nameTextView: AppCompatTextView? = null
    private var durationTextView: AppCompatTextView? = null
    private var dictionaryTextView: AppCompatTextView? = null

    private var wordsAdapter: DictionaryWordsAdapter? = null
    private var quizId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz_detail, null)
        nameTextView = view.findViewById(R.id.name)
        durationTextView = view.findViewById(R.id.duration)
        dictionaryTextView = view.findViewById(R.id.dictionary)
        wordsRecyclerView = view.findViewById(R.id.words_recycler_view)
        wordsRecyclerView.layoutManager = LinearLayoutManager(context)
        wordsRecyclerView.addItemDecoration(ListItemDecoration(context = requireContext()))
        wordsAdapter = DictionaryWordsAdapter(mutableListOf())
        wordsRecyclerView.adapter = wordsAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.displayErrorUIState.drop(1).collect { errorMessage ->
                        displayError(errorMessage, wordsRecyclerView)
                    }
                }
                launch {
                    viewModel.loadingUIState.collect { visible ->

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
                    viewModel.nameUIState.drop(1).collect { value ->
                        nameTextView?.text = value
                    }
                }
                launch {
                    viewModel.durationUIState.drop(1).collect { value ->
                        durationTextView?.text = value
                    }
                }
                launch {
                    viewModel.dictionaryUIState.drop(1).collect { value ->
                        dictionaryTextView?.text = value
                    }
                }
                launch {
                    viewModel.wordUIState.collect { word ->
                        wordsAdapter?.add(word)
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_quiz_detail, { menu, mi ->
        }, {
            when (it) {
                R.id.edit -> {

                    return@addMenuProvider true
                }

                R.id.run_quiz -> {

                    return@addMenuProvider true
                }

                else -> false
            }
        })
        quizId = arguments?.getString(BUNDLE_QUIZ_ID, null)
        viewModel.loadQuiz(context, quizId)
    }
}
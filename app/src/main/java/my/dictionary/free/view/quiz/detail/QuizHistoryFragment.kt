package my.dictionary.free.view.quiz.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.user.dictionary.words.DictionaryWordsAdapter
import my.dictionary.free.view.widget.ListItemDecoration

@AndroidEntryPoint
class QuizHistoryFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = QuizHistoryFragment::class.simpleName
        const val BUNDLE_QUIZ =
            "my.dictionary.free.view.quiz.detail.QuizHistoryFragment.BUNDLE_QUIZ"
    }

    private lateinit var quizHistoryRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz_history, null)
        quizHistoryRecyclerView = view.findViewById(R.id.quiz_history_recycler_view)
        quizHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
        quizHistoryRecyclerView.addItemDecoration(ListItemDecoration(context = requireContext()))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        val quiz = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_QUIZ,
            Quiz::class.java
        ) else arguments?.getParcelable(BUNDLE_QUIZ) as? Quiz
        quiz?.let {
            Log.d(TAG, "display histories ${quiz.histories.size}")
            quizHistoryRecyclerView.adapter = QuizHistoryAdapter(quiz.histories)
        }
    }

}
package my.dictionary.free.view.quiz.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.QuizResult
import my.dictionary.free.view.ext.getColorInt
import my.dictionary.free.view.quiz.UserQuizzesAdapter

class QuizHistoryAdapter(
    private val data: List<QuizResult>
): RecyclerView.Adapter<QuizHistoryAdapter.ViewHolder>()  {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: AppCompatTextView
        val langsTextView: AppCompatTextView
        val wordsCountTextView: AppCompatTextView
        val rightAnswersTextView: AppCompatTextView

        init {
            timeTextView = view.findViewById(R.id.time)
            langsTextView = view.findViewById(R.id.lang_pair)
            wordsCountTextView = view.findViewById(R.id.wordsCount)
            rightAnswersTextView = view.findViewById(R.id.rightAnswers)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_quiz_history, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val quiz = data[position]
        val context = viewHolder.itemView.context
        viewHolder.langsTextView.text = quiz.langPair
        viewHolder.timeTextView.text = quiz.dateTime
        viewHolder.wordsCountTextView.text = context.getString(R.string.total_words, quiz.wordsCount)
        viewHolder.rightAnswersTextView.text = context.getString(R.string.right_answers, quiz.rightAnswers)
    }

    override fun getItemCount() = data.size
}
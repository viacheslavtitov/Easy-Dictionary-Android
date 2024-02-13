package my.dictionary.free.view.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.view.ext.getColorInt

class UserQuizzesAdapter(
    private val data: MutableList<Quiz>,
    private val filteredData: MutableList<Quiz>
) :
    RecyclerView.Adapter<UserQuizzesAdapter.ViewHolder>(), Filterable {

    private var tempRemoveItem: Quiz? = null
    private var tempRemoveItemPosition: Int? = null
    private var selectedQuizzes = mutableListOf<Quiz>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var swipePosition: Int = 0
        val nameTextView: AppCompatTextView
        val langsTextView: AppCompatTextView

        init {
            nameTextView = view.findViewById(R.id.name)
            langsTextView = view.findViewById(R.id.lang_pair)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_quiz, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val quiz = filteredData[position]
        val context = viewHolder.itemView.context
        viewHolder.nameTextView.text = quiz.name
        quiz.dictionary?.let { dictionary ->
            viewHolder.langsTextView.text =
                "${dictionary.dictionaryFrom.langFull} - ${dictionary.dictionaryTo.langFull} (${quiz.quizWords.size})"
        }
        viewHolder.swipePosition = position
        val selected = selectedQuizzes.firstOrNull { it._id == quiz._id } != null
        viewHolder.itemView.setBackgroundColor(
            if (selected) context.getColorInt(R.color.gray_300) else context.getColorInt(
                R.color.gray_200
            )
        )
    }

    override fun getItemCount() = filteredData.size

    fun temporaryRemoveItem(position: Int, needToUpdate: Boolean = true) {
        if (position < filteredData.size && position > -1) {
            tempRemoveItem = data.removeAt(position)
            tempRemoveItemPosition = position
            if (needToUpdate) {
                this.notifyItemRemoved(position)
            }
        }
    }

    fun finallyRemoveItem() {
        tempRemoveItemPosition = null
        tempRemoveItem = null
    }

    fun getRemoveQuizeByTimer() = tempRemoveItem

    fun undoRemovedItem() {
        if (tempRemoveItemPosition != null && tempRemoveItem != null) {
            filteredData.add(tempRemoveItemPosition!!, tempRemoveItem!!)
            this.notifyItemInserted(tempRemoveItemPosition!!)
            tempRemoveItemPosition = null
            tempRemoveItem = null
        }
    }

    fun getItemByPosition(position: Int): Quiz? {
        return if (position < filteredData.size && position > -1) filteredData[position] else null
    }

    fun selectQuiz(quiz: Quiz) {
        val position = filteredData.indexOfFirst { it._id == quiz._id }
        if (selectedQuizzes.firstOrNull { it._id == quiz._id } == null) {
            selectedQuizzes.add(quiz)
            if (position > -1) {
                this.notifyItemChanged(position)
            }
        } else {
            val selectedPosition = selectedQuizzes.indexOfFirst { it._id == quiz._id }
            if (selectedPosition > -1) {
                selectedQuizzes.removeAt(selectedPosition)
            }
            if (position > -1) {
                this.notifyItemChanged(position)
            }
        }
    }

    fun getSelectedQuizzesCount() = selectedQuizzes.size

    fun getSelectedQuizzes() = selectedQuizzes

    fun clearSelectedQuizzes() {
        selectedQuizzes.clear()
        this.notifyDataSetChanged()
    }

    fun clearData() {
        data.clear()
        selectedQuizzes.clear()
        filteredData.clear()
        tempRemoveItemPosition = null
        tempRemoveItem = null
        this.notifyDataSetChanged()
    }

    fun add(quiz: Quiz) {
        data.add(quiz)
        filteredData.add(quiz)
        this.notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return filter
    }

    private val filter = object : Filter() {
        override fun performFiltering(query: CharSequence?): FilterResults {
            val filtered = data.filter { it.name.contains(query ?: "", true) }
            return FilterResults().apply {
                count = filtered.size
                values = filtered
            }
        }

        override fun publishResults(query: CharSequence?, fr: FilterResults?) {
            filteredData.clear()
            filteredData.addAll(fr?.values as? MutableList<Quiz> ?: emptyList())
            notifyDataSetChanged()
        }

    }

}
package my.dictionary.free.view.user.dictionary.words

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.domain.models.AlphabetSort
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.view.ext.getColorInt

class DictionaryWordsAdapter(
    private val data: MutableList<Word>,
    private val filteredData: MutableList<Word>
) :
    RecyclerView.Adapter<DictionaryWordsAdapter.ViewHolder>(), Filterable {

    private var tempRemoveItem: Word? = null
    private var tempRemoveItemPosition: Int? = null
    private var sort: AlphabetSort? = null
    private var selectedWords = mutableListOf<Word>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var swipePosition: Int = 0
        val originalTextView: AppCompatTextView
        val translatedTextView: AppCompatTextView
        val rootView: View

        init {
            originalTextView = view.findViewById(R.id.original_word)
            translatedTextView = view.findViewById(R.id.translated_word)
            rootView = view.findViewById(R.id.root)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_word, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val word = filteredData[position]
        val context = viewHolder.itemView.context
        viewHolder.originalTextView.text = word.original
        val firstTranslatedWord = word.translates.firstOrNull()?.translation ?: ""
        val translatedText =
            if (word.translates.size <= 1) "- $firstTranslatedWord" else "- $firstTranslatedWord..."
        viewHolder.translatedTextView.text = translatedText
        viewHolder.swipePosition = position
        val selected = selectedWords.firstOrNull { it._id == word._id } != null
        viewHolder.rootView.setBackgroundColor(
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

    fun getRemoveWordByTimer() = tempRemoveItem

    fun undoRemovedItem() {
        if (tempRemoveItemPosition != null && tempRemoveItem != null) {
            data.add(tempRemoveItemPosition!!, tempRemoveItem!!)
            this.notifyItemInserted(tempRemoveItemPosition!!)
            tempRemoveItemPosition = null
            tempRemoveItem = null
        }
    }

    fun getItemByPosition(position: Int): Word? {
        return if (position < filteredData.size && position > -1) filteredData[position] else null
    }

    fun selectWord(word: Word) {
        val position = filteredData.indexOfFirst { it._id == word._id }
        if (selectedWords.firstOrNull { it._id == word._id } == null) {
            selectedWords.add(word)
            if (position > -1) {
                this.notifyItemChanged(position)
            }
        } else {
            val selectedPosition = selectedWords.indexOfFirst { it._id == word._id }
            if (selectedPosition > -1) {
                selectedWords.removeAt(selectedPosition)
            }
            if (position > -1) {
                this.notifyItemChanged(position)
            }
        }
    }

    fun getSelectedWordsCount() = selectedWords.size

    fun getSelectedWords() = selectedWords

    fun getWords() = filteredData

    fun clearSelectedWords() {
        selectedWords.clear()
        this.notifyDataSetChanged()
    }

    fun selectAll() {
        selectedWords.clear()
        filteredData.forEach {
            selectedWords.add(it)
        }
        this.notifyDataSetChanged()
    }

    fun isAllSelected() = selectedWords.size == data.size

    fun clearData() {
        data.clear()
        filteredData.clear()
        selectedWords.clear()
        tempRemoveItemPosition = null
        tempRemoveItem = null
        this.notifyDataSetChanged()
    }

    fun add(dict: Word) {
        data.add(dict)
        filteredData.add(dict)
        needSortByAlphabet(sort)
        this.notifyDataSetChanged()
    }

    fun filterByCategory(categoryId: String?) {
        if(categoryId == null) {
            filteredData.clear()
            filteredData.addAll(data)
        } else {
            val filteredByCategory =
                data.filter { it.translates.filter { it.categoryId == categoryId }.isNotEmpty() }
            filteredData.clear()
            filteredData.addAll(filteredByCategory)
        }
        needSortByAlphabet(sort)
        this.notifyDataSetChanged()
    }

    fun sortByAlphabet(sort: AlphabetSort) {
        this.sort = sort
        needSortByAlphabet(sort)
        this.notifyDataSetChanged()
    }

    private fun needSortByAlphabet(sort: AlphabetSort?) {
        if(sort == null) return
        when(sort) {
            AlphabetSort.A_Z -> {
                filteredData.sortBy { it.original }
            }
            AlphabetSort.Z_A -> {
                filteredData.sortByDescending { it.original }
            }
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    private val filter = object : Filter() {
        override fun performFiltering(query: CharSequence?): FilterResults {
            val filtered = data.filter {
                it.original.contains(
                    query ?: "",
                    true
                ) || it.translates.filter { it.translation.contains(query ?: "", true) }
                    .isNotEmpty()
            }
            return FilterResults().apply {
                count = filtered.size
                values = filtered
            }
        }

        override fun publishResults(query: CharSequence?, fr: FilterResults?) {
            filteredData.clear()
            filteredData.addAll(fr?.values as? MutableList<Word> ?: emptyList())
            needSortByAlphabet(sort)
            notifyDataSetChanged()
        }
    }

}
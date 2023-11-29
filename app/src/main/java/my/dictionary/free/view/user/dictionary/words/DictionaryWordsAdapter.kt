package my.dictionary.free.view.user.dictionary.words

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.view.ext.getColorInt

class DictionaryWordsAdapter(
    private val data: MutableList<Word>
) :
    RecyclerView.Adapter<DictionaryWordsAdapter.ViewHolder>() {

    private var tempRemoveItem: Word? = null
    private var tempRemoveItemPosition: Int? = null
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
        val word = data[position]
        val context = viewHolder.itemView.context
        viewHolder.originalTextView.text = word.original
        val firstTranslatedWord = word.translates.firstOrNull()?.translate ?: ""
        val translatedText = if(word.translates.isEmpty()) "- $firstTranslatedWord" else "- $firstTranslatedWord..."
        viewHolder.translatedTextView.text = translatedText
        viewHolder.swipePosition = position
        val selected = selectedWords.firstOrNull { it._id == word._id } != null
        viewHolder.rootView.setBackgroundColor(
            if (selected) context.getColorInt(R.color.gray_300) else context.getColorInt(
                R.color.gray_200
            )
        )
    }

    override fun getItemCount() = data.size

    fun temporaryRemoveItem(position: Int, needToUpdate: Boolean = true) {
        if (position < data.size && position > -1) {
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
        return if (position < data.size && position > -1) data[position] else null
    }

    fun selectWord(word: Word) {
        val position = data.indexOfFirst { it._id == word._id }
        if (selectedWords.firstOrNull { it._id == word._id } == null) {
            selectedWords.add(word)
            if (position > -1) {
                this.notifyItemChanged(position)
            }
        } else {
            val selectedPosition = selectedWords.indexOfFirst { it._id == word._id }
            if(selectedPosition > -1) {
                selectedWords.removeAt(selectedPosition)
            }
            if (position > -1) {
                this.notifyItemChanged(position)
            }
        }
    }

    fun getSelectedWordsCount() = selectedWords.size

    fun getSelectedWords() = selectedWords

    fun clearSelectedWords() {
        selectedWords.clear()
        this.notifyDataSetChanged()
    }
    fun clearData() {
        data.clear()
        selectedWords.clear()
        tempRemoveItemPosition = null
        tempRemoveItem = null
        this.notifyDataSetChanged()
    }

    fun add(dict: Word) {
        data.add(dict)
        this.notifyDataSetChanged()
    }

}
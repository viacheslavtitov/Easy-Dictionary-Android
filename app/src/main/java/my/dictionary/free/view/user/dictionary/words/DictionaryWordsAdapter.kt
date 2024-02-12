package my.dictionary.free.view.user.dictionary.words

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.domain.models.AlphabetSort
import my.dictionary.free.domain.models.filter.FilterModel
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.tags.CategoryTag
import my.dictionary.free.domain.models.words.tags.Tag
import my.dictionary.free.domain.models.words.tags.WordTag
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.view.ext.dp
import my.dictionary.free.view.ext.getColorInt
import my.dictionary.free.view.ext.hide
import my.dictionary.free.view.ext.visible

class DictionaryWordsAdapter(
    private val data: MutableList<Word>,
    private val filteredData: MutableList<Word>,
    private val wordTypes: List<String>,
    private val onWordClickListener: OnWordClickListener? = null,
    private var expandTranslations: Boolean = false,
    private var hideTranslations: Boolean = false
) :
    RecyclerView.Adapter<DictionaryWordsAdapter.ViewHolder>(), Filterable {

    private var tempRemoveItem: Word? = null
    private var tempRemoveItemPosition: Int? = null
    private var sort: AlphabetSort? = null
    private var selectedWords = mutableListOf<Word>()
    private var filterModel: FilterModel? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var swipePosition: Int = 0
        val originalTextView: AppCompatTextView
        val translatedTextView: AppCompatTextView
        val dropDownImage: AppCompatImageView
        val rootView: View
        val titleContainer: View
        val translatedContainer: LinearLayoutCompat

        init {
            originalTextView = view.findViewById(R.id.original_word)
            translatedTextView = view.findViewById(R.id.translated_word)
            dropDownImage = view.findViewById(R.id.drop_down_image)
            rootView = view.findViewById(R.id.root)
            titleContainer = view.findViewById(R.id.title_container)
            translatedContainer = view.findViewById(R.id.translated_container)
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
        fillAllTranslations(viewHolder, context, word, hideTranslations)
        viewHolder.swipePosition = position
        val selected = selectedWords.firstOrNull { it._id == word._id } != null
        viewHolder.rootView.setBackgroundColor(
            if (selected) context.getColorInt(R.color.gray_300) else context.getColorInt(
                R.color.gray_200
            )
        )
        onWordClickListener?.let { listener ->
            viewHolder.rootView.setOnClickListener {
                listener.onClick(word)
            }
            viewHolder.rootView.setOnLongClickListener {
                listener.onLongClick(word)
                true
            }
        }
    }

    private fun fillAllTranslations(viewHolder: ViewHolder, context: Context, word: Word, hideTranslations: Boolean) {
        viewHolder.translatedContainer.removeAllViews()
        fillTranslationValue(context, viewHolder.translatedTextView, word.translates.first(), hideTranslations)
        if (word.translates.size <= 1) {
            viewHolder.dropDownImage.visible(false, View.GONE)
            viewHolder.titleContainer.setOnClickListener(null)
        } else {
            viewHolder.dropDownImage.visible(true)
            word.translates.forEachIndexed { index, translate ->
                if (index != 0) {
                    val addTranslationTextView = AppCompatTextView(viewHolder.rootView.context)
                    val params = LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                    )
                    addTranslationTextView.layoutParams = params
                    addTranslationTextView.textSize = 6.dp.toFloat()
                    addTranslationTextView.setTextColor(context.getColorInt(R.color.main_text))
                    fillTranslationValue(context, addTranslationTextView, translate, hideTranslations)
                    viewHolder.translatedContainer.addView(addTranslationTextView)
                }
            }
            viewHolder.titleContainer.setOnClickListener {
                viewHolder.translatedContainer.visible(
                    viewHolder.translatedContainer.visibility != View.VISIBLE,
                    View.GONE
                )
                viewHolder.dropDownImage.setImageResource(
                    if (viewHolder.translatedContainer.visibility == View.VISIBLE)
                        R.drawable.ic_baseline_arrow_drop_up_24 else R.drawable.ic_baseline_arrow_drop_down_24
                )
            }
            viewHolder.translatedContainer.visible(expandTranslations, View.GONE)
            viewHolder.dropDownImage.setImageResource(
                if (viewHolder.translatedContainer.visibility == View.VISIBLE)
                    R.drawable.ic_baseline_arrow_drop_up_24 else R.drawable.ic_baseline_arrow_drop_down_24
            )
            viewHolder.titleContainer.setOnLongClickListener {
                onWordClickListener?.onLongClick(word)
                true
            }
        }
        if(onWordClickListener == null) {
            viewHolder.rootView.setOnClickListener {
                fillAllTranslations(viewHolder, context, word, false)
            }
        }
    }

    private fun fillTranslationValue(
        context: Context,
        textView: AppCompatTextView,
        translation: TranslationVariant,
        hideTranslations: Boolean
    ) {
        val value = if(hideTranslations) translation.translation.hide() else translation.translation
        if (translation.category != null) {
            textView.setText(
                Html.fromHtml(
                    context.getString(
                        R.string.category_color_value,
                        translation.category!!.categoryName,
                        value
                    ), Html.FROM_HTML_MODE_LEGACY
                ), TextView.BufferType.SPANNABLE
            )
        } else {
            textView.text = "- $value"
        }
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

    fun add(dict: Word, query: String?) {
        data.add(dict)
        filteredData.add(dict)
        filterIfNeeds()
        needSortByAlphabet(sort)
        if (!query.isNullOrEmpty()) {
            getFilter().filter(query)
        } else {
            this.notifyDataSetChanged()
        }
    }

    fun filterByCategory(categoryId: String?) {
        if (categoryId == null) {
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
        if (sort == null) return
        when (sort) {
            AlphabetSort.A_Z -> {
                filteredData.sortBy { it.original }
            }

            AlphabetSort.Z_A -> {
                filteredData.sortByDescending { it.original }
            }
        }
    }

    fun setFilterModel(model: FilterModel?) {
        filterModel = model
        filterIfNeeds()
        needSortByAlphabet(sort)
        this.notifyDataSetChanged()
    }

    fun getFilteredModel() = filterModel

    private fun filterIfNeeds() {
        filterModel?.let { filter ->
            if (filter.tags.isNotEmpty() || filter.categories.isNotEmpty() || filter.types.isNotEmpty()) {
                val newFilteredData = mutableSetOf<Word>()
                (filter.tags as? List<WordTag>)?.let { tags ->
                    if (tags.isNotEmpty()) {
                        val matchWordTag = arrayListOf<Word>()
                        data.forEach { word ->
                            val matchTag =
                                tags.find { wordTag -> word.tags.find { it._id == wordTag._id } != null } != null
                            if (matchTag) {
                                matchWordTag.add(word)
                            }
                        }
                        newFilteredData.addAll(matchWordTag)
                    }
                }
                (filter.categories as? List<CategoryTag>)?.let { categories ->
                    if (categories.isNotEmpty()) {
                        val matchWordTag = arrayListOf<Word>()
                        data.forEach { word ->
                            val matchTag =
                                categories.find { categoryTag -> word.translates.find { it.categoryId == categoryTag.id } != null } != null
                            if (matchTag) {
                                matchWordTag.add(word)
                            }
                        }
                        newFilteredData.addAll(matchWordTag)
                    }
                }
                (filter.types as? List<Tag>)?.let { types ->
                    if (types.isNotEmpty()) {
                        val matchWordTag = arrayListOf<Word>()
                        for (word in data) {
                            if (word.type == 0) continue
                            val wordType = wordTypes[word.type]
                            val matchTag =
                                types.find { wordTag -> wordType == wordTag.tagName } != null
                            if (matchTag) {
                                matchWordTag.add(word)
                            }
                        }
                        newFilteredData.addAll(matchWordTag)
                    }
                }
                filteredData.clear()
                filteredData.addAll(newFilteredData)
            }
        }
    }

    fun expandTranslates(expand: Boolean) {
        expandTranslations = expand
        this.notifyDataSetChanged()
    }

    fun hideTranslates(hide: Boolean) {
        hideTranslations = hide
        this.notifyDataSetChanged()
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
            filterIfNeeds()
            needSortByAlphabet(sort)
            notifyDataSetChanged()
        }
    }

}
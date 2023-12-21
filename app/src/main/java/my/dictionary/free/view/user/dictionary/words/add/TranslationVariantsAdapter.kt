package my.dictionary.free.view.user.dictionary.words.add

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.view.ext.visible

class TranslationVariantsAdapter(
    private val data: MutableList<TranslationVariant> = mutableListOf(),
    private val listener: OnTranslationVariantEditListener
) :
    RecyclerView.Adapter<TranslationVariantsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryTextView: AppCompatTextView
        val translatedTextView: AppCompatTextView
        val translatedExampleTextView: AppCompatTextView
        val editView: View
        val deleteView: View
        val rootView: View

        init {
            categoryTextView = view.findViewById(R.id.category)
            translatedTextView = view.findViewById(R.id.translated_word)
            translatedExampleTextView = view.findViewById(R.id.translated_example)
            editView = view.findViewById(R.id.edit)
            deleteView = view.findViewById(R.id.delete)
            rootView = view.findViewById(R.id.root)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_translation_variant, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val variant = data[position]
        viewHolder.categoryTextView.visible(variant.category != null, View.GONE)
        variant.category?.let {
            viewHolder.categoryTextView.text = it.categoryName
        }
        viewHolder.translatedTextView.text =
            if (variant.categoryId != null) "- ${variant.translation}" else variant.translation
        viewHolder.translatedExampleTextView.visible(!variant.example.isNullOrEmpty(), View.GONE)
        if (!variant.example.isNullOrEmpty()) {
            viewHolder.translatedExampleTextView.text = variant.example
        }
        viewHolder.editView.setOnClickListener {
            listener.onEdit(variant)
        }
        viewHolder.deleteView.setOnClickListener {
            delete(position)
        }
    }

    override fun getItemCount() = data.size

    fun clear() {
        data.clear()
        this.notifyDataSetChanged()
    }

    fun add(translationVariant: TranslationVariant) {
        data.add(translationVariant)
        this.notifyDataSetChanged()
    }

    fun delete(position: Int) {
        data.removeAt(position)
        this.notifyDataSetChanged()
    }

    fun getData() = data
}

interface OnTranslationVariantEditListener {
    fun onEdit(entity: TranslationVariant)
}
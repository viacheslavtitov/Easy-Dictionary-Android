package my.dictionary.free.view.user.dictionary.words.translations.add

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatTextView
import my.dictionary.free.R
import my.dictionary.free.domain.models.words.variants.TranslationCategory

class CategorySpinnerAdapter(
    context: Context,
    private val data: MutableList<TranslationCategory> = mutableListOf(
        TranslationCategory.empty()
    )
) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): TranslationCategory = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parentView: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.item_spinner_single, parentView, false)
        view.findViewById<AppCompatTextView>(R.id.title).text = data[position].categoryName
        return view
    }

    fun add(item: TranslationCategory) {
        data.add(item)
        this.notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        data.add(TranslationCategory.empty())
        this.notifyDataSetChanged()
    }

    fun getItemByPosition(position: Int) = data[position]

    fun getItems() = data

    fun findPositionItem(translation: TranslationCategory): Int {
        var resultPosition = 0
        for ((index, value) in data.withIndex()) {
            if (value._id == translation._id) {
                resultPosition = index
                break
            }
        }
        return resultPosition
    }
}
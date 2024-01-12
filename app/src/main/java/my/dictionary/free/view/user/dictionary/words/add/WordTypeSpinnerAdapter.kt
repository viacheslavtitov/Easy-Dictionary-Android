package my.dictionary.free.view.user.dictionary.words.add

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatTextView
import my.dictionary.free.R

class WordTypeSpinnerAdapter(context: Context, private val data: List<String>) : BaseAdapter() {
    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): String = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parentView: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.item_spinner_single, parentView, false)
        view.findViewById<AppCompatTextView>(R.id.title).text = data[position]
        return view
    }

    fun getItemByPosition(position: Int) = data[position]

    fun findPositionItem(wordType: String): Int {
        var resultPosition = 0
        for ((index, value) in data.withIndex()) {
            if (value == wordType) {
                resultPosition = index
                break
            }
        }
        return resultPosition
    }
}
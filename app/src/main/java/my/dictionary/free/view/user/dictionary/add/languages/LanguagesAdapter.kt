package my.dictionary.free.view.user.dictionary.add.languages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.domain.models.language.Language

class LanguagesAdapter(private val data: List<Language>, private val clickListener: OnLanguageClickListener) :
    RecyclerView.Adapter<LanguagesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: AppCompatTextView

        init {
            nameTextView = view.findViewById(R.id.name)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_languages, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val lang = data[position]
        viewHolder.nameTextView.text = lang.value
        viewHolder.itemView.setOnClickListener { clickListener.onLanguageClick(lang) }
    }

    override fun getItemCount() = data.size
}

interface OnLanguageClickListener {
    fun onLanguageClick(language: Language)
}
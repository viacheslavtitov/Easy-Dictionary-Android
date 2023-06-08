package my.dictionary.free.view.user.dictionary.add.languages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.dictionary.free.R
import my.dictionary.free.domain.models.language.Language

class LanguagesAdapter(private val data: List<Language>, private val clickListener: OnLanguageClickListener) :
    RecyclerView.Adapter<LanguagesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: AppCompatTextView
        val flagImage: AppCompatImageView

        init {
            nameTextView = view.findViewById(R.id.name)
            flagImage = view.findViewById(R.id.image_flag_lang)
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
        Glide
            .with(viewHolder.itemView.context)
            .load(lang.flags.png)
            .centerCrop()
            .placeholder(R.drawable.ic_flag_neutral_default)
            .into(viewHolder.flagImage)
        viewHolder.itemView.setOnClickListener { clickListener.onLanguageClick(lang) }
    }

    override fun getItemCount() = data.size
}

interface OnLanguageClickListener {
    fun onLanguageClick(language: Language)
}
package my.dictionary.free.view.user.dictionary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary

class UserDictionaryAdapter(
    private val data: List<Dictionary>,
    private val clickListener: OnDictionaryClickListener
) :
    RecyclerView.Adapter<UserDictionaryAdapter.SimpleViewHolder>() {

    class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: AppCompatTextView
        val flagLangFromImage: AppCompatImageView
        val flagLangToImage: AppCompatImageView

        init {
            nameTextView = view.findViewById(R.id.lang_pair)
            flagLangFromImage = view.findViewById(R.id.image_flag_lang_from)
            flagLangToImage = view.findViewById(R.id.image_flag_lang_to)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SimpleViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_user_dictionary, viewGroup, false)
        return SimpleViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleViewHolder, position: Int) {
        val dictionary = data[position]
        if(dictionary.dialect != null) {
            viewHolder.nameTextView.text =
                "${dictionary.dictionaryFrom.langFull} - ${dictionary.dictionaryTo.langFull}\n${dictionary.dialect}"
        } else {
            viewHolder.nameTextView.text =
                "${dictionary.dictionaryFrom.langFull} - ${dictionary.dictionaryTo.langFull}"
        }
        Glide
            .with(viewHolder.itemView.context)
            .load(dictionary.dictionaryFrom.flag?.png)
            .centerCrop()
            .placeholder(R.drawable.ic_flag_neutral_default)
            .into(viewHolder.flagLangFromImage)
        Glide
            .with(viewHolder.itemView.context)
            .load(dictionary.dictionaryTo.flag?.png)
            .centerCrop()
            .placeholder(R.drawable.ic_flag_neutral_default)
            .into(viewHolder.flagLangToImage)
        viewHolder.itemView.setOnClickListener { clickListener.onDictionaryClick(dictionary) }
    }

    override fun getItemCount() = data.size

}

interface OnDictionaryClickListener {
    fun onDictionaryClick(dictionary: Dictionary)
}
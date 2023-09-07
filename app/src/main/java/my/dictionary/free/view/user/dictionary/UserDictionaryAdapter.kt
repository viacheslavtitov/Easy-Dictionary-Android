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
    private val data: MutableList<Dictionary>,
    private val clickListener: OnDictionaryClickListener
) :
    RecyclerView.Adapter<UserDictionaryAdapter.SimpleViewHolder>() {

    private var tempRemoveItem: Dictionary? = null
    private var tempRemoveItemPosition: Int? = null

    class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var swipePosition: Int = 0
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
        if (!dictionary.dialect.isNullOrEmpty()) {
            viewHolder.nameTextView.text =
                "${dictionary.dictionaryFrom.langFull} - ${dictionary.dictionaryTo.langFull}\n${dictionary.dialect}"
        } else {
            viewHolder.nameTextView.text =
                "${dictionary.dictionaryFrom.langFull} - ${dictionary.dictionaryTo.langFull}"
        }
        viewHolder.swipePosition = position
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

    fun temporaryRemoveItem(position: Int, needToUpdate: Boolean = true) {
        if(position < data.size && position > -1) {
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

    fun undoRemovedItem() {
        if (tempRemoveItemPosition != null && tempRemoveItem != null) {
            data.add(tempRemoveItemPosition!!, tempRemoveItem!!)
            this.notifyItemInserted(tempRemoveItemPosition!!)
            tempRemoveItemPosition = null
            tempRemoveItem = null
        }
    }

}

interface OnDictionaryClickListener {
    fun onDictionaryClick(dictionary: Dictionary)
}
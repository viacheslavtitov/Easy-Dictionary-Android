package my.dictionary.free.view.user.dictionary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.view.ext.getColorInt

class UserDictionaryAdapter(
    private val data: MutableList<Dictionary>,
    private val filteredData: MutableList<Dictionary>
) :
    RecyclerView.Adapter<UserDictionaryAdapter.ViewHolder>(), Filterable {

    private var tempRemoveItem: Dictionary? = null
    private var tempRemoveItemPosition: Int? = null
    private var selectedDictionaries = mutableListOf<Dictionary>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var swipePosition: Int = 0
        val nameTextView: AppCompatTextView
        val flagLangFromImage: AppCompatImageView
        val flagLangToImage: AppCompatImageView
        val rootView: View

        init {
            nameTextView = view.findViewById(R.id.lang_pair)
            flagLangFromImage = view.findViewById(R.id.image_flag_lang_from)
            flagLangToImage = view.findViewById(R.id.image_flag_lang_to)
            rootView = view.findViewById(R.id.root)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_user_dictionary, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val dictionary = data[position]
        val context = viewHolder.itemView.context
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
        val selected = selectedDictionaries.firstOrNull { it._id == dictionary._id } != null
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

    fun getRemoveDictionaryByTimer() = tempRemoveItem

    fun undoRemovedItem() {
        if (tempRemoveItemPosition != null && tempRemoveItem != null) {
            data.add(tempRemoveItemPosition!!, tempRemoveItem!!)
            this.notifyItemInserted(tempRemoveItemPosition!!)
            tempRemoveItemPosition = null
            tempRemoveItem = null
        }
    }

    fun getItemByPosition(position: Int): Dictionary? {
        return if (position < filteredData.size && position > -1) filteredData[position] else null
    }

    fun selectDictionary(dictionary: Dictionary) {
        val position = filteredData.indexOfFirst { it._id == dictionary._id }
        if (selectedDictionaries.firstOrNull { it._id == dictionary._id } == null) {
            selectedDictionaries.add(dictionary)
            if (position > -1) {
                this.notifyItemChanged(position)
            }
        } else {
            val selectedPosition = selectedDictionaries.indexOfFirst { it._id == dictionary._id }
            if (selectedPosition > -1) {
                selectedDictionaries.removeAt(selectedPosition)
            }
            if (position > -1) {
                this.notifyItemChanged(position)
            }
        }
    }

    fun getSelectedDictionariesCount() = selectedDictionaries.size

    fun getSelectedDictionaries() = selectedDictionaries

    fun clearSelectedDevices() {
        selectedDictionaries.clear()
        this.notifyDataSetChanged()
    }

    fun clearData() {
        data.clear()
        filteredData.clear()
        selectedDictionaries.clear()
        tempRemoveItemPosition = null
        tempRemoveItem = null
        this.notifyDataSetChanged()
    }

    fun add(dict: Dictionary) {
        data.add(dict)
        filteredData.add(dict)
        this.notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return filter
    }

    private val filter = object : Filter() {
        override fun performFiltering(query: CharSequence?): FilterResults {
            val filtered = data.filter {
                it.dictionaryFrom.langFull?.contains(
                    query ?: "",
                    true
                ) ?: false || it.dictionaryTo.langFull?.contains(query ?: "", true) ?: false
            }
            return FilterResults().apply {
                count = filtered.size
                values = filtered
            }
        }

        override fun publishResults(query: CharSequence?, fr: FilterResults?) {
            filteredData.clear()
            filteredData.addAll(fr?.values as? MutableList<Dictionary> ?: emptyList())
            notifyDataSetChanged()
        }

    }

}
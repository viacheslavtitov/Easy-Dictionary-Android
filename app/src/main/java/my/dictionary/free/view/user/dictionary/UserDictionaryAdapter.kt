package my.dictionary.free.view.user.dictionary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary

class UserDictionaryAdapter(
    private val data: List<Dictionary>,
    private val clickListener: OnDictionaryClickListener
) :
    RecyclerView.Adapter<UserDictionaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: AppCompatTextView

        init {
            nameTextView = view.findViewById(R.id.lang_pair)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_user_dictionary, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val dictionary = data[position]
        viewHolder.nameTextView.text = "${dictionary.langFromFull} - ${dictionary.langToFull}"
        viewHolder.itemView.setOnClickListener { clickListener.onDictionaryClick(dictionary) }
    }

    override fun getItemCount() = data.size
}

interface OnDictionaryClickListener {
    fun onDictionaryClick(dictionary: Dictionary)
}
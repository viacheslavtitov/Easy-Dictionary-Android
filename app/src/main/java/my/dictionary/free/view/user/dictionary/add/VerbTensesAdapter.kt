package my.dictionary.free.view.user.dictionary.add

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.VerbTense

class VerbTensesAdapter(
    private val data: ArrayList<VerbTense>,
    private val listener: OnVerbTenseEditListener
) :
    RecyclerView.Adapter<VerbTensesAdapter.ViewHolder>() {

    companion object {
        private val TAG = VerbTensesAdapter::class.simpleName
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: AppCompatTextView
        val deleteView: View
        val rootView: View

        init {
            nameTextView = view.findViewById(R.id.name)
            deleteView = view.findViewById(R.id.delete)
            rootView = view.findViewById(R.id.root)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_verb_tenses, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val tense = data[position]
        viewHolder.nameTextView.text = tense.name
        viewHolder.deleteView.setOnClickListener {
            delete(position)
            listener.onDelete(tense)
        }
    }

    override fun getItemCount() = data.size

    fun clear() {
        data.clear()
        this.notifyDataSetChanged()
    }

    fun add(entity: VerbTense) {
        if(data.find { entity.name == it.name } == null) {
            data.add(entity)
            this.notifyDataSetChanged()
        } else {
            Log.d(TAG, "${entity.name} is already exist")
        }
    }

    fun edit(entity: VerbTense, oldName: String) {
        if (data.find { it._id == entity._id || it.name == oldName } != null) {
            val existIndex = data.indexOfFirst {
                it._id == entity._id || it.name == oldName
            }
            if (existIndex > -1) {
                data[existIndex] = entity
            }
            this.notifyDataSetChanged()
        }
    }

    fun delete(position: Int) {
        data.removeAt(position)
        this.notifyDataSetChanged()
    }

    fun getData() = data
}

interface OnVerbTenseEditListener {
    fun onDelete(entity: VerbTense)
}
package my.dictionary.free.view.widget

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class OnListTouchListener(
    context: Context,
    private val recyclerView: RecyclerView,
    private val onListItemClickListener: OnListItemClickListener
) : RecyclerView.OnItemTouchListener {

    private val gestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                child?.let {
                    onListItemClickListener.onListItemLongClick(it)
                }
            }
        })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val child = rv.findChildViewUnder(e.x, e.y)
        child?.let {
            if (gestureDetector.onTouchEvent(e)) {
                onListItemClickListener.onListItemClick(it)
            }
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
}

interface OnListItemClickListener {
    fun onListItemClick(childView: View)
    fun onListItemLongClick(childView: View)
}
package my.dictionary.free.view.user.dictionary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.view.widget.OnItemSwipedListener

class SwipeDictionaryItem(
    private val context: Context,
    private val onItemSwipedListener: OnItemSwipedListener,
    bckgColor: Int? = null,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val DEFAULT_BACKGROUND_COLOR = ContextCompat.getColor(context, R.color.main_light)
    private var backgroundColor: Int = DEFAULT_BACKGROUND_COLOR
    private val backgroundPaint: Paint by lazy {
        Paint().apply {
            color = backgroundColor
        }
    }

    init {
        bckgColor?.let {
            this.backgroundColor = it
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if(direction == ItemTouchHelper.LEFT) {
            when(viewHolder) {
                is UserDictionaryAdapter.ViewHolder -> {
                    onItemSwipedListener.onSwiped(viewHolder.swipePosition)
                }
            }
        }
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (isCurrentlyActive) {
                val itemView: View = viewHolder.itemView
//            if(dX < 0) {
                val backgroundRect = RectF(
                    itemView.left.toFloat(),
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                canvas.drawRoundRect(backgroundRect, 0.0f, 0.0f, backgroundPaint)
//            } else {
//                canvas.drawRect(itemView.left.toFloat(),
//                    itemView.top.toFloat(),
//                    itemView.right.toFloat(),
//                    itemView.bottom.toFloat(), Paint())
//                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//            }
            }
        }
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
    }
}
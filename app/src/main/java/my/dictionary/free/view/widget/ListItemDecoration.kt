package my.dictionary.free.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import my.dictionary.free.R
import my.dictionary.free.view.ext.dp
import my.dictionary.free.view.ext.getColorInt

class ListItemDecoration(
    private val context: Context,
    private val dividerHeight: Int = 1.dp,
    private val skipLastPosition: Boolean = true,
    @ColorRes private val colorRes: Int = R.color.light_blue_200
) : RecyclerView.ItemDecoration() {

    private val backgroundPaint by lazy {
        Paint().apply {
            color = context.getColorInt(colorRes)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    //distance between items (height of divider)
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
//        parent.adapter?.let { adapter ->
//            val childAdapterPosition = parent.getChildAdapterPosition(view)
//                .let { if (it == RecyclerView.NO_POSITION) return else it }
//        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.adapter?.let { adapter ->
            parent.children // Displayed children on screen
                .forEach { view ->
                    val childAdapterPosition = parent.getChildAdapterPosition(view)
                        .let { if (it == RecyclerView.NO_POSITION) return else it }
                    if (childAdapterPosition != adapter.itemCount - 1) {
                        val left = view.left
                        val top = view.bottom
                        val right = view.right
                        val bottom = view.bottom + dividerHeight
                        val rect = Rect(left, top, right, bottom)
                        c.drawRect(rect, backgroundPaint)
                    }
                }
        }
    }

}
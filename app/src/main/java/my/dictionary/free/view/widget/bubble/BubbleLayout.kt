package my.dictionary.free.view.widget.bubble

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import my.dictionary.free.view.ext.findViewByCoordinate
import kotlin.math.max


class BubbleLayout : ViewGroup {
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxHeight = 0
        var maxWidth = 0
        // Find out how big everyone wants to be
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        // Find rightmost and bottom-most child
        for (i in 0 until childCount) {
            val child: View = getChildAt(i)
            if (child.visibility != GONE) {
                maxWidth = max(maxWidth, child.measuredWidth)
                maxHeight = max(maxHeight, child.measuredHeight)
            }
        }
        // Account for padding too
        maxWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom
        // Check against minimum height and width
        maxHeight = max(maxHeight, suggestedMinimumHeight)
        maxWidth = max(maxWidth, suggestedMinimumWidth)

        val measureWidth = resolveSizeAndState(maxWidth, widthMeasureSpec, 0)
        val measureHeight = resolveSizeAndState(maxHeight, heightMeasureSpec, 0)
        setMeasuredDimension(
            measureWidth,
            measureHeight
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var nextChildTop = top
        var nextChildRight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val childLayoutParams = child.layoutParams as BubbleLayoutParams
                val leftOffset = childLayoutParams.leftMargin
                val topOffset = childLayoutParams.topMargin
                val rightOffset = childLayoutParams.rightMargin
                val bottomOffset = childLayoutParams.bottomMargin

                var childLeft = nextChildRight + child.left + leftOffset
                var childTop = child.top + topOffset
                var childRight = childLeft + child.measuredWidth + rightOffset
                var childBottom = childTop + child.measuredHeight

                if (childRight >= right) {
                    nextChildRight = 0
                    childLeft = nextChildRight + child.left + leftOffset
                    childTop = nextChildTop + topOffset
                    childRight = childLeft + child.measuredWidth + rightOffset
                    childBottom = childTop + child.measuredHeight
                }

                child.layout(
                    childLeft, childTop,
                    childRight,
                    childBottom
                )
                nextChildTop = child.bottom
                nextChildRight += childRight
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                (findViewByCoordinate(ev.x, ev.y) as? BubbleView)?.let {
                    it.select(!it.selected())
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    class BubbleLayoutParams(width: Int, height: Int) : LayoutParams(width, height) {
        var leftMargin = 0
        var topMargin = 0
        var rightMargin = 0
        var bottomMargin = 0
    }
}
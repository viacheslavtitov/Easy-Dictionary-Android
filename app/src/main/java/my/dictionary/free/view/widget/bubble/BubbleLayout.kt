package my.dictionary.free.view.widget.bubble

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import my.dictionary.free.domain.models.words.tags.Tag
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

    private var readOnly: Boolean = false

    fun init(context: Context, attrs: AttributeSet?) {
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxHeight = 0
        var maxWidth = 0
        val isWrapContent = layoutParams.height == LayoutParams.WRAP_CONTENT
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
        if (isWrapContent) {
            maxHeight = 0
            maxWidth = 0
            var childrenRowsCount = 1
            for (i in 0 until childCount) {
                val child: View = getChildAt(i)
                if (child.visibility != GONE) {
                    val childLayoutParams = child.layoutParams as BubbleLayoutParams
                    maxHeight = max(maxHeight, child.measuredHeight)
                    maxWidth += child.measuredWidth + childLayoutParams.leftMargin + childLayoutParams.rightMargin
                    if (maxWidth > measureWidth) {
                        maxWidth = 0
                        childrenRowsCount++
                    }
                }
            }
            if (childrenRowsCount > 0) childrenRowsCount++
            maxHeight *= childrenRowsCount
        }
        val measureHeight = resolveSizeAndState(maxHeight, heightMeasureSpec, 0)

        setMeasuredDimension(
            measureWidth,
            measureHeight
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var currentChildTop = 0
        var previousChildBottom = 0
        var nextChildRight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val childLayoutParams = child.layoutParams as BubbleLayoutParams
                val leftOffset = childLayoutParams.leftMargin
                val topOffset = childLayoutParams.topMargin
                val rightOffset = childLayoutParams.rightMargin
                val bottomOffset = childLayoutParams.bottomMargin

                var childLeft = nextChildRight + leftOffset
                var childTop = currentChildTop + topOffset
                var childRight = childLeft + child.measuredWidth + rightOffset
                var childBottom = childTop + child.measuredHeight

                if (childRight >= right) {
                    nextChildRight = 0
                    childLeft = leftOffset
                    childTop = previousChildBottom + topOffset
                    childRight = childLeft + child.measuredWidth
                    childBottom = childTop + child.measuredHeight
                    currentChildTop = childTop
                }

                child.layout(
                    childLeft, childTop,
                    childRight,
                    childBottom
                )
                previousChildBottom = child.bottom
                nextChildRight = childRight
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (!readOnly) {
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    (findViewByCoordinate(ev.x, ev.y) as? BubbleView)?.let {
                        it.select(!it.selected())
                        if(it.isHide()) {
                            it.isHide(false)
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun <T>getTags(selected: Boolean): ArrayList<T> {
        val tags = arrayListOf<T>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE && child is BubbleView) {
                val childSelected = child.selected()
                if (selected == childSelected) {
                    child.getWordTag()?.let {
                        tags.add(it as T)
                    }
                }
            }
        }
        return tags
    }

    fun setReadOnly(enable: Boolean) {
        readOnly = enable
    }

    class BubbleLayoutParams(width: Int, height: Int) : LayoutParams(width, height) {
        var leftMargin = 0
        var topMargin = 0
        var rightMargin = 0
        var bottomMargin = 0
    }
}
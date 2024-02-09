package my.dictionary.free.view.widget.bubble

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import my.dictionary.free.domain.models.words.tags.Tag
import my.dictionary.free.view.ext.dp

class BubbleView : View {

    companion object {
        private const val TEXT_PADDING = 9
        private const val TEXT_SIZE = 18
        private const val PADDING = 9
    }

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

    //    private var parentPaddingRect: Rect = Rect()
    private var sidesRadius: Float = 0.0f
    private var textHeight: Float = 0.0f
    private var textWidth: Float = 0.0f
    private var textExactCenterY: Float = 0.0f
    private var textY: Float = 0.0f
    private var textX: Float = 0.0f
    private var paddingContainer: Int = 0
    private var rectangleRect = RectF()
    private var selected = false
    private var isHiden = false

    private val textPaint by lazy {
        TextPaint().apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            textSize = TEXT_SIZE.dp.toFloat()
        }
    }

    private val selectBackgroundPaint =
        Paint().apply {
            color = Color.rgb(0, 104, 191)
            style = Paint.Style.FILL
            isAntiAlias = true

        }

    private val unSelectBackgroundPaint: Paint by lazy {
        Paint().apply {
            color = Color.rgb(110, 197, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    private var wordTag: Tag? = null

    fun init(context: Context, attrs: AttributeSet?) {
        val params = BubbleLayout.BubbleLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        paddingContainer = PADDING.dp
        params.leftMargin = paddingContainer
        params.topMargin = paddingContainer
        params.rightMargin = paddingContainer
        params.bottomMargin = paddingContainer
        layoutParams = params
    }

    fun setWordTag(tag: Tag?) {
        this.wordTag = tag
        invalidate()
    }

    fun getWordTag() = wordTag

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val bounds = Rect()
        val text = wordTag?.tagName ?: ""
        textPaint.getTextBounds(text, 0, text.length, bounds)
        textExactCenterY = bounds.exactCenterY()
        textHeight = bounds.height().toFloat()
        textWidth = bounds.width().toFloat()

        val desiredHeightText = bounds.height() + (TEXT_PADDING.dp * 2)
        sidesRadius = desiredHeightText / 2.0f
        val desiredWidthText = bounds.width() + (sidesRadius * 2)
        setMeasuredDimension(desiredWidthText.toInt(), desiredHeightText)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val leftRectangle = 0.0f + sidesRadius
        val topRectangle = 0.0f
        val rightRectangle = leftRectangle + textWidth
        val bottomRectangle = bottom.toFloat()
        rectangleRect.left = leftRectangle
        rectangleRect.top = topRectangle
        rectangleRect.right = rightRectangle
        rectangleRect.bottom = bottomRectangle
        textX = leftRectangle
        textY = (height / 2) - textExactCenterY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        wordTag?.tagName?.let { tag ->
            if (selected()) {
                drawBackground(canvas, selectBackgroundPaint)
            } else {
                drawBackground(canvas, unSelectBackgroundPaint)
            }
            if (!isHiden)
                drawText(canvas, tag)
        }
    }

    private fun drawBackground(canvas: Canvas, paint: Paint) {
        canvas.drawRect(rectangleRect, paint)
        canvas.drawCircle(
            rectangleRect.left,
            (rectangleRect.top + sidesRadius), sidesRadius, paint
        )
        canvas.drawCircle(
            rectangleRect.right,
            (rectangleRect.top + sidesRadius), sidesRadius, paint
        )
    }

    private fun drawText(canvas: Canvas, tag: String) {
        canvas.drawText(
            tag,
            textX,
            textY,
            textPaint
        )
    }

    fun select(select: Boolean) {
        selected = select
        invalidate()
    }

    fun selected() = selected

    fun isHide(hide: Boolean) {
        isHiden = hide
        invalidate()
    }

    fun isHide() = isHiden
}
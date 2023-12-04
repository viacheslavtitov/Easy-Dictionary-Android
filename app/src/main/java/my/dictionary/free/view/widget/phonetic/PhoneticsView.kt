package my.dictionary.free.view.widget.phonetic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import my.dictionary.free.R
import my.dictionary.free.view.ext.dp

class PhoneticsView : View {

    companion object {
        private const val LINES = 4
        private const val WIDTH_GRID_SIZE = 2
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

    private var backgroundColorRes: Int = 0

    private val backgroundPaint: Paint by lazy {
        Paint().apply {
            color = backgroundColorRes
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    private val textPaint: Paint by lazy {
        Paint().apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            textSize = symbolTextSize
        }
    }

    private val gridPaint: Paint by lazy {
        Paint().apply {
            color = Color.rgb(238, 238, 238)
        }
    }

    private var symbolCount: Int = 0
    private var symbolsInLine: Int = 0
    private var phoneticList: List<String> = emptyList()
    private var symbolWidthView: Float = 0.0f
    private var symbolHeightView: Float = 0.0f
    private var symbolTextSize: Float = 0.0f

    private var clickListener: OnPhoneticClickListener? = null

    fun init(context: Context, attrs: AttributeSet?) {
        backgroundColorRes = context.resources.getColor(R.color.gray_300)
//        context.theme.obtainStyledAttributes(
//            attrs,
//            R.styleable.ProgressDotsView,
//            0, 0
//        ).apply {
//            try {
//                dotsCount = getInteger(R.styleable.ProgressDotsView_dotsCount, 0)
//                inactiveColorRes = getColor(
//                    R.styleable.ProgressDotsView_inActiveColor,
//                    R.color.text_color_secondary
//                )
//                activeColorRes =
//                    getColor(R.styleable.ProgressDotsView_activeColor, R.color.main_background)
//                radius = getDimensionPixelSize(R.styleable.ProgressDotsView_radius, 20)
//                widthBetweenDots =
//                    getDimensionPixelSize(
//                        R.styleable.ProgressDotsView_widthBetweenDots,
//                        WIDTH_BETWEEN_DOTS
//                    )
//            } finally {
//                recycle()
//            }
//        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawGrid(canvas, LINES, symbolsInLine)
        var currentLine = 1
        phoneticList.forEachIndexed { index, symbol ->
            val start = (currentLine * symbolsInLine) - symbolsInLine
            val end = currentLine * symbolsInLine
            var indexInLine = index - start
            val range = IntRange(
                start = start,
                endInclusive = end
            )
            if (!range.contains(index) || index == end) {
                currentLine += 1
                indexInLine = 0
            }
            if (currentLine <= LINES) {
                drawSymbol(canvas, symbol, currentLine, indexInLine)
            }
        }
    }

    private fun drawSymbol(canvas: Canvas, symbol: String, line: Int, indexInLine: Int) {
        val leftR = indexInLine * symbolWidthView
        val topR = height - ((line - 1) * symbolHeightView)
        val rightR = leftR + symbolWidthView
        val bottomR = topR - symbolHeightView
        val rect = RectF().apply {
            left = leftR
            top = topR
            right = rightR
            bottom = bottomR
        }
        canvas.drawText(
            symbol,
            rect.centerX() - symbolTextSize / 4,
            rect.centerY() + symbolTextSize / 4,
            textPaint
        )
//        canvas.drawCircle(rect.centerX(), rect.centerY(), 5.0f, textPaint)
    }

    private fun drawGrid(canvas: Canvas, lineCount: Int, symbolInLine: Int) {
        val widthGrid = WIDTH_GRID_SIZE.dp.toFloat()
        for (lineNumber in 1..lineCount) {
            //draw horizontal
            val startX = 0.0f
            val startY = height - symbolHeightView * lineNumber
            val endX = width.toFloat()
            val endY = startY - widthGrid
            canvas.drawLine(startX, startY, endX, endY, gridPaint)
        }
        for (symbolNumber in 1..symbolInLine) {
            //draw vertical
            val startX = width - symbolWidthView * symbolNumber
            val startY = height.toFloat()
            val endX = startX - widthGrid
            val endY = 0.0f
            canvas.drawLine(startX, startY, endX, endY, gridPaint)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        val rect = RectF().apply {
            bottom = 0.0f
            left = 0.0f
            right = width.toFloat()
            top = height.toFloat()
        }
        canvas.drawRect(rect, backgroundPaint)
    }

    fun setPhonetics(list: List<String>) {
        phoneticList = list
        symbolCount = list.count()
        symbolsInLine = symbolCount / LINES
        if (symbolCount % LINES != 0) {
            symbolsInLine += 1
        }
        symbolWidthView = (width / symbolsInLine).toFloat()
        symbolHeightView = (height / LINES).toFloat()
        symbolTextSize = (listOf(symbolWidthView, symbolHeightView).min() / 2)
        invalidate()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (!phoneticList.isNullOrEmpty()) {
            event?.let {
                if (event.action == MotionEvent.ACTION_UP) {
                    val matchX = event.x
                    val matchY = event.y
                    var horizontalLineItem = -1
                    var verticalLineItem = -1
                    for (lineNumber in 1..LINES) {
                        //draw horizontal
                        val startX = 0.0f
                        val startY = height - symbolHeightView * lineNumber
                        val endX = width.toFloat()
                        val endY = startY + symbolHeightView
                        if (matchCoordinates(startX, startY, endX, endY, matchX, matchY)) {
                            horizontalLineItem = lineNumber
                            break
                        }
                    }
                    for (symbolNumber in 1..symbolsInLine) {
                        //draw vertical
                        val startX = symbolWidthView * symbolNumber - symbolWidthView
                        val startY = 0.0f
                        val endX = startX + symbolWidthView
                        val endY = height.toFloat()
                        if (matchCoordinates(startX, startY, endX, endY, matchX, matchY)) {
                            verticalLineItem = symbolNumber
                            break
                        }
                    }
                    if (horizontalLineItem >= 0 && verticalLineItem >= 0) {
                        var resultSymbolPosition =
                            horizontalLineItem * symbolsInLine - symbolsInLine + verticalLineItem - 1
                        if (resultSymbolPosition >= 0 && resultSymbolPosition < phoneticList.size) {
                            clickListener?.let {
                                it.onPhoneticClick(
                                    resultSymbolPosition,
                                    phoneticList[resultSymbolPosition]
                                )
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    private fun matchCoordinates(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        matchX: Float,
        matchY: Float
    ): Boolean {
        val rect = RectF().apply {
            left = startX
            top = startY
            right = endX
            bottom = endY
        }
        return rect.contains(matchX, matchY)
    }

    fun setOnClickListener(listener: OnPhoneticClickListener) {
        clickListener = listener
    }
}

interface OnPhoneticClickListener {
    fun onPhoneticClick(position: Int, symbol: String)
}
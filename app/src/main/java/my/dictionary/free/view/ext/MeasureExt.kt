package my.dictionary.free.view.ext

import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.roundToInt

/**
 * Convert Int value into DP
 */
val Int.dp: Int get() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics
).roundToInt()

/**
 * Convert Int value into PX
 */
val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
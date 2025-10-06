package org.easydictionary.app.view.ext

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
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

/**
 * Convert Dp value into PX
 */
@Composable
fun Dp.toPx(): Float {
    val density = LocalDensity.current
    return with(density) { this@toPx.toPx() }
}

/**
 * Convert Dp value into PX
 */
fun Dp.toPx(density: Density): Float = with(density) { this@toPx.toPx() }

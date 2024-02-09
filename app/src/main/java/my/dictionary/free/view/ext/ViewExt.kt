package my.dictionary.free.view.ext

import android.animation.Animator
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.text.Spannable
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.widget.ImageViewCompat

fun View.visible(
    visible: Boolean = true, invisibleStrategy: Int = View.INVISIBLE,
    durationInMillisecondsAnimation: Long = 0L, animationListener: Animator.AnimatorListener? = null
) {
    if (durationInMillisecondsAnimation > 0) {
        alpha = if (visible) 0f else 1f
        visibility = View.VISIBLE
        animate()
            .alpha(if (visible) 1f else 0f)
            .setDuration(durationInMillisecondsAnimation)
            .setListener(animationListener)
    }
    visibility = if (visible) View.VISIBLE else invisibleStrategy
}

fun ImageView.setSvgColor(@ColorRes color: Int) =
    setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN)

fun ImageView.setTint(@ColorRes color: Int?) {
    if (color == null) {
        ImageViewCompat.setImageTintList(this, null)
    } else {
        ImageViewCompat.setImageTintList(
            this,
            ColorStateList.valueOf(ContextCompat.getColor(context, color))
        )
    }
}

fun ImageView.setImageDrawableWithAnimation(drawable: Drawable, duration: Int = 300) {
    val currentDrawable = getDrawable()
    if (currentDrawable == null) {
        setImageDrawable(drawable)
        return
    }

    val transitionDrawable = TransitionDrawable(
        arrayOf(
            currentDrawable,
            drawable
        )
    )
    setImageDrawable(transitionDrawable)
    transitionDrawable.startTransition(duration)
}

/**
 * @param boldText - exists part of <b>fullText</b> param
 */
fun TextView.setBoldText(
    fullText: String,
    boldText: String
) {
    val allText = String.format(fullText, boldText)
    val startSpan = allText.indexOf(boldText)
    val endSpan = startSpan + boldText.length
    text = buildSpannedString {
        append(allText)
        setSpan(
            StyleSpan(Typeface.BOLD),
            startSpan,
            endSpan,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

fun ViewGroup.findViewByCoordinate(x: Float, y: Float): View? {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child.visibility != ViewGroup.GONE) {
            if (x >= 0 && y >= 0 && (x >= child.left && x <= child.right) && (y <= child.bottom && y >= child.top)) return child
        }
    }
    return null
}
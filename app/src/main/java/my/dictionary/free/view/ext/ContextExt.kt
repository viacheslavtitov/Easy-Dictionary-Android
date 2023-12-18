package my.dictionary.free.view.ext

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Context.getColorInt(@ColorRes colorResId: Int) = ContextCompat.getColor(this, colorResId)

/**
 * Hide keyboard which focused in passed view
 */
fun Context.hideKeyboard(view: View?) {
    view?.let {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
package my.dictionary.free.view.ext

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Context.getColorInt(@ColorRes colorResId: Int) = ContextCompat.getColor(this, colorResId)
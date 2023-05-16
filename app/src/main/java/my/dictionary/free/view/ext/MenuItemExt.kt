package my.dictionary.free.view.ext

import android.content.Context
import android.content.res.ColorStateList
import android.view.MenuItem
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import my.dictionary.free.R

fun MenuItem.setTint(context: Context, @ColorRes color: Int?) {
    if (color == null) {
        MenuItemCompat.setIconTintList(this, null)
    } else {
        MenuItemCompat.setIconTintList(this, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))
    }
}
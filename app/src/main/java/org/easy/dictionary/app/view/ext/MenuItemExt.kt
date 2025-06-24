package org.easy.dictionary.app.view.ext

import android.content.Context
import android.content.res.ColorStateList
import android.view.MenuItem
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import org.easy.dictionary.app.R

fun MenuItem.setTint(context: Context, @ColorRes color: Int?) {
    if (color == null) {
        MenuItemCompat.setIconTintList(this, null)
    } else {
        MenuItemCompat.setIconTintList(this, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))
    }
}
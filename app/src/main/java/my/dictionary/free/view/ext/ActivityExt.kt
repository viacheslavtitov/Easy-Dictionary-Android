package my.dictionary.free.view.ext

import android.app.Activity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Activity.visibleSystemBars(
    visible: Boolean = true,
    type: Int = WindowInsetsCompat.Type.statusBars(),
    behavior: Int = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
) {
    val windowInsetsController =
        ViewCompat.getWindowInsetsController(window.decorView) ?: return
    // Configure the behavior of the hidden system bars
    windowInsetsController.systemBarsBehavior = behavior
    // Hide both the status bar and the navigation bar
    if(visible) {
        windowInsetsController.show(type)
    } else {
        windowInsetsController.hide(type)
    }
}
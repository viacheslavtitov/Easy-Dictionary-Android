package my.dictionary.free.domain.utils

import android.os.Build

/**
 * Return true if Android version is Tiramisu or higher
 */
fun hasTiramisu() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/**
 * Return true if Android version is Lollipop or higher
 */

fun hasL() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

/**
 * Return true if Android version is Marshmallow or higher
 */

fun hasMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

/**
 * Return true if Android version is Nougat or higher
 */
fun hasNougat() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

/**
 * Return true if Android version is Oreo or higher
 */
fun hasOreo() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/**
 * Return true if Android version is Oreo MR1 or higher
 */
fun hasOreoMR1() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

/**
 * Return true if Android version is Q or higher
 */
fun hasQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

/**
 * Return true if Android version is R or higher
 */
fun hasR() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

/**
 * Return true if Android version is P or higher
 */
fun hasP() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
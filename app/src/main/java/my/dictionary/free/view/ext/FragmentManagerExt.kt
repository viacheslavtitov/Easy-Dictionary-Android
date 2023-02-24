package my.dictionary.free.view.ext

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun FragmentManager.findAndDismissDialog(tag: String) {
    this.findFragmentByTag(tag)?.let {
        if (it is DialogFragment) {
            it.dismiss()
        }
    }
}

inline fun <reified T : Fragment> FragmentManager.findNavFragment(hostId: Int): T? {
    val navHostFragment = findFragmentById(hostId)
    return navHostFragment?.childFragmentManager?.fragments?.findLast {
        it is T
    } as T?
}
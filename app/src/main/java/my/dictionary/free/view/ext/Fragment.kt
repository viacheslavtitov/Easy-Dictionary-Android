package my.dictionary.free.view.ext

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import my.dictionary.free.R

fun Fragment.addMenuProvider(
    @MenuRes menuRes: Int,
    menuCreated: (menu: Menu) -> Unit,
    callbackMenuClicked: (id: Int) -> Boolean
) {
    val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(menuRes, menu)
            menu.iterator().forEach {
                it.setTint(requireContext(), R.color.white)
            }
            menuCreated(menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) = callbackMenuClicked(menuItem.itemId)

    }
    (requireActivity() as MenuHost).addMenuProvider(
        menuProvider,
        viewLifecycleOwner,
        Lifecycle.State.RESUMED
    )
}

fun Fragment.addMenuProvider(
    @MenuRes menuRes: Int,
    menuCreated: (menu: Menu, menuInflater: MenuInflater) -> Unit,
    callbackMenuClicked: (id: Int) -> Boolean
) {
    val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(menuRes, menu)
            menu.iterator().forEach {
                it.setTint(requireContext(), R.color.white)
            }
            menuCreated(menu, menuInflater)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) = callbackMenuClicked(menuItem.itemId)

    }
    (requireActivity() as MenuHost).addMenuProvider(
        menuProvider,
        viewLifecycleOwner,
        Lifecycle.State.RESUMED
    )
}
package org.easy.dictionary.app.view

import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.easy.dictionary.app.R

abstract class AbstractBaseFragment: Fragment() {

    private var errorSnackbar: Snackbar? = null

    private fun displayError(@StringRes errorMessageResId: Int, view: View) {
        val errorMessage = context?.getString(errorMessageResId) ?: ""
        displayError(errorMessage, view)
    }

    fun displayError(errorMessage: String?, view: View?) {
        if(view == null) return
        if(!errorMessage.isNullOrEmpty()) {
            if(errorSnackbar != null || errorSnackbar?.isShown == true) {
                errorSnackbar?.dismiss()
                errorSnackbar = null
            }
            errorSnackbar = Snackbar.make(
                view,
                errorMessage,
                Snackbar.LENGTH_SHORT
            )
            errorSnackbar?.show()
        }
    }

    fun displayInputDialog(title: String?, message: String?) {
        context?.let {ctx ->
            val builder = MaterialAlertDialogBuilder(ctx).setView(R.layout.fragment_dialog_input)
            builder.create().listView
        }
    }

}
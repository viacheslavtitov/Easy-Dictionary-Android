package my.dictionary.free.view

import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class AbstractBaseFragment: Fragment() {

    private var errorSnackbar: Snackbar? = null

    private fun displayError(@StringRes errorMessageResId: Int, view: View) {
        val errorMessage = context?.getString(errorMessageResId) ?: ""
        displayError(errorMessage, view)
    }

    fun displayError(errorMessage: String?, view: View) {
        if(!errorMessage.isNullOrEmpty()) {
            if(errorSnackbar != null || errorSnackbar!!.isShown) {
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

}
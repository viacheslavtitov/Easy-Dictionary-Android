package org.easydictionary.app.view

import androidx.appcompat.app.AppCompatActivity
import org.easydictionary.app.view.dialogs.ISimpleInfoDialogBuilder
import org.easydictionary.app.view.ext.findAndDismissDialog

abstract class AbstractBaseActivity: AppCompatActivity() {

    fun displayAlert(dialogBuilder: ISimpleInfoDialogBuilder, tag: String) {
        supportFragmentManager.findAndDismissDialog(tag)
        dialogBuilder.build().show(supportFragmentManager, tag)
    }

}
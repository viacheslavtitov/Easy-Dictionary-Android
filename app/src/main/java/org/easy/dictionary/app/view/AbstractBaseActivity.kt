package org.easy.dictionary.app.view

import androidx.appcompat.app.AppCompatActivity
import org.easy.dictionary.app.view.dialogs.ISimpleInfoDialogBuilder
import org.easy.dictionary.app.view.ext.findAndDismissDialog

abstract class AbstractBaseActivity: AppCompatActivity() {

    fun displayAlert(dialogBuilder: ISimpleInfoDialogBuilder, tag: String) {
        supportFragmentManager.findAndDismissDialog(tag)
        dialogBuilder.build().show(supportFragmentManager, tag)
    }

}
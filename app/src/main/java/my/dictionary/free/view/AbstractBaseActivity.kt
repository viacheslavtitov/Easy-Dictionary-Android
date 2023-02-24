package my.dictionary.free.view

import androidx.appcompat.app.AppCompatActivity
import my.dictionary.free.view.dialogs.ISimpleInfoDialogBuilder
import my.dictionary.free.view.ext.findAndDismissDialog

abstract class AbstractBaseActivity: AppCompatActivity() {

    fun displayAlert(dialogBuilder: ISimpleInfoDialogBuilder, tag: String) {
        supportFragmentManager.findAndDismissDialog(tag)
        dialogBuilder.build().show(supportFragmentManager, tag)
    }

}
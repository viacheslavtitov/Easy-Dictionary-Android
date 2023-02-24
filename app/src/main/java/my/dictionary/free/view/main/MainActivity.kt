package my.dictionary.free.view.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import my.dictionary.free.R
import my.dictionary.free.view.AbstractBaseActivity
import my.dictionary.free.view.ext.visibleSystemBars


class MainActivity : AbstractBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        visibleSystemBars(visible = true, type = WindowInsetsCompat.Type.statusBars())
        visibleSystemBars(visible = true, type = WindowInsetsCompat.Type.systemBars())

    }
}
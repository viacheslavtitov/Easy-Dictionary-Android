package org.easydictionary.app.view.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import org.easydictionary.app.R
import org.easydictionary.app.domain.viewmodels.splash.SplashViewModel
import org.easydictionary.app.view.AbstractBaseActivity
import org.easydictionary.app.view.ext.visibleSystemBars
import org.easydictionary.app.view.main.MainActivity

@AndroidEntryPoint
class SplashActivity : AbstractBaseActivity() {

    companion object {
        private val TAG = SplashActivity::class.simpleName
    }

    private var motionLayout: MotionLayout? = null
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        visibleSystemBars(visible = false, type = WindowInsetsCompat.Type.statusBars())
        visibleSystemBars(visible = false, type = WindowInsetsCompat.Type.systemBars())
        motionLayout = findViewById(R.id.motionLayout)
        signOrToMain()
    }

    private fun signOrToMain() {
        if (splashViewModel.isUserSignedIn()) {
            // already signed in
            launchMainActivity()
        } else {
            signIn()
        }
    }

    private fun signIn() {

    }

    private fun launchMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
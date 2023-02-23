package my.dictionary.free.view.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.*
import my.dictionary.free.R
import my.dictionary.free.view.ext.visibleSystemBars
import my.dictionary.free.view.main.MainActivity

class SplashActivity : AppCompatActivity() {

    private var motionLayout: MotionLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        visibleSystemBars(visible = false, type = WindowInsetsCompat.Type.statusBars())
        visibleSystemBars(visible = false, type = WindowInsetsCompat.Type.systemBars())
        motionLayout = findViewById(R.id.motionLayout)
    }

    override fun onResume() {
        super.onResume()
        runAnimation()
    }

    private fun runAnimation() {
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            withContext(Dispatchers.Main) {
                motionLayout?.apply {
                    setTransition(R.id.first, R.id.second)
                    transitionToEnd {
                        setTransition(R.id.second, R.id.third)
                        transitionToEnd {
                            setTransition(R.id.third, R.id.fourth)
                            transitionToEnd {
                                launchMainActivity()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
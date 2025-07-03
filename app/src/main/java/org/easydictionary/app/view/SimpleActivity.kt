package org.easydictionary.app.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import org.easydictionary.app.view.widget.global.EasyDictionaryTheme

class SimpleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyDictionaryTheme {
                Surface {
                    SimpleScreen()
                }
            }
        }
    }
}

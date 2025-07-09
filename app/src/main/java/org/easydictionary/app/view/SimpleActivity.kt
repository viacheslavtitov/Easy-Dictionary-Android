package org.easydictionary.app.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import org.easydictionary.app.domain.viewmodels.main.SimpleMainViewModel
import org.easydictionary.app.view.indicators.LoadingIndicatorCircle
import org.easydictionary.app.view.widget.global.EasyDictionaryTheme

class SimpleActivity : ComponentActivity() {

    private val viewModel: SimpleMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyDictionaryTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    SimpleScreen()

                    val loadingState by viewModel.loadingState
                    if (loadingState) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .zIndex(1f), // always in the top
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicatorCircle()
                        }
                    }
                }
            }
        }
    }
}

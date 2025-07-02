package org.easydictionary.app.view.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.easydictionary.app.domain.viewmodels.auth.SignInViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
//
//@Composable
//fun SignInScreen(viewModel: SignInViewModel = hiltViewModel()) {
//    val activity = LocalContext.current as ComponentActivity
//    val sharedViewModel: SharedMainViewModel = hiltViewModel(activity)
//    var text by remember { mutableStateOf("") }
//    TextField(
//        state = rememberTextFieldState("Hello\nWorld\nInvisible"),
//        onValueChange = { text = it },
//        lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2),
//        placeholder = { Text("") },
//        textStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
//        label = { Text("Enter text") },
//        modifier = Modifier.padding(20.dp)
//    )
//    Column(
//        modifier = Modifier.fillMaxSize().padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        TextField(
//            value = text,
//            onValueChange = { text = it },
//            label = { Text("Label") },
//            singleLine = true
//        )
//    }
//}
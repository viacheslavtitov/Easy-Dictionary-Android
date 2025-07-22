@file:OptIn(ExperimentalMaterial3Api::class)

package org.easydictionary.app.view.topbars

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.easydictionary.app.view.widget.global.Colors
import org.easydictionary.app.view.widget.global.LightColors

@Composable
fun TitleTopBar(
    title: String
) {
    TopAppBar(
        title = { TopBarTitle(title = title) },
        colors = topBarLightColors()
    )
}

@Composable
fun DrawerTitleTopBar(
    title: String,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    TopAppBar(
        title = { TopBarTitle(title = title) },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    if (drawerState.isClosed) {
                        drawerState.open()
                    } else {
                        drawerState.close()
                    }
                }
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        colors = topBarLightColors()
    )
}

@Composable
private fun TopBarTitle(title: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    val isDark = isSystemInDarkTheme()
    val titleColor =
        if (isDark)
            LightColors.Text_Main
        else
            LightColors.Text_Main
    Text(modifier = modifier, text = title, fontSize = 18.sp, color = titleColor)
}

@Composable
private fun topBarLightColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = Colors.White,
    scrolledContainerColor = Colors.White,
    navigationIconContentColor = LightColors.Outlined,
    titleContentColor = LightColors.Outlined,
    actionIconContentColor = LightColors.Main_Dark
)
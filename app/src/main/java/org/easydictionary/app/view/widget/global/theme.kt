package org.easydictionary.app.view.widget.global

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val LightColorScheme = lightColorScheme(
    primary = LightColors.Main,
    secondary = LightColors.Secondary,
    onSecondary = LightColors.Secondary_Light,
    background = LightColors.Secondary_Screen_Background,
    onPrimary = LightColors.Main_Dark,
    onSurface = LightColors.Outlined,
    surface = LightColors.Surface
)

@Composable
fun EasyDictionaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LightColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
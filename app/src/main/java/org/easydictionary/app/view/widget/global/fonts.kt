package org.easydictionary.app.view.widget.global

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontWeight
import org.easydictionary.app.R

val googleProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage  = "com.google.android.gms",
    certificates     = R.array.com_google_android_gms_fonts_certs
)

val notoSans = FontFamily(
    Font(googleFont = GoogleFont("Noto Sans"), fontProvider = googleProvider, weight = FontWeight.Bold)
)
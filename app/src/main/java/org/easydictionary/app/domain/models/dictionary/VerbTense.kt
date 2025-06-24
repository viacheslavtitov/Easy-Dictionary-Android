package org.easydictionary.app.domain.models.dictionary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VerbTense(
    val _id: String? = null,
    val name: String,
) : Parcelable {
}
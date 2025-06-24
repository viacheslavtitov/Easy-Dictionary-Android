package org.easy.dictionary.app.domain.models.filter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.easy.dictionary.app.domain.models.words.tags.Tag

@Parcelize
data class FilterModel(val tags: List<Tag>, val categories: List<Tag>, val types: List<Tag>) :
    Parcelable
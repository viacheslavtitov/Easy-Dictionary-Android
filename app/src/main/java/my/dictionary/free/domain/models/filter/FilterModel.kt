package my.dictionary.free.domain.models.filter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import my.dictionary.free.domain.models.words.tags.Tag

@Parcelize
data class FilterModel(val tags: List<Tag>, val categories: List<Tag>, val types: List<Tag>) :
    Parcelable
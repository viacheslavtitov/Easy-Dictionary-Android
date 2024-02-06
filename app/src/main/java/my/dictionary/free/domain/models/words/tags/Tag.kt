package my.dictionary.free.domain.models.words.tags

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class Tag(val tagName: String, val id: String): Parcelable
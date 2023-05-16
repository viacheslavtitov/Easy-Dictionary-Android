package my.dictionary.free.domain.models.language

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Language(@SerializedName("key") val key: String, @SerializedName("value") val value: String) :
    Parcelable

enum class LangType(type: Int) {
    FROM(0), TO(1)
}
package org.easydictionary.app.data.models.language

import com.google.gson.annotations.SerializedName

data class PhoneticsResponse(
    @SerializedName("pulmonic_consonants") val pulmonicConsonants: List<Phonetic> = emptyList(),
    @SerializedName("non_pulmonic_consonants") val nonPulmonicConsonants: List<Phonetic> = emptyList(),
    @SerializedName("other_consonant_symbols") val otherConsonantSymbols: List<Phonetic> = emptyList(),
    @SerializedName("vowels") val vowels: List<Phonetic> = emptyList(),
    @SerializedName("diacritics_combining") val diacriticsCombining: List<Phonetic> = emptyList(),
    @SerializedName("suprasegmentals") val suprasegmentals: List<Phonetic> = emptyList(),
    @SerializedName("tones_and_accents") val tonesAndAccents: List<Phonetic> = emptyList(),
) {
    fun toListDomain(): List<org.easydictionary.app.domain.models.language.Phonetic> {
        return buildList(
            pulmonicConsonants.size + nonPulmonicConsonants.size
                    + otherConsonantSymbols.size + vowels.size + diacriticsCombining.size
                    + suprasegmentals.size + tonesAndAccents.size
        ) {
            addAll(pulmonicConsonants.map { toDomain(it) })
            addAll(nonPulmonicConsonants.map { toDomain(it) })
            addAll(otherConsonantSymbols.map { toDomain(it) })
            addAll(vowels.map { toDomain(it) })
            addAll(diacriticsCombining.map { toDomain(it) })
            addAll(suprasegmentals.map { toDomain(it) })
            addAll(tonesAndAccents.map { toDomain(it) })
        }
    }
}

fun toDomain(phonetic: Phonetic): org.easydictionary.app.domain.models.language.Phonetic {
    return org.easydictionary.app.domain.models.language.Phonetic(
        symbol = phonetic.symbol,
        name = phonetic.name,
        unicode = phonetic.unicode
    )
}

data class Phonetic(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("unicode") val unicode: String,
)
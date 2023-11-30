package my.dictionary.free.domain.models.words

data class Word(
    val _id: String? = null,
    val dictionaryId: String,
    val original: String,
    val phonetic: String? = null,
    val translates: List<TranslateVariant>
) {
    companion object {
        fun empty() = Word(
            _id = null,
            dictionaryId = "",
            original = "",
            phonetic = null,
            translates = emptyList()
        )
    }
}

data class TranslateVariant(
    val _id: String? = null,
    val translate: String,
    val description: String? = null
)
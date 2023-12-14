package my.dictionary.free.domain.models.quiz

import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word

data class Quiz(
    val _id: String? = null,
    val userId: String,
    var dictionary: Dictionary? = null,
    val name: String,
    val timeInSeconds: Int,
    val words: MutableList<Word> = mutableListOf()
) {
    companion object {
        const val DEFAULT_QUIZE_TIME = 60
        fun empty(): Quiz = Quiz(
            _id = null,
            userId = "",
            dictionary = null,
            name = "",
            timeInSeconds = DEFAULT_QUIZE_TIME,
            words = mutableListOf()
        )
    }

    override fun toString(): String {
        return "id = $_id | userId = $userId | dictionary = ${dictionary?.dictionaryFrom} - ${dictionary?.dictionaryTo} | name = $name | timeInSeconds = $timeInSeconds | words = ${words.size}"
    }
}

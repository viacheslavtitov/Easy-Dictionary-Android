package org.easydictionary.app.domain.models.quiz

import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.models.words.Word

data class Quiz(
    val _id: String? = null,
    val userId: String,
    var dictionary: Dictionary? = null,
    val name: String,
    val timeInSeconds: Int,
    val reversed: Boolean = false,
    val hidePhonetic: Boolean = false,
    val showTags: Boolean = false,
    val showCategories: Boolean = false,
    val showTypes: Boolean = false,
    val words: MutableList<Word> = mutableListOf(),
    val quizWords: MutableList<QuizWords> = mutableListOf(),
    val histories: MutableList<QuizResult> = mutableListOf()
) {
    companion object {
        const val DEFAULT_QUIZE_TIME = 60
        fun empty(): Quiz = Quiz(
            _id = null,
            userId = "",
            dictionary = null,
            name = "",
            hidePhonetic = false,
            showTags = false,
            showCategories = false,
            showTypes = false,
            timeInSeconds = DEFAULT_QUIZE_TIME,
            words = mutableListOf()
        )
    }
}

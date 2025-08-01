package org.easydictionary.app.domain.models.navigation

import kotlinx.serialization.json.Json
import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.filter.FilterModel
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.quiz.Quiz
import org.easydictionary.app.domain.models.words.Word
import org.easydictionary.app.domain.models.words.variants.TranslationVariant

sealed class AppNavigation(val route: String) {
    object SplashScreen : AppNavigation("splash")
    object SignInScreen : AppNavigation("signin")
    object SignUpScreen : AppNavigation("signup")
    object HomeScreen : AppNavigation("home")
    object SettingsScreen : AppNavigation("settings")
    object LanguagesScreen : AppNavigation("languages/{langType}") {
        fun createRoute(langType: LangType) = "languages/${langType.type}"
    }

    object AddNewLanguageScreen : AppNavigation("languages/add")
    object DictionariesScreen : AppNavigation("dictionary")
    object AddUserDictionaryScreen : AppNavigation("dictionary/add")
    object EditDictionaryScreen : AppNavigation("dictionary/edit/{dictionary}") {
        fun createRoute(dictionary: DictionaryDetailShort) =
            "dictionary/edit/${Json.encodeToString(dictionary)}"
    }

    object AddUserQuizScreen : AppNavigation("quiz/add")

    object EditQuizScreenFromDetail : AppNavigation("quiz/edit/{quiz}") {
        fun createRoute(quiz: Quiz) = "quiz/edit/$quiz"
    }

    object EditQuizScreenFromQuizList : AppNavigation("quiz/edit/{quiz}") {
        fun createRoute(quiz: Quiz) = "quiz/edit/$quiz"
    }

    object UserQuizScreen : AppNavigation("quiz/{quiz}") {
        fun createRoute(quiz: Quiz) = "quiz/$quiz"
    }

    object RunQuizScreen : AppNavigation("quiz/run/{quiz}") {
        fun createRoute(quiz: Quiz) = "quiz/run/$quiz"
    }

    object DictionaryWordsScreen : AppNavigation("dictionary/words/{dictionary}") {
        fun createRoute(dictionary: Dictionary) = "dictionary/words/$dictionary"
    }

    object AddDictionaryWordScreen : AppNavigation("dictionary/words/add/{dictionaryId}") {
        fun createRoute(dictionaryId: String) = "dictionary/words/add/$dictionaryId"
    }

    object EditDictionaryWordScreen : AppNavigation("dictionary/words/edit/{word}") {
        fun createRoute(word: Word) = "dictionary/words/edit/$word"
    }

    object WordsMultiChooseScreen : AppNavigation("dictionary/words/multi/{dictionaryId}/{words}") {
        fun createRoute(dictionaryId: String, words: ArrayList<Word>?) =
            "dictionary/words/multi/$dictionaryId/$words"
    }

    object DictionaryChooseScreen : AppNavigation("dictionary/choose")
    object AddTranslationVariantsScreen : AppNavigation("translation/variant/add/{word}") {
        fun createRoute(word: String?) = "translation/variant/add/$word"
    }

    object AddWordTagsScreen : AppNavigation("word/tags/add/{word}/{dictionary}") {
        fun createRoute(word: Word?, dictionary: Dictionary) = "word/tags/add/$word/$dictionary"
    }

    object DictionaryFilterScreen : AppNavigation("dictionary/filter/{dictionary}/{filterModel}") {
        fun createRoute(dictionary: Dictionary, filterModel: FilterModel?) =
            "dictionary/filter/$dictionary/$filterModel"
    }

    object DictionaryMultiChooseFilterScreen :
        AppNavigation("dictionary/filter/choose/{dictionary}/{filterModel}") {
        fun createRoute(dictionary: Dictionary, filterModel: FilterModel?) =
            "dictionary/filter/choose/$dictionary/$filterModel"
    }

    object EditTranslationVariantsScreen :
        AppNavigation("translation/variant/edit/{dictionaryId}/{word}/{translation}") {
        fun createRoute(word: String?, dictionaryId: String, translation: TranslationVariant) =
            "translation/variant/edit/$dictionaryId/$word/$translation"
    }
}
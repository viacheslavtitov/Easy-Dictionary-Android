package org.easydictionary.app.domain.models.navigation

import kotlinx.serialization.json.Json
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.translation.ComposedTranslation
import org.easydictionary.app.domain.models.word.WordDetail

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

    object AddDictionaryWordScreen : AppNavigation("dictionary/words/{dictionary}") {
        fun createRoute(dictionary: DictionaryDetailShort) =
            "dictionary/words/${Json.encodeToString(dictionary)}"
    }

    object AddDictionaryWordTranslationsScreen :
        AppNavigation("dictionary/words/translation/add/{dictionaryId}") {
        fun createRoute(dictionaryId: Int) = "dictionary/words/translation/add/${dictionaryId}"
    }

    object EditDictionaryWordTranslationsScreen :
        AppNavigation("dictionary/words/translation/edit/{dictionaryId}/{translation}") {
        fun createRoute(translation: ComposedTranslation, dictionaryId: Int) =
            "dictionary/words/translation/edit/${dictionaryId}/${Json.encodeToString(translation)}"
    }

    object AddNewCategoryScreen : AppNavigation("dictionary/category/add")

    object EditDictionaryWordScreen : AppNavigation("{dictionary}/words/edit/{word}") {
        fun createRoute(dictionary: DictionaryDetailShort, word: WordDetail) =
        "${Json.encodeToString(dictionary)}/words/edit/${Json.encodeToString(word)}"
    }

    object UserQuizScreen : AppNavigation("quiz")
}
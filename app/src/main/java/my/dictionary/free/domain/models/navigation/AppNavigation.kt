package my.dictionary.free.domain.models.navigation

import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.language.LangType

sealed class AppNavigation()

class LanguagesScreen(val langType: LangType) : AppNavigation()

class AddUserDictionaryScreen() : AppNavigation()

class DictionaryWordsScreen(val dictionary: Dictionary) : AppNavigation()
class AddDictionaryWordScreen(val dictionaryId: String) : AppNavigation()
class AddTranslationVariantsScreen(val word: String?) : AppNavigation()
package my.dictionary.free.domain.models.navigation

import my.dictionary.free.domain.models.language.LangType

sealed class AppNavigation()

class LanguagesScreen(val langType: LangType) : AppNavigation()

class AddUserDictionaryScreen() : AppNavigation()
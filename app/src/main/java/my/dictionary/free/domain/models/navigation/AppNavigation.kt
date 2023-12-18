package my.dictionary.free.domain.models.navigation

import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.language.LangType
import my.dictionary.free.domain.models.quiz.Quiz

sealed class AppNavigation()

class LanguagesScreen(val langType: LangType) : AppNavigation()

class AddUserDictionaryScreen() : AppNavigation()
class AddUserQuizScreen() : AppNavigation()

class UserQuizScreen(val quiz: Quiz) : AppNavigation()
class RunQuizScreen(val quiz: Quiz) : AppNavigation()
class DictionaryWordsScreen(val dictionary: Dictionary) : AppNavigation()
class AddDictionaryWordScreen(val dictionaryId: String) : AppNavigation()
class WordsMultiChooseScreen(val dictionaryId: String) : AppNavigation()
class DictionaryChooseScreen() : AppNavigation()
class AddTranslationVariantsScreen(val word: String?) : AppNavigation()
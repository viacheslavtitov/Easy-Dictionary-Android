package my.dictionary.free.domain.models.navigation

import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.language.LangType
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.variants.TranslationVariant

sealed class AppNavigation()

class HomeScreen() : AppNavigation()
class LanguagesScreen(val langType: LangType) : AppNavigation()

class AddUserDictionaryScreen() : AppNavigation()
class EditDictionaryScreen(val dictionary: Dictionary) : AppNavigation()
class AddUserQuizScreen() : AppNavigation()

class EditQuizScreenFromDetail(val quiz: Quiz) : AppNavigation()
class EditQuizScreenFromQuizList(val quiz: Quiz) : AppNavigation()
class UserQuizScreen(val quiz: Quiz) : AppNavigation()
class RunQuizScreen(val quiz: Quiz) : AppNavigation()
class DictionaryWordsScreen(val dictionary: Dictionary) : AppNavigation()
class AddDictionaryWordScreen(val dictionaryId: String) : AppNavigation()
class EditDictionaryWordScreen(val word: Word) : AppNavigation()
class WordsMultiChooseScreen(val dictionaryId: String, val words: ArrayList<Word>?) : AppNavigation()
class DictionaryChooseScreen() : AppNavigation()
class AddTranslationVariantsScreen(val word: String?) : AppNavigation()
class AddWordTagsScreen(val word: String?, val dictionary: Dictionary) : AppNavigation()
class EditTranslationVariantsScreen(val word: String?, val dictionaryId: String, val translation: TranslationVariant) : AppNavigation()
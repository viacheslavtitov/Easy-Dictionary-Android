package my.dictionary.free.domain.viewmodels.user.dictionary.words.add

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationsUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import javax.inject.Inject

@HiltViewModel
class AddDictionaryWordViewModel @Inject constructor(
    private val wordsUseCase: WordsUseCase,
    private val getCreateDictionaryUseCase: GetCreateDictionaryUseCase,
    private val getCreateTranslationsUseCase: GetCreateTranslationsUseCase,
    private val getCreateTranslationCategoriesUseCase: GetCreateTranslationCategoriesUseCase
) : ViewModel() {

    companion object {
        private val TAG = AddDictionaryWordViewModel::class.simpleName
    }

    private val _phoneticsUIState: MutableStateFlow<List<String>> =
        MutableStateFlow(emptyList())
    val phoneticsUIState: StateFlow<List<String>> = _phoneticsUIState.asStateFlow()

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _clearTranslationsUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val clearTranslationsUIState: StateFlow<Boolean> = _clearTranslationsUIState.asStateFlow()

    private val _displayErrorUIState = Channel<String>()
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    private val _validateWord = Channel<String>()
    val validateWord: StateFlow<String> = _validateWord.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    private val _successCreateWordUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val successCreateWordUIState: StateFlow<Boolean> =
        _successCreateWordUIState.asStateFlow()

    //edit flows
    private val _nameUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val nameUIState: StateFlow<String> = _nameUIState.asStateFlow()

    private val _typeUIState: MutableStateFlow<Int> =
        MutableStateFlow(0)
    val typeUIState: StateFlow<Int> = _typeUIState.asStateFlow()

    private val _phoneticUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val phoneticUIState: StateFlow<String> = _phoneticUIState.asStateFlow()

    private val _translationVariantsUIState = Channel<TranslationVariant>()
    val translationVariantsUIState = _translationVariantsUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TranslationVariant.empty())

    private var dictionary: Dictionary? = null
    private var editWord: Word? = null
    private var notSavingTranslations = mutableListOf<TranslationVariant>()
    private var tempDeletedTranslations = mutableListOf<TranslationVariant>()

    fun loadData(context: Context?, dictionaryId: String?, word: Word?) {
        if (context == null) return
        if (dictionaryId.isNullOrEmpty()) {
            viewModelScope.launch {
                _displayErrorUIState.send(context.getString(R.string.error_load_data))
            }
            return
        }
        this.editWord = word
        viewModelScope.launch {
            getCreateDictionaryUseCase.getDictionaryById(context, dictionaryId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _displayErrorUIState.send(
                        it.message ?: context.getString(R.string.unknown_error)
                    )
                }
                .onStart {
                    Log.d(TAG, "onStart")
                    _loadingUIState.value = true
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingUIState.value = false
                }
                .collect {
                    Log.d(
                        TAG,
                        "collect dictionary ${it.dictionaryFrom.lang} - ${it.dictionaryTo.lang}"
                    )
                    dictionary = it
                }
        }.invokeOnCompletion {
            loadPhonetic(context, dictionary)
            if (isEditMode()) {
                loadWordData()
            }
        }
    }

    private fun loadPhonetic(context: Context, dictionary: Dictionary?) {
        dictionary?.let { dict ->
            viewModelScope.launch {
                val phonetics = wordsUseCase.getPhonetics(context, dict.dictionaryFrom.lang)
                Log.d(TAG, "found phonetics count ${phonetics.size}")
                _phoneticsUIState.value = phonetics
            }
        }
    }

    private fun loadWordData() {
        editWord?.let { word ->
            viewModelScope.launch {
                _loadingUIState.value = true
                _clearTranslationsUIState.value = true
                _nameUIState.value = word.original
                _typeUIState.value = word.type
                word.phonetic?.let {
                    _phoneticUIState.value = it
                }
                for (translate in word.translates) {
                    if (isTranslationExistInTempDeletedList(translate)) continue
                    if (translate.categoryId != null) {
                        getCreateTranslationCategoriesUseCase.getCategoryById(
                            translate.categoryId
                        )
                            .onStart {
                                Log.d(TAG, "load category for ${translate.translation} onStart")
                            }
                            .onCompletion {
                                Log.d(
                                    TAG,
                                    "load category for ${translate.translation} onCompletion"
                                )
                            }
                            .collect {
                                translate.category = it
                                _translationVariantsUIState.send(translate)
                            }
                    } else {
                        _translationVariantsUIState.send(translate)
                    }
                }
                notSavingTranslations.forEach {
                    _translationVariantsUIState.send(it)
                }
                _clearTranslationsUIState.value = false
                _loadingUIState.value = false
            }
        }
    }

    private fun isTranslationExistInTempDeletedList(translationVariant: TranslationVariant): Boolean {
        return tempDeletedTranslations.find { translationVariant._id == it._id } != null
    }

    fun validate(
        context: Context?,
        word: String?,
        translations: List<TranslationVariant>
    ): Boolean {
        if (context == null) return false
        if (word.isNullOrEmpty()) {
            viewModelScope.launch {
                _validateWord.send(context.getString(R.string.field_required))
            }
            return false
        }
        if (translations.isEmpty()) {
            viewModelScope.launch {
                _displayErrorUIState.send(context.getString(R.string.error_empty_translations))
            }
            return false
        }
        return true
    }

    fun save(
        context: Context?,
        wordName: String?,
        typePosition: Int,
        translations: List<TranslationVariant>,
        phonetic: String?
    ) {
        if (context == null) return
        if (wordName.isNullOrEmpty()) return
        if (dictionary == null || dictionary?._id.isNullOrEmpty()) return
        viewModelScope.launch {
            _loadingUIState.value = true
            _successCreateWordUIState.value = false
            if (isEditMode()) {
                val entity = Word(
                    _id = editWord!!._id!!,
                    dictionaryId = dictionary?._id!!,
                    original = wordName.trim(),
                    type = typePosition,
                    phonetic = phonetic,
                    translates = translations,
                    tags = emptyList()
                )
                val wordResult = wordsUseCase.updateWord(entity)
                if (!wordResult) {
                    val error = context.getString(R.string.error_update_word)
                    _displayErrorUIState.send(error)
                    _loadingUIState.value = false
                } else {
                    val shouldDeleteTranslationsIds = arrayListOf<String>()
                    editWord?.translates?.forEach { translate ->
                        if (translations.find { it._id == translate._id } == null) {
                            shouldDeleteTranslationsIds.add(translate._id!!)
                        }
                    }
                    if (shouldDeleteTranslationsIds.isNotEmpty()) {
                        val resultDeleteTranslations =
                            getCreateTranslationsUseCase.deleteTranslationsFromWord(
                                shouldDeleteTranslationsIds,
                                dictionary!!._id!!,
                                editWord!!._id!!
                            )
                        if (!resultDeleteTranslations.first) {
                            val error = resultDeleteTranslations.second
                                ?: context.getString(R.string.error_create_quiz)
                            _displayErrorUIState.send(error)
                        }
                    }
                    var translationUpdatedSuccess = true
                    for (tr in translations) {
                        if (tr._id?.isNullOrEmpty() == false) continue
                        val translationResult = getCreateTranslationsUseCase.createTranslation(
                            tr.copyWithNewWordId(editWord!!._id!!), dictionary!!._id!!
                        )
                        if (!translationResult.first) {
                            val error =
                                translationResult.second
                                    ?: context.getString(R.string.error_create_word)
                            _displayErrorUIState.send(error)
                            translationUpdatedSuccess = false
                            break
                        }
                    }
                    _loadingUIState.value = false
                    _successCreateWordUIState.value = translationUpdatedSuccess
                }
            } else {
                val dictionaryId = dictionary?._id!!
                val entity = Word(
                    dictionaryId = dictionaryId,
                    original = wordName.trim(),
                    type = typePosition,
                    phonetic = phonetic,
                    translates = translations,
                    tags = emptyList()
                )
                val wordResult = wordsUseCase.createWord(entity)
                if (!wordResult.first) {
                    val error = wordResult.second ?: context.getString(R.string.error_create_word)
                    _displayErrorUIState.send(error)
                    _loadingUIState.value = false
                } else {
                    var translationCreatedSuccess = true
                    for (tr in translations) {
                        val translationResult = getCreateTranslationsUseCase.createTranslation(
                            tr.copyWithNewWordId(wordResult.third ?: ""), dictionaryId
                        )
                        if (!translationResult.first) {
                            val error =
                                translationResult.second
                                    ?: context.getString(R.string.error_create_word)
                            _displayErrorUIState.send(error)
                            wordsUseCase.deleteWord(entity)
                            translationCreatedSuccess = false
                            break
                        }
                    }
                    _loadingUIState.value = false
                    _successCreateWordUIState.value = translationCreatedSuccess
                }
            }
        }
    }

    fun isEditMode() = editWord != null

    fun addTranslation(translationVariant: TranslationVariant) {
        val existIndex = notSavingTranslations.indexOfFirst {
            it.translation == translationVariant.translation
        }
        if (existIndex >= 0) {
            notSavingTranslations[existIndex] = translationVariant
        } else {
            notSavingTranslations.add(translationVariant)
        }
    }

    fun deleteTranslation(translationVariant: TranslationVariant) {
        if (translationVariant._id != null) {
            tempDeletedTranslations.add(translationVariant)
        } else {
            val existIndex = notSavingTranslations.indexOfFirst {
                it.translation == translationVariant.translation
            }
            if (existIndex >= 0) {
                notSavingTranslations.removeAt(existIndex)
            }
        }
    }

    fun getDictionary() = dictionary
    fun getEditedWord() = editWord

}
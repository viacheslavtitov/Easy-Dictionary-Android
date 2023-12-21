package my.dictionary.free.domain.viewmodels.user.dictionary.words.add

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
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

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    val validateWord: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val _successCreateWordUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val successCreateWordUIState: StateFlow<Boolean> =
        _successCreateWordUIState.asStateFlow()

    //edit flows
    private val _nameUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val nameUIState: StateFlow<String> = _nameUIState.asStateFlow()

    private val _phoneticUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val phoneticUIState: StateFlow<String> = _phoneticUIState.asStateFlow()

    val translationVariantsUIState: MutableSharedFlow<TranslationVariant> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var dictionary: Dictionary? = null
    private var editWord: Word? = null

    fun loadData(context: Context?, dictionaryId: String?, word: Word?) {
        if (context == null) return
        if (dictionaryId.isNullOrEmpty()) {
            _displayErrorUIState.value = context.getString(R.string.error_load_data)
            return
        }
        this.editWord = word
        viewModelScope.launch {
            getCreateDictionaryUseCase.getDictionaryById(context, dictionaryId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _displayErrorUIState.value =
                        it.message ?: context.getString(R.string.unknown_error)
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
                _nameUIState.value = word.original
                word.phonetic?.let {
                    _phoneticUIState.value = it
                }
                word.translates.forEach { translate ->
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
                                translationVariantsUIState.tryEmit(translate)
                            }
                    } else {
                        translationVariantsUIState.tryEmit(translate)
                    }
                }
                _loadingUIState.value = false
            }
        }
    }

    fun validate(
        context: Context?,
        word: String?,
        translations: List<TranslationVariant>
    ): Boolean {
        if (context == null) return false
        if (word.isNullOrEmpty()) {
            validateWord.tryEmit(context.getString(R.string.field_required))
            return false
        }
        if (translations.isEmpty()) {
            _displayErrorUIState.value = context.getString(R.string.error_empty_translations)
            return false
        }
        return true
    }

    fun save(
        context: Context?,
        wordName: String?,
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
                    phonetic = phonetic,
                    translates = translations
                )
                val wordResult = wordsUseCase.updateWord(entity)
                if (!wordResult) {
                    val error = context.getString(R.string.error_update_word)
                    _displayErrorUIState.value = error
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
                            _displayErrorUIState.value = error
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
                            _displayErrorUIState.value = error
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
                    phonetic = phonetic,
                    translates = translations
                )
                val wordResult = wordsUseCase.createWord(entity)
                if (!wordResult.first) {
                    val error = wordResult.second ?: context.getString(R.string.error_create_word)
                    _displayErrorUIState.value = error
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
                            _displayErrorUIState.value = error
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

}
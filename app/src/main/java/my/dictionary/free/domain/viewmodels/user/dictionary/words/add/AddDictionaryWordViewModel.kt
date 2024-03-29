package my.dictionary.free.domain.viewmodels.user.dictionary.words.add

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.tags.WordTag
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.models.words.verb_tense.WordVerbTense
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationsUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import my.dictionary.free.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class AddDictionaryWordViewModel @Inject constructor(
    private val wordsUseCase: WordsUseCase,
    private val getCreateDictionaryUseCase: GetCreateDictionaryUseCase,
    private val getCreateTranslationsUseCase: GetCreateTranslationsUseCase,
    private val getCreateTranslationCategoriesUseCase: GetCreateTranslationCategoriesUseCase,
    private val uiStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private val TAG = AddDictionaryWordViewModel::class.simpleName
        private const val KEY_STATE_TYPE = "type"
        private const val KEY_STATE_VERB_TENSES = "verb_tenses"
    }

    private val _validateWord = Channel<String>()
    val validateWord: StateFlow<String> = _validateWord.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    //edit flows
    private val _nameUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val nameUIState: StateFlow<String> = _nameUIState.asStateFlow()

    val typeSavedUIState: StateFlow<Int> = uiStateHandle.getStateFlow(KEY_STATE_TYPE, 0)

    val tensesSavedUIState: StateFlow<List<Pair<String, String>>> =
        uiStateHandle.getStateFlow(KEY_STATE_VERB_TENSES, emptyList())

    private val _phoneticUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val phoneticUIState: StateFlow<String> = _phoneticUIState.asStateFlow()

    private var dictionary: Dictionary? = null
    private var editWord: Word? = null
    private var notSavingTranslations = mutableListOf<TranslationVariant>()
    private var tempDeletedTranslations = mutableListOf<TranslationVariant>()

    fun loadData(context: Context?, dictionaryId: String?, word: Word?) =
        flow<FetchDataState<List<String>>> {
            if (context == null) {
                return@flow
            }
            if (dictionaryId.isNullOrEmpty()) {
                emit(FetchDataState.ErrorStateString(context.getString(R.string.error_load_data)))
                return@flow
            }
            Log.d(TAG, "loadData($dictionaryId, ${word?.original})")
            editWord = word
            emit(FetchDataState.StartLoadingState)
            getCreateDictionaryUseCase.getDictionaryById(context, dictionaryId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    emit(FetchDataState.ErrorState(it))
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    emit(FetchDataState.FinishLoadingState)
                }
                .map {
                    Log.d(
                        TAG,
                        "collect dictionary ${it.dictionaryFrom.lang} - ${it.dictionaryTo.lang}"
                    )
                    dictionary = it
                    return@map loadPhonetic(context, dictionary)
                }
                .collect {
                    emit(it.first())
                }
        }

    private fun loadPhonetic(context: Context, dictionary: Dictionary?) =
        flow<FetchDataState<List<String>>> {
            if (dictionary != null) {
                val phonetics =
                    wordsUseCase.getPhonetics(context, dictionary!!.dictionaryFrom.lang)
                Log.d(TAG, "found phonetics count ${phonetics.size}")
                emit(FetchDataState.DataState(phonetics))
            } else {
                emit(FetchDataState.DataState(emptyList()))
            }
        }

    fun loadWordData() = flow<FetchDataState<TranslationVariant>> {
        Log.d(TAG, "not saving translations exist ${notSavingTranslations.size}")
        notSavingTranslations.forEach {
            emit(FetchDataState.DataState(it))
        }
        if (editWord == null) {
            return@flow
        }
        editWord?.phonetic?.let {
            _phoneticUIState.value = it
        }
        _nameUIState.value = editWord!!.original
        saveType(editWord!!.type)
        saveTenses(editWord?.tenses?.map { Pair(it.tenseId, it.value) })
        emit(FetchDataState.StartLoadingState)
        for (translate in editWord!!.translates) {
            if (isTranslationExistInTempDeletedList(translate)) continue
            if (translate.categoryId != null) {
                Log.d(TAG, "load translation category by id ${translate.categoryId}")
                getCreateTranslationCategoriesUseCase.getCategoryById(
                    translate.categoryId
                )
                    .onCompletion {
                        Log.d(
                            TAG,
                            "load category for ${translate.translation} onCompletion"
                        )
                        emit(FetchDataState.FinishLoadingState)
                    }
                    .collect {
                        translate.category = it
                        emit(FetchDataState.DataState(translate))
                    }
            } else {
                emit(FetchDataState.FinishLoadingState)
                emit(FetchDataState.DataState(translate))
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
    ) = flow<FetchDataState<Boolean>> {
        if (context == null) return@flow
        if (word.isNullOrEmpty()) {
            _validateWord.send(context.getString(R.string.field_required))
            emit(FetchDataState.DataState(false))
            return@flow
        }
        if (translations.isEmpty()) {
            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_empty_translations)))
            emit(FetchDataState.DataState(false))
            return@flow
        }
        emit(FetchDataState.DataState(true))
    }

    fun save(
        context: Context?,
        wordName: String?,
        typePosition: Int,
        translations: List<TranslationVariant>,
        phonetic: String?,
        tags: List<WordTag>,
        tenses: List<Pair<String, String>>
    ) = flow<FetchDataState<Boolean>> {
        if (context == null) return@flow
        if (wordName.isNullOrEmpty()) return@flow
        if (dictionary == null || dictionary?._id.isNullOrEmpty()) return@flow
        emit(FetchDataState.StartLoadingState)
        if (isEditMode()) {
            val entity = Word(
                _id = editWord!!._id!!,
                dictionaryId = dictionary?._id!!,
                original = wordName.trim(),
                type = typePosition,
                phonetic = phonetic,
                translates = translations,
                tags = arrayListOf(),
                tenses = arrayListOf()
            )
            val wordResult = wordsUseCase.updateWord(entity)
            if (!wordResult) {
                val error = context.getString(R.string.error_update_word)
                emit(FetchDataState.ErrorStateString(error))
                emit(FetchDataState.FinishLoadingState)
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
                        emit(FetchDataState.ErrorStateString(error))
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
                        emit(FetchDataState.ErrorStateString(error))
                        translationUpdatedSuccess = false
                        break
                    }
                }
                if (tags.isNotEmpty()) {
                    Log.d(TAG, "add tags ${tags.size}")
                    val addTagsResult =
                        wordsUseCase.addTagsToWord(dictionary!!._id!!, tags, editWord!!._id!!)
                    Log.d(TAG, "tags added result $addTagsResult")
                }
                editWord?.tenses?.forEach {
                    Log.d(TAG, "remove verb tense ${it.value}")
                    val result =
                        wordsUseCase.deleteVerbTense(
                            dictionary!!._id!!,
                            editWord!!._id!!,
                            it._id ?: ""
                        )
                    Log.d(TAG, "remove verb tense result $result")
                }
                tenses.forEach {
                    Log.d(TAG, "add verb tense ${it.first} ${it.second}")
                    val result =
                        wordsUseCase.addTensesToWord(
                            dictionary!!._id!!,
                            editWord!!._id!!,
                            it.first,
                            it.second
                        )
                    Log.d(TAG, "add verb tense result $result")
                }
                emit(FetchDataState.FinishLoadingState)
                emit(FetchDataState.DataState(translationUpdatedSuccess))
            }
        } else {
            val dictionaryId = dictionary?._id!!
            val entity = Word(
                dictionaryId = dictionaryId,
                original = wordName.trim(),
                type = typePosition,
                phonetic = phonetic,
                translates = translations,
                tags = arrayListOf(),
                tenses = arrayListOf()
            )
            val wordResult = wordsUseCase.createWord(entity)
            if (!wordResult.first) {
                val error = wordResult.second ?: context.getString(R.string.error_create_word)
                emit(FetchDataState.ErrorStateString(error))
                emit(FetchDataState.FinishLoadingState)
            } else {
                val convertedTenses = arrayListOf<WordVerbTense>()
                tenses.forEach {
                    val tenseId = it.first
                    val value = it.second
                    convertedTenses.add(
                        WordVerbTense(
                            _id = null,
                            tenseId = tenseId,
                            wordId = wordResult.third ?: "",
                            value = value
                        )
                    )
                }
                var translationCreatedSuccess = true
                for (tr in translations) {
                    val translationResult = getCreateTranslationsUseCase.createTranslation(
                        tr.copyWithNewWordId(wordResult.third ?: ""), dictionaryId
                    )
                    if (!translationResult.first) {
                        val error =
                            translationResult.second
                                ?: context.getString(R.string.error_create_word)
                        emit(FetchDataState.ErrorStateString(error))
                        wordsUseCase.deleteWord(entity)
                        translationCreatedSuccess = false
                        break
                    }
                }
                if (tags.isNotEmpty()) {
                    Log.d(TAG, "add tags ${tags.size}")
                    val addTagsResult =
                        wordsUseCase.addTagsToWord(dictionaryId, tags, wordResult.third ?: "")
                    Log.d(TAG, "tags added result $addTagsResult")
                }
                if (convertedTenses.isNotEmpty()) {
                    Log.d(TAG, "add tenses ${convertedTenses.size}")
                    convertedTenses.forEach {
                        val addTensesResult =
                            wordsUseCase.addTensesToWord(
                                dictionaryId,
                                it.wordId,
                                it.tenseId,
                                it.value
                            )
                        Log.d(
                            TAG,
                            "tense {${it.value} added result ${addTensesResult.first} ${addTensesResult.second}"
                        )
                    }
                }
                emit(FetchDataState.FinishLoadingState)
                emit(FetchDataState.DataState(translationCreatedSuccess))
            }
        }
    }

    fun isEditMode() = editWord != null

    fun addTranslation(translationVariant: TranslationVariant) {
        var existIndex = notSavingTranslations.indexOfFirst {
            it.translation == translationVariant.translation
        }
        if (existIndex >= 0) {
            notSavingTranslations[existIndex] = translationVariant
        } else {
            notSavingTranslations.add(translationVariant)
        }
        if (translationVariant._id != null && editWord != null) {
            existIndex = editWord!!.translates.indexOfFirst {
                it._id == translationVariant._id
            }
            if (existIndex >= 0) {
                val updatedTranslation = editWord!!.translates.toMutableList()
                updatedTranslation[existIndex] = translationVariant
                editWord = Word(
                    _id = editWord!!._id,
                    dictionaryId = editWord!!.dictionaryId,
                    original = editWord!!.original,
                    type = editWord!!.type,
                    phonetic = editWord!!.phonetic,
                    translates = updatedTranslation,
                    tags = editWord!!.tags,
                    tenses = editWord!!.tenses
                )
            }
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

    fun saveType(value: Int?) {
        uiStateHandle[KEY_STATE_TYPE] = value
//        getEditedWord()?.let {
//            editWord = Word(
//                _id = it._id,
//                dictionaryId = it.dictionaryId,
//                type = value ?: 0,
//                original = it.original,
//                phonetic = it.phonetic,
//                translates = it.translates,
//                tags = it.tags,
//                tenses = it.tenses
//            )
//        }
    }

    fun saveTenses(value: List<Pair<String, String>>?) {
        uiStateHandle[KEY_STATE_VERB_TENSES] = value
        getEditedWord()?.let { word ->
            word.tenses.clear()
            value?.forEach {
                word.tenses.add(
                    WordVerbTense(
                        _id = null,
                        tenseId = it.first,
                        wordId = word._id ?: "",
                        value = it.second,
                    )
                )
            }
        }
    }

}
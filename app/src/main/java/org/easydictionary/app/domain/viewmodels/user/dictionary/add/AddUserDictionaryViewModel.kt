package org.easydictionary.app.domain.viewmodels.user.dictionary.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.domain.usecases.dictionary.CreateDictionaryParams
import org.easydictionary.app.domain.usecases.dictionary.CreateDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.DeleteDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.UpdateDictionaryParams
import org.easydictionary.app.domain.usecases.dictionary.UpdateDictionaryUseCase
import org.easydictionary.app.domain.usecases.languages.AddUserLanguageParams
import org.easydictionary.app.domain.usecases.languages.AddUserLanguageUseCase
import org.easydictionary.app.domain.usecases.word.GetAllWordsForDictionaryParams
import org.easydictionary.app.domain.usecases.word.GetAllWordsForDictionaryUseCase
import org.easydictionary.app.domain.usecases.word.SearchWordsForDictionaryParams
import org.easydictionary.app.domain.usecases.word.SearchWordsForDictionaryUseCase
import javax.inject.Inject

@HiltViewModel
class AddUserDictionaryViewModel @Inject constructor(
    private val createDictionaryUseCase: CreateDictionaryUseCase,
    private val addUserLanguageUseCase: AddUserLanguageUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase,
    private val updateDictionaryUseCase: UpdateDictionaryUseCase,
    private val getAllWordsForDictionaryUseCase: GetAllWordsForDictionaryUseCase,
    private val searchWordsForDictionaryUseCase: SearchWordsForDictionaryUseCase
) : ViewModel() {

    companion object {
        private val TAG = AddUserDictionaryViewModel::class.simpleName
        private const val WORDS_PAGE_SIZE = 20
    }

    private var latestWordsPagId: Int = 0
    private var latestSearchWordsPagId: Int = 0
    private var editDictionary: DictionaryDetailShort? = null
    private val _selectedLanguageFrom = MutableStateFlow<Language?>(null)
    val selectedLanguageFrom: StateFlow<Language?> = _selectedLanguageFrom.asStateFlow()
    private val _selectedLanguageTo = MutableStateFlow<Language?>(null)
    val selectedLanguageTo: StateFlow<Language?> = _selectedLanguageTo.asStateFlow()
    private val _loadingDataUI = MutableStateFlow<Boolean>(false)
    val loadingDataUI: StateFlow<Boolean> = _loadingDataUI.asStateFlow()
    private val _dialect = MutableStateFlow<String>("")
    val dialect: StateFlow<String> = _dialect.asStateFlow()
    private val _errorUI = MutableStateFlow<String>("")
    val errorUI: StateFlow<String> = _errorUI.asStateFlow()
    private val _dictionaryCreated = MutableSharedFlow<Boolean>()
    val dictionaryCreated: SharedFlow<Boolean> = _dictionaryCreated
    private val _words = MutableStateFlow<List<WordDetail>>(emptyList())
    val words: StateFlow<List<WordDetail>> = _words.asStateFlow()
    private var hasMore = true
    private var latestFetchType: FetchWordsType = FetchWordsType.All
    private val lockLoadWords = kotlinx.coroutines.sync.Semaphore(1, acquiredPermits = 0)

    fun setLanguage(langType: LangType, json: String) {
        val language: Language = Json.decodeFromString(json)
        when (langType) {
            LangType.FROM -> {
                _selectedLanguageFrom.value = language
            }

            LangType.TO -> {
                _selectedLanguageTo.value = language
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun createDictionary(dialectValue: String? = null) {
        val languageFrom =
            selectedLanguageFrom.value ?: throw DictionaryValidationException.LanguageFromException
        val languageTo =
            selectedLanguageTo.value ?: throw DictionaryValidationException.LanguageToException
        Log.d(TAG, "createDictionary($dialectValue)")
        _loadingDataUI.value = true
        viewModelScope.launch {
            combine(
                addUserLanguageUseCase(
                    AddUserLanguageParams(languageFrom.code, languageFrom.name)
                ),
                addUserLanguageUseCase(AddUserLanguageParams(languageTo.code, languageTo.name))
            ) { langFrom, langTo ->
                langFrom to langTo
            }.flatMapLatest { langs ->
                if (langs.first is DomainResult.Success && langs.second is DomainResult.Success)
                    return@flatMapLatest createDictionaryUseCase(
                        CreateDictionaryParams(
                            dialectValue,
                            (langs.first as DomainResult.Success<Language>).data.id,
                            (langs.second as DomainResult.Success<Language>).data.id
                        )
                    ) else {
                    throw IllegalStateException("Languages wasn't created")
                }
            }.catch {
                Log.d(TAG, "catch ${it.message}")
                _errorUI.value = it.message ?: "Error"
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _loadingDataUI.value = false
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        Log.d(TAG, "Dictionary created")
                        _dictionaryCreated.emit(true)
                    }

                    is DomainResult.Error -> _errorUI.value = result.message
                }
            }
        }
    }

    fun updateDictionary(dialectValue: String? = null) {
        if (!isEditMode()) return
        Log.d(TAG, "updateDictionary $dialectValue")
        _loadingDataUI.value = true
        viewModelScope.launch {
            updateDictionaryUseCase(
                UpdateDictionaryParams(
                    id = editDictionary!!.id,
                    dialect = dialectValue
                )
            )
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _errorUI.value = it.message ?: "Error"
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "Dictionary ${editDictionary?.id} updated")
                            _dictionaryCreated.emit(true)
                        }

                        is DomainResult.Error -> _errorUI.value = result.message
                    }
                }
        }
    }

    fun delete() {
        if (!isEditMode()) return
        val deleteId = editDictionary?.id ?: return
        _loadingDataUI.value = true
        viewModelScope.launch {
            deleteDictionaryUseCase.invoke(deleteId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _errorUI.value = it.message ?: "Error"
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "Dictionary $deleteId deleted")
                            _dictionaryCreated.emit(true)
                        }

                        is DomainResult.Error -> _errorUI.value = result.message
                    }
                }
        }
    }

    fun isEditMode() = editDictionary != null

    fun setEditMode(dictionaryDetailShort: DictionaryDetailShort?) {
        editDictionary = dictionaryDetailShort
        _selectedLanguageFrom.value = editDictionary?.langFrom
        _selectedLanguageTo.value = editDictionary?.langTo
        _dialect.value = editDictionary?.dialect ?: ""
    }

    fun loadWords() {
        Log.d(TAG, "loadWords: hasMore = $hasMore | latestFetchType = ${latestFetchType.name}")
        if (!hasMore && latestFetchType == FetchWordsType.All) {
            Log.e(TAG, "loadWords blocked because hasMore is false or latestFetchType is ALL")
            return
        }
        val dictionaryId = editDictionary?.id
        if (dictionaryId == null) {
            Log.e(TAG, "dictionaryId is null")
            return
        }
        if (!lockLoadWords.tryAcquire()) {
            Log.e(TAG, "loadWords blocked because it's already running")
            return
        }
        latestFetchType = FetchWordsType.All
        Log.d(TAG, "loadWords($latestWordsPagId)")
        _loadingDataUI.value = true
        latestSearchWordsPagId = 0
        viewModelScope.launch {
            getAllWordsForDictionaryUseCase(
                GetAllWordsForDictionaryParams(
                    lastPageId = latestWordsPagId,
                    pageSize = WORDS_PAGE_SIZE,
                    dictionaryId = dictionaryId
                )
            ).catch {
                Log.d(TAG, "catch ${it.message}")
                _errorUI.value = it.message ?: "Error"
            }.onCompletion {
                Log.d(TAG, "loadWords onCompletion")
                _loadingDataUI.value = false
                lockLoadWords.release()
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        Log.d(TAG, "Words downloaded ${result.data.words.size}")
                        hasMore = result.data.hasMore
                        latestWordsPagId = result.data.latestId
                        _words.update { current ->
                            (current + result.data.words).distinctBy { it.id }
                        }
                    }

                    is DomainResult.Error -> {
                        latestWordsPagId = 0
                        hasMore = false
                        _errorUI.value = result.message
                    }
                }
            }
        }
    }

    fun searchWords(query: String) {
        Log.d(
            TAG,
            "searchWords($query): hasMore = $hasMore | latestFetchType = ${latestFetchType.name}"
        )
        if (!hasMore && latestFetchType == FetchWordsType.Search) {
            Log.e(TAG, "loadWords blocked because hasMore is false or latestFetchType is Search")
            return
        }
        val dictionaryId = editDictionary?.id
        if (dictionaryId == null) {
            Log.e(TAG, "dictionaryId is null")
            return
        }
        if (query.isEmpty()) {
            Log.e(TAG, "Query is empty")
            loadWords()
            return
        }
        if (!lockLoadWords.tryAcquire()) {
            Log.e(TAG, "searchWords blocked because it's already running")
            return
        }
        latestFetchType = FetchWordsType.Search
        Log.d(TAG, "searchWords($query - $latestSearchWordsPagId)")
        _loadingDataUI.value = true
        latestWordsPagId = 0
        _words.value = emptyList()
        viewModelScope.launch {
            searchWordsForDictionaryUseCase(
                SearchWordsForDictionaryParams(
                    query = query,
                    lastPageId = latestSearchWordsPagId,
                    pageSize = WORDS_PAGE_SIZE,
                    dictionaryId = dictionaryId
                )
            ).catch {
                Log.d(TAG, "catch ${it.message}")
                _errorUI.value = it.message ?: "Error"
            }.onCompletion {
                Log.d(TAG, "searchWords onCompletion")
                lockLoadWords.release()
                _loadingDataUI.value = false
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        Log.d(TAG, "Words downloaded ${result.data.words.size}")
                        hasMore = latestSearchWordsPagId == result.data.latestId
                        latestSearchWordsPagId = result.data.latestId
                        _words.update { current ->
                            (current + result.data.words).distinctBy { it.id }
                        }
                    }

                    is DomainResult.Error -> {
                        latestSearchWordsPagId = 0
                        hasMore = false
                        _errorUI.value = result.message
                    }
                }
            }
        }
    }
}

sealed class DictionaryValidationException(message: String) : Exception(message) {
    object LanguageFromException : Exception("Language from is not valid or empty")
    object LanguageToException : Exception("Language to is not valid or empty")
}

enum class FetchWordsType {
    All, Search
}
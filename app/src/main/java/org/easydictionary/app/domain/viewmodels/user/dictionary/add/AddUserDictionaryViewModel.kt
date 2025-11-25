package org.easydictionary.app.domain.viewmodels.user.dictionary.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.Json
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.domain.usecases.dictionary.CreateDictionaryParams
import org.easydictionary.app.domain.usecases.dictionary.CreateDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.DeleteDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.GetDetailDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.UpdateDictionaryParams
import org.easydictionary.app.domain.usecases.dictionary.UpdateDictionaryUseCase
import org.easydictionary.app.domain.usecases.languages.AddUserLanguageParams
import org.easydictionary.app.domain.usecases.languages.AddUserLanguageUseCase
import org.easydictionary.app.domain.usecases.word.GetAllWordsForDictionaryParams
import org.easydictionary.app.domain.usecases.word.GetAllWordsForDictionaryUseCase
import org.easydictionary.app.domain.usecases.word.SearchWordsForDictionaryParams
import org.easydictionary.app.domain.usecases.word.SearchWordsForDictionaryUseCase
import java.util.Collections
import javax.inject.Inject

sealed interface AddOrEditUserDictionaryEffect {
    data object DictionaryCreated : AddOrEditUserDictionaryEffect
    data class ShowError(val message: String) : AddOrEditUserDictionaryEffect
}

interface AddOrEditUserDictionaryContract {
    val state: StateFlow<AddOrEditUserDictionaryUiState>
    val effects: Flow<AddOrEditUserDictionaryEffect>

    fun loadWords()

    fun onDialectChanged(dialect: String?)
    fun onQueryChanged(query: String?, fromScroll: Boolean)
    fun createDictionary()
    fun updateDictionary()
    fun deleteDictionary()
    fun isEditMode(): Boolean
    fun setLanguage(langType: LangType, json: String)
    fun setEditMode(dictionaryDetailShort: DictionaryDetailShort?)
}

@HiltViewModel
class AddUserDictionaryViewModel @Inject constructor(
    private val createDictionaryUseCase: CreateDictionaryUseCase,
    private val addUserLanguageUseCase: AddUserLanguageUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase,
    private val updateDictionaryUseCase: UpdateDictionaryUseCase,
    private val getAllWordsForDictionaryUseCase: GetAllWordsForDictionaryUseCase,
    private val searchWordsForDictionaryUseCase: SearchWordsForDictionaryUseCase,
    private val getDetailDictionaryUseCase: GetDetailDictionaryUseCase,
) : ViewModel(), AddOrEditUserDictionaryContract {

    companion object {
        private val TAG = AddUserDictionaryViewModel::class.simpleName
        private const val WORDS_PAGE_SIZE = 20
    }

    private val _state = MutableStateFlow(AddOrEditUserDictionaryUiState())
    override val state: StateFlow<AddOrEditUserDictionaryUiState> = _state

    private val _effects =
        MutableSharedFlow<AddOrEditUserDictionaryEffect>(extraBufferCapacity = 1, replay = 1)
    override val effects: Flow<AddOrEditUserDictionaryEffect> = _effects

    override fun setLanguage(langType: LangType, json: String) {
        val language: Language = Json.decodeFromString(json)
        when (langType) {
            LangType.FROM -> {
                _state.update { it.copy(selectedLanguageFrom = language) }
            }

            LangType.TO -> {
                _state.update { it.copy(selectedLanguageTo = language) }
            }
        }
    }

    override fun onDialectChanged(dialect: String?) {
        _state.update { it.copy(dialect = dialect) }
    }

    override fun onQueryChanged(query: String?, fromScroll: Boolean) {
        Log.d(
            TAG,
            "onQueryChanged = $query | fromScroll = $fromScroll | query in state = ${state.value.filter.query}"
        )
        if (state.value.filter.query == query && !state.value.hasMore) {
            Log.e(TAG, "All words are already loaded")
            return
        }
        val loadMore = !query.isNullOrEmpty() && state.value.filter.query == query && fromScroll
        _state.update { it.copy(filter = it.filter.copy(query = query)) }
        searchWords(query, loadMore)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun createDictionary() {
        val languageFrom =
            state.value.selectedLanguageFrom
                ?: throw DictionaryValidationException.LanguageFromException
        val languageTo =
            state.value.selectedLanguageTo
                ?: throw DictionaryValidationException.LanguageToException
        Log.d(TAG, "createDictionary(${state.value.dialect})")
        _state.update { it.copy(isLoading = true) }
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
                            state.value.dialect,
                            (langs.first as DomainResult.Success<Language>).data.id,
                            (langs.second as DomainResult.Success<Language>).data.id
                        )
                    ) else {
                    throw IllegalStateException("Languages wasn't created")
                }
            }.catch {
                Log.d(TAG, "catch ${it.message}")
                _effects.tryEmit(AddOrEditUserDictionaryEffect.ShowError(it.message ?: "Error"))
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _state.update { it.copy(isLoading = false) }
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        Log.d(TAG, "Dictionary created")
                        _effects.tryEmit(AddOrEditUserDictionaryEffect.DictionaryCreated)
                    }

                    is DomainResult.Error -> _effects.tryEmit(
                        AddOrEditUserDictionaryEffect.ShowError(
                            result.message
                        )
                    )
                }
            }
        }
    }

    override fun updateDictionary() {
        if (!isEditMode()) return
        Log.d(TAG, "updateDictionary $${state.value.dialect}")
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            updateDictionaryUseCase(
                UpdateDictionaryParams(
                    id = state.value.editDictionary!!.id,
                    dialect = state.value.dialect
                )
            ).catch {
                Log.d(TAG, "catch ${it.message}")
                _effects.tryEmit(AddOrEditUserDictionaryEffect.ShowError(it.message ?: "Error"))
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _state.update { it.copy(isLoading = false) }
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        Log.d(TAG, "Dictionary ${state.value.editDictionary?.id} updated")
                        _effects.tryEmit(AddOrEditUserDictionaryEffect.DictionaryCreated)
                    }

                    is DomainResult.Error -> _effects.tryEmit(
                        AddOrEditUserDictionaryEffect.ShowError(
                            result.message
                        )
                    )
                }
            }
        }
    }

    override fun deleteDictionary() {
        if (!isEditMode()) return
        val deleteId = state.value.editDictionary?.id ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            deleteDictionaryUseCase.invoke(deleteId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddOrEditUserDictionaryEffect.ShowError(it.message ?: "Error"))
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "Dictionary ${state.value.editDictionary?.id} deleted")
                            _effects.tryEmit(AddOrEditUserDictionaryEffect.DictionaryCreated)
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            AddOrEditUserDictionaryEffect.ShowError(
                                result.message
                            )
                        )
                    }
                }
        }
    }

    override fun isEditMode() = state.value.editDictionary != null

    override fun setEditMode(dictionaryDetailShort: DictionaryDetailShort?) {
        _state.update {
            it.copy(
                editDictionary = dictionaryDetailShort,
                selectedLanguageTo = dictionaryDetailShort?.langFrom,
                selectedLanguageFrom = dictionaryDetailShort?.langTo,
                dialect = dictionaryDetailShort?.dialect
            )
        }
        dictionaryDetailShort?.let {
            loadDetailDictionary(it.id)
        }
    }

    override fun loadWords() {
        Log.d(
            TAG,
            "loadWords: hasMore = ${state.value.hasMore} | latestFetchType = ${state.value.latestFetchType.name}"
        )
        if (!state.value.hasMore && state.value.latestFetchType == FetchWordsType.All) {
            Log.e(TAG, "loadWords blocked because hasMore is false or latestFetchType is ALL")
            return
        }
        val dictionaryId = state.value.editDictionary?.id
        if (dictionaryId == null) {
            Log.e(TAG, "dictionaryId is null")
            return
        }
        if (!state.value.lockLoadWords.tryAcquire()) {
            Log.e(TAG, "loadWords blocked because it's already running")
            return
        }
        _state.update { it.copy(latestFetchType = FetchWordsType.All) }
        Log.d(TAG, "loadWords(${state.value.latestWordsPagId})")
        _state.update { it.copy(latestSearchWordsPagId = 0, isLoading = true) }
        viewModelScope.launch {
            try {
                getAllWordsForDictionaryUseCase(
                    GetAllWordsForDictionaryParams(
                        lastPageId = state.value.latestWordsPagId,
                        pageSize = WORDS_PAGE_SIZE,
                        dictionaryId = dictionaryId
                    )
                ).catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddOrEditUserDictionaryEffect.ShowError(it.message ?: "Error"))
                }.onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
                }.collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "Words downloaded ${result.data.words.size}")
                            _state.update {
                                it.copy(
                                    hasMore = result.data.hasMore,
                                    latestWordsPagId = result.data.latestId,
                                    words = it.words + result.data.words
                                )
                            }
                        }

                        is DomainResult.Error -> {
                            _state.update {
                                it.copy(
                                    hasMore = false,
                                    latestWordsPagId = 0
                                )
                            }
                            _effects.tryEmit(
                                AddOrEditUserDictionaryEffect.ShowError(
                                    result.message
                                )
                            )
                        }
                    }
                }
            } finally {
                state.value.lockLoadWords.release()
            }
        }
    }

    private fun searchWords(query: String?, loadMore: Boolean = false) {
        Log.d(
            TAG,
            "searchWords($query): loadMore = $loadMore | latestFetchType = ${state.value.latestFetchType.name}"
        )
        val dictionaryId = state.value.editDictionary?.id
        if (dictionaryId == null) {
            Log.e(TAG, "dictionaryId is null")
            return
        }
        if (query.isNullOrEmpty()) {
            Log.e(TAG, "Query is empty")
            loadWords()
            return
        }
        if (!state.value.lockLoadWords.tryAcquire()) {
            Log.e(TAG, "searchWords blocked because it's already running")
            return
        }
        val latestSearchWordsPagId = if (loadMore) state.value.latestSearchWordsPagId else 0
        if (latestSearchWordsPagId == 0) {
            _state.update {
                it.copy(
                    words = emptyList()
                )
            }
        }
        _state.update {
            it.copy(
                latestFetchType = FetchWordsType.Search,
                latestWordsPagId = 0,
                isLoading = true,
                latestSearchWordsPagId = latestSearchWordsPagId
            )
        }

        Log.d(TAG, "searchWords($query - $latestSearchWordsPagId)")
        viewModelScope.launch {
            try {
                searchWordsForDictionaryUseCase(
                    SearchWordsForDictionaryParams(
                        query = query,
                        lastPageId = latestSearchWordsPagId,
                        pageSize = WORDS_PAGE_SIZE,
                        dictionaryId = dictionaryId
                    )
                ).catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddOrEditUserDictionaryEffect.ShowError(it.message ?: "Error"))
                }.onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
                }.collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "Words downloaded ${result.data.words.size}")
                            _state.update {
                                it.copy(
                                    hasMore = result.data.hasMore,
                                    latestSearchWordsPagId = result.data.latestId,
                                    words = it.words + result.data.words
                                )
                            }
                        }

                        is DomainResult.Error -> {
                            _state.update {
                                it.copy(
                                    hasMore = false,
                                    latestSearchWordsPagId = 0
                                )
                            }
                            _effects.tryEmit(
                                AddOrEditUserDictionaryEffect.ShowError(
                                    result.message
                                )
                            )
                        }
                    }
                }
            } finally {
                state.value.lockLoadWords.release()
            }
        }
    }

    private fun loadDetailDictionary(dictionaryId: Int) {
        if (!isEditMode()) return
        Log.d(TAG, "loadDetailDictionary($dictionaryId)")
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getDetailDictionaryUseCase.invoke(dictionaryId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddOrEditUserDictionaryEffect.ShowError(it.message ?: "Error"))
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            _state.update { it.copy(detailDictionary = result.data) }
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            AddOrEditUserDictionaryEffect.ShowError(
                                result.message
                            )
                        )
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
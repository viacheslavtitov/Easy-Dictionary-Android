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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.category.CategoryDictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.dictionary.WordTypeSelectableItem
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.word.WordTag
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
import org.easydictionary.app.domain.utils.DateRangeFormatter
import org.easydictionary.app.view.ext.toMillis
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed interface AddOrEditUserDictionaryEffect {
    data object DictionaryCreated : AddOrEditUserDictionaryEffect
    data class ShowError(val message: String) : AddOrEditUserDictionaryEffect
}

interface AddOrEditUserDictionaryContract {
    val state: StateFlow<AddOrEditUserDictionaryUiState>
    val effects: Flow<AddOrEditUserDictionaryEffect>

    fun loadWords(query: String?)

    fun onDialectChanged(dialect: String?)
    fun createDictionary()
    fun updateDictionary()
    fun deleteDictionary()
    fun isEditMode(): Boolean
    fun getDictionary(): DictionaryDetailShort?
    fun setLanguage(langType: LangType, json: String)
    fun setEditMode(dictionaryDetailShort: DictionaryDetailShort?)
    fun onFilterTagClicked(tag: WordTag)
    fun onFilterCategoryClicked(category: CategoryDictionary)
    fun onFilterWordTypeClicked(type: WordTypeSelectableItem)
    fun onFilterDateRangeFromChanged(date: String?)
    fun onFilterDateRangeToChanged(date: String?)
    fun onFilterClearClicked()
    fun getDateRangeFormatter(): DateTimeFormatter
    fun getFilterDateFromInMillis(): Long?
    fun getFilterDateToInMillis(): Long?
}

@HiltViewModel
class AddUserDictionaryViewModel @Inject constructor(
    private val createDictionaryUseCase: CreateDictionaryUseCase,
    private val addUserLanguageUseCase: AddUserLanguageUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase,
    private val updateDictionaryUseCase: UpdateDictionaryUseCase,
    private val getAllWordsForDictionaryUseCase: GetAllWordsForDictionaryUseCase,
    private val dateRangeFormatter: DateRangeFormatter,
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
    override fun getDictionary(): DictionaryDetailShort? = state.value.editDictionary

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

    override fun onFilterTagClicked(tag: WordTag) {
        _state.update {
            it.copy(
                filter = it.filter.copy(tags = it.filter.tags.map { t ->
                    if (t.id == tag.id) t.copy(
                        selected = !tag.selected
                    ) else t
                })
            )
        }
    }

    override fun onFilterCategoryClicked(category: CategoryDictionary) {
        _state.update {
            it.copy(
                filter = it.filter.copy(categories = it.filter.categories.map { c ->
                    if (c.id == category.id) c.copy(
                        selected = !category.selected
                    ) else c
                })
            )
        }
    }

    override fun onFilterWordTypeClicked(type: WordTypeSelectableItem) {
        _state.update {
            it.copy(
                filter = it.filter.copy(wordTypes = it.filter.wordTypes.map { t ->
                    if (t.name == type.name) t.copy(
                        selected = !type.selected
                    ) else t
                })
            )
        }
    }

    override fun onFilterDateRangeFromChanged(date: String?) {
        _state.update {
            it.copy(
                filter = it.filter.copy(dateFrom = date)
            )
        }
    }

    override fun onFilterDateRangeToChanged(date: String?) {
        _state.update {
            it.copy(
                filter = it.filter.copy(dateTo = date)
            )
        }
    }

    override fun onFilterClearClicked() {
        _state.update {
            it.copy(
                filter = it.filter.copy(
                    wordTypes = it.filter.wordTypes.map { t ->
                        t.copy(selected = false)
                    },
                    tags = it.filter.tags.map { t ->
                        t.copy(selected = false)
                    },
                    categories = it.filter.categories.map { t ->
                        t.copy(selected = false)
                    },
                )
            )
        }
    }

    override fun getDateRangeFormatter(): DateTimeFormatter {
        return dateRangeFormatter.format()
    }

    override fun getFilterDateFromInMillis(): Long? {
        return state.value.filter.dateFrom?.toMillis(dateRangeFormatter.format())
    }

    override fun getFilterDateToInMillis(): Long? {
        return state.value.filter.dateTo?.toMillis(dateRangeFormatter.format())
    }

    override fun loadWords(query: String?) {
        Log.d(
            TAG,
            "loadWords: hasMore = ${state.value.hasMore}"
        )
        var isNewLoad = false
        val dictionaryId = state.value.editDictionary?.id
        if (dictionaryId == null) {
            Log.e(TAG, "dictionaryId is null")
            return
        }
        if (state.value.isLoading) {
            Log.e(TAG, "words loading is in progress")
            return
        }
        _state.update { it.copy(filter = it.filter.copy(query = query)) }
        if (state.value.latestAppliedFilter != state.value.filter) {
            Log.d(
                TAG,
                "Latest filter is not equal with new. HasMore and pagination id will be zeroed"
            )
            _state.update {
                it.copy(
                    hasMore = true,
                    latestWordsPagId = 0,
                )
            }
            isNewLoad = true
        }
        if (!state.value.hasMore) {
            Log.e(TAG, "loadWords blocked because hasMore is false")
            return
        }
        Log.d(TAG, "loadWords(${state.value.latestWordsPagId})")
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getAllWordsForDictionaryUseCase(
                GetAllWordsForDictionaryParams(
                    lastPageId = state.value.latestWordsPagId,
                    pageSize = WORDS_PAGE_SIZE,
                    dictionaryId = dictionaryId,
                    query = state.value.filter.query ?: "",
                    categoryIds = state.value.filter.toCategoryIds(),
                    wordTypes = state.value.filter.toWordTypes(),
                    tagIds = state.value.filter.toTagIds(),
                    dateFrom = state.value.filter.dateFrom ?: "",
                    dateTo = state.value.filter.dateTo ?: ""
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
                                words = if (isNewLoad) result.data.words else it.words + result.data.words,
                                latestAppliedFilter = it.filter.copy()
                            )
                        }
                    }

                    is DomainResult.Error -> {
                        _effects.tryEmit(
                            AddOrEditUserDictionaryEffect.ShowError(
                                result.message
                            )
                        )
                    }
                }
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
                            _state.update {
                                it.copy(
                                    detailDictionary = result.data, filter = WordsFilterUIState(
                                        tags = result.data.tags,
                                        categories = result.data.categories,
                                        wordTypes = result.data.wordTypes
                                    )
                                )
                            }
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
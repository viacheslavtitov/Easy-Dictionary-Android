package org.easydictionary.app.domain.viewmodels.user.dictionary.add.languages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.usecases.languages.GetLanguagesStaticUseCase
import org.easydictionary.app.domain.usecases.languages.GetLanguagesUserUseCase
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    private val getLanguagesStaticUseCase: GetLanguagesStaticUseCase,
    private val getLanguagesUserUseCase: GetLanguagesUserUseCase,
) : ViewModel() {

    companion object {
        private val TAG = LanguagesViewModel::class.simpleName
        const val BUNDLE_SELECTED_LANGUAGE_FROM = "BUNDLE_SELECTED_LANGUAGE_FROM"
        const val BUNDLE_SELECTED_LANGUAGE_TO = "BUNDLE_SELECTED_LANGUAGE_TO"
    }

    private val _allLanguages = MutableStateFlow<List<Language>>(emptyList())
    val searchQuery = MutableStateFlow("")
    val filteredLanguages: StateFlow<List<Language>> =
        combine(_allLanguages, searchQuery.debounce(300)) { list, query ->
            if (query.isBlank()) list
            else list.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    private val _loadingDataUI = MutableStateFlow<Boolean>(false)
    val loadingDataUI: StateFlow<Boolean> = _loadingDataUI.asStateFlow()
    private val _errorUI = MutableStateFlow<String>("")
    val errorUI: StateFlow<String> = _errorUI.asStateFlow()

    fun loadLanguages() {
        Log.d(TAG, "loadLanguages()")
        _loadingDataUI.value = true
        viewModelScope.launch {
            combine(
                getLanguagesStaticUseCase(Unit),
                getLanguagesUserUseCase(Unit)
            ) { staticLanguages, userLanguages ->
                if (staticLanguages is DomainResult.Success && userLanguages is DomainResult.Success) {
                    val merged = (userLanguages.data + staticLanguages.data.map { language ->
                        Language(
                            code = language.code,
                            name = language.name,
                            id = language.id ?: -1
                        )
                    }).distinctBy { it.name }
                    return@combine DomainResult.Success(merged)
                } else {
                    return@combine userLanguages
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
                        Log.d(TAG, "collected ${result.data.size} languages")
                        _allLanguages.value = result.data
                    }

                    is DomainResult.Error -> _errorUI.value = result.message
                }
            }
        }
    }

    fun addNewLanguage(language: String) {
        if (language.isNotEmpty() && _allLanguages.value.find { it.name == language } == null) {
            val minElement = _allLanguages.value.minBy { it.id }
            Log.d(TAG, "Add new language $language by id ${minElement.id - 1}")
            _allLanguages.value =
                _allLanguages.value + Language(id = minElement.id - 1, name = language)
        } else {
            Log.d(TAG, "Can't add language is empty")
        }
    }
}
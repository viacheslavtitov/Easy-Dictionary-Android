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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.usecases.dictionary.GetCreateDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.UpdateDictionaryUseCase
import org.easydictionary.app.domain.usecases.languages.AddUserLanguageUseCase
import org.easydictionary.app.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import javax.inject.Inject

@HiltViewModel
class AddUserDictionaryViewModel @Inject constructor(
    private val dictionaryUseCase: GetCreateDictionaryUseCase,
    private val addUserLanguageUseCase: AddUserLanguageUseCase,
    private val updateDictionaryUseCase: UpdateDictionaryUseCase
) : ViewModel() {

    companion object {
        private val TAG = AddUserDictionaryViewModel::class.simpleName
    }

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
                addUserLanguageUseCase.invoke(languageFrom.code, languageFrom.name),
                addUserLanguageUseCase.invoke(languageTo.code, languageTo.name)
            ) { langFrom, langTo ->
                langFrom to langTo
            }.flatMapLatest { langs ->
                if (langs.first is DomainResult.Success && langs.second is DomainResult.Success)
                    return@flatMapLatest dictionaryUseCase.createDictionary(
                        dialectValue,
                        (langs.first as DomainResult.Success<Language>).data.id,
                        (langs.second as DomainResult.Success<Language>).data.id
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
        if(!isEditMode()) return
        Log.d(TAG, "updateDictionary $dialectValue")
        _loadingDataUI.value = true
        viewModelScope.launch {
            updateDictionaryUseCase.invoke(
                Dictionary(
                    id = editDictionary!!.id,
                    dialect = dialectValue,
                    langToId = editDictionary!!.langFrom!!.id,
                    langFromId = editDictionary!!.langFrom!!.id
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

    fun isEditMode() = editDictionary != null

    fun setEditMode(dictionaryDetailShort: DictionaryDetailShort?) {
        editDictionary = dictionaryDetailShort
        _selectedLanguageFrom.value = editDictionary?.langFrom
        _selectedLanguageTo.value = editDictionary?.langTo
        _dialect.value = editDictionary?.dialect ?: ""
    }
}

sealed class DictionaryValidationException(message: String) : Exception(message) {
    object LanguageFromException : Exception("Language from is not valid or empty")
    object LanguageToException : Exception("Language to is not valid or empty")
}
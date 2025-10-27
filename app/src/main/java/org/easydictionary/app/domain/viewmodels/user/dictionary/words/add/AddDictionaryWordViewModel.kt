package org.easydictionary.app.domain.viewmodels.user.dictionary.words.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.language.Phonetic
import org.easydictionary.app.domain.models.translation.ComposedTranslation
import org.easydictionary.app.domain.models.translation.TranslationNotCreated
import org.easydictionary.app.domain.models.translation.TranslationWithCategory
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.domain.usecases.languages.GetPhoneticsUseCase
import org.easydictionary.app.domain.usecases.word.AddWordToDictionaryParams
import org.easydictionary.app.domain.usecases.word.AddWordToDictionaryUseCase
import org.easydictionary.app.domain.usecases.word.DeleteWordUseCase
import org.easydictionary.app.domain.usecases.word.translations.AddTranslationParams
import org.easydictionary.app.domain.usecases.word.translations.AddTranslationUseCase
import org.easydictionary.app.domain.usecases.word.translations.DeleteTranslationUseCase
import org.easydictionary.app.domain.usecases.word.types.GetWordTypesUseCase
import javax.inject.Inject

@HiltViewModel
class AddDictionaryWordViewModel @Inject constructor(
    private val addWordToDictionaryUseCase: AddWordToDictionaryUseCase,
    private val deleteWordUseCase: DeleteWordUseCase,
    private val getPhoneticsUseCase: GetPhoneticsUseCase,
    private val getWordTypesUseCase: GetWordTypesUseCase,
    private val deleteTranslationUseCase: DeleteTranslationUseCase,
    private val addTranslationUseCase: AddTranslationUseCase,
) : ViewModel() {

    companion object {
        private val TAG = AddDictionaryWordViewModel::class.simpleName
        const val BUNDLE_NEED_UPDATE_WORDS =
            "org.easydictionary.app.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordViewModel.BUNDLE_NEED_UPDATE_WORDS"
    }

    private val _loadingDataUI = MutableStateFlow<Boolean>(false)
    val loadingDataUI: StateFlow<Boolean> = _loadingDataUI.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _translations = MutableStateFlow<List<ComposedTranslation>>(emptyList())
    val translations: StateFlow<List<ComposedTranslation>> = _translations.asStateFlow()
    private val _wordTypes = MutableStateFlow<List<String>>(emptyList())
    val wordTypes: StateFlow<List<String>> = _wordTypes.asStateFlow()
    private val _phonetics = MutableStateFlow<List<Phonetic>>(emptyList())
    val phonetics: StateFlow<List<Phonetic>> = _phonetics.asStateFlow()
    private val _wordCreated = MutableSharedFlow<Boolean>()
    val wordCreated: SharedFlow<Boolean> = _wordCreated
    private val _wordDeleted = MutableSharedFlow<Boolean>()
    val wordDeleted: SharedFlow<Boolean> = _wordDeleted

    private var dictionary: DictionaryDetailShort? = null
    private var editWord: WordDetail? = null

    fun displayError(message: String) {
        _errorMessage.tryEmit(message)
    }

    fun isEditMode() = editWord != null

    fun setDictionary(dictionary: DictionaryDetailShort?) {
        this.dictionary = dictionary
        loadPhonetics()
    }

    fun setWord(word: WordDetail?) {
        this.editWord = word
//        _translations.value = emptyList()
        editWord?.translations?.forEach {
            addTranslation(it)
        }
    }

    private fun loadPhonetics() {
        viewModelScope.launch {
            getPhoneticsUseCase(Unit)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    displayError(it.message ?: "Error")
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "collected ${result.data.size} phonetics")
                            _phonetics.value = result.data
                        }

                        is DomainResult.Error -> displayError(result.message)
                    }
                }
        }
    }

    fun addTranslation(translation: TranslationNotCreated) {
        Log.d(
            TAG,
            "addTranslation ${translation.translate} for existing word ${isEditMode()} with size ${_translations.value.size}"
        )
        val exist = _translations.value.find { it.translate == translation.translate } != null
        if(exist) {
            Log.e(TAG, "translation ${translation.translate} is already exist")
            return
        }
        if (!isEditMode()) {
            _translations.value = _translations.value + ComposedTranslation(
                category = translation.category,
                translate = translation.translate,
                description = translation.description
            )
        } else {
            val wordId = editWord?.id ?: return
            _loadingDataUI.value = true
            viewModelScope.launch {
                addTranslationUseCase(
                    AddTranslationParams(
                        wordId = wordId,
                        translate = translation.translate,
                        description = translation.description,
                        categoryId = translation.category?.id
                    )
                )
                    .catch {
                        Log.d(TAG, "catch ${it.message}")
                        displayError(it.message ?: "Error")
                    }
                    .onCompletion {
                        Log.d(TAG, "onCompletion")
                        _loadingDataUI.value = false
                    }
                    .collect { result ->
                        when (result) {
                            is DomainResult.Success -> {
                                _translations.value = _translations.value + ComposedTranslation(
                                    category = translation.category,
                                    translate = translation.translate,
                                    description = translation.description,
                                    id = result.data
                                )
                            }

                            is DomainResult.Error -> displayError(result.message)
                        }
                    }
            }
        }
    }

    fun addTranslation(translation: TranslationWithCategory) {
        val exist = _translations.value.find { "${it.id}-${it.translate}" == "${translation.id}-${translation.translate}" } != null
        if(exist) {
            Log.e(TAG, "translation ${translation.translate} is already exist")
            return
        }
        _translations.value = _translations.value + ComposedTranslation(
            category = translation.category,
            translate = translation.translate,
            description = translation.description,
            id = translation.id,
            wordId = editWord?.id
        )
    }

    fun deleteTranslation(translation: ComposedTranslation) {
        if (translation.id == null) {
            _translations.value = _translations.value.minus(translation)
        } else {
            deleteTranslationForever(translation)
        }
    }

    fun loadWordTypes() {
        Log.d(TAG, "loadWordTypes")
        _loadingDataUI.value = true
        viewModelScope.launch {
            getWordTypesUseCase(Unit)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    displayError(it.message ?: "Error")
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "collected ${result.data.size} word types")
                            _wordTypes.value = result.data
                        }

                        is DomainResult.Error -> displayError(result.message)
                    }
                }
        }
    }

    fun createWord(
        original: String,
        phonetic: String?,
        type: String?
    ) {
        val dictionary = dictionary ?: return
        _loadingDataUI.value = true
        viewModelScope.launch {
            addWordToDictionaryUseCase(
                AddWordToDictionaryParams(
                    dictionary.id,
                    original,
                    phonetic,
                    type,
                    translations.value.map {
                        TranslationNotCreated(
                            category = it.category,
                            translate = it.translate,
                            description = it.description
                        )
                    })
            )
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    displayError(it.message ?: "Error")
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            _wordCreated.emit(true)
                        }

                        is DomainResult.Error -> displayError(result.message)
                    }
                }
        }
    }

    fun delete() {
        if (!isEditMode()) {
            Log.e(TAG, "Can't delete word because you are not in edit mode")
            return
        }
        val wordId = editWord?.id ?: return
        _loadingDataUI.value = true
        viewModelScope.launch {
            deleteWordUseCase(wordId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    displayError(it.message ?: "Error")
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            _wordDeleted.emit(true)
                        }

                        is DomainResult.Error -> displayError(result.message)
                    }
                }
        }
    }

    private fun deleteTranslationForever(translation: ComposedTranslation) {
        if (!isEditMode()) {
            Log.e(
                TAG,
                "Can't delete translation(${translation.translate}) because you are not in edit mode"
            )
            return
        }
        val translationId = translation.id ?: return
        _loadingDataUI.value = true
        viewModelScope.launch {
            deleteTranslationUseCase(translationId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    displayError(it.message ?: "Error")
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            _translations.value = _translations.value.minus(translation)
                        }

                        is DomainResult.Error -> displayError(result.message)
                    }
                }
        }
    }

    fun updateTranslation(translation: ComposedTranslation) {
        _translations.update { list ->
            list.map { if (it.id == translation.id) translation else it }
        }
    }

}
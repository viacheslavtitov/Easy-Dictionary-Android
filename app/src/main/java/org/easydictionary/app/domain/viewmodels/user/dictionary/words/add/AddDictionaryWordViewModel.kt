package org.easydictionary.app.domain.viewmodels.user.dictionary.words.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
import org.easydictionary.app.domain.models.word.WordTag
import org.easydictionary.app.domain.usecases.languages.GetPhoneticsUseCase
import org.easydictionary.app.domain.usecases.word.AddWordToDictionaryParams
import org.easydictionary.app.domain.usecases.word.AddWordToDictionaryUseCase
import org.easydictionary.app.domain.usecases.word.DeleteWordUseCase
import org.easydictionary.app.domain.usecases.word.UpdateWordUseCase
import org.easydictionary.app.domain.usecases.word.UpdateWordUseCaseParams
import org.easydictionary.app.domain.usecases.word.tags.CreateNewTagParams
import org.easydictionary.app.domain.usecases.word.tags.CreateNewTagUseCase
import org.easydictionary.app.domain.usecases.word.tags.GetTagsForDictionaryUseCase
import org.easydictionary.app.domain.usecases.word.tags.GetTagsForWordUseCase
import org.easydictionary.app.domain.usecases.word.translations.AddTranslationParams
import org.easydictionary.app.domain.usecases.word.translations.AddTranslationUseCase
import org.easydictionary.app.domain.usecases.word.translations.DeleteTranslationUseCase
import org.easydictionary.app.domain.usecases.word.types.GetWordTypesUseCase
import javax.inject.Inject

interface AddDictionaryWordContract {
    val state: StateFlow<AddDictionaryWordUiState>
    val effects: Flow<AddDictionaryWordEffect>

    fun onOriginalChanged(original: String?)
    fun onPhoneticChanged(phonetic: String?)
    fun onTypeChanged(type: String?)
    fun onNewTagChanged(tag: String?)

    fun onTagSelected(tag: WordTag, selected: Boolean)
    fun setDictionary(dictionary: DictionaryDetailShort?)
    fun setWord(word: WordDetail?)
    fun addTranslation(translation: TranslationNotCreated)
    fun addTranslation(translation: TranslationWithCategory)
    fun deleteTranslation(translation: ComposedTranslation)
    fun updateTranslation(translation: ComposedTranslation)

    fun createNewTag()
    fun createWord()
    fun updateWord()
    fun deleteWord()
    fun isEditMode(): Boolean
}

sealed class AddDictionaryWordValidationException(message: String) : Exception(message) {
    object OriginalFieldException : Exception("Original field is not valid or empty")
    object TranslationEmptyException : Exception("Translations are not valid or empty")
    object TagEmptyException : Exception("Tag is not valid or empty")
}

@HiltViewModel
class AddDictionaryWordViewModel @Inject constructor(
    private val addWordToDictionaryUseCase: AddWordToDictionaryUseCase,
    private val deleteWordUseCase: DeleteWordUseCase,
    private val getPhoneticsUseCase: GetPhoneticsUseCase,
    private val getWordTypesUseCase: GetWordTypesUseCase,
    private val deleteTranslationUseCase: DeleteTranslationUseCase,
    private val addTranslationUseCase: AddTranslationUseCase,
    private val createNewTagUseCase: CreateNewTagUseCase,
    private val getTagsForDictionaryUseCase: GetTagsForDictionaryUseCase,
    private val getTagsForWordUseCase: GetTagsForWordUseCase,
    private val updateWordUseCase: UpdateWordUseCase,
) : ViewModel(), AddDictionaryWordContract {

    companion object {
        private val TAG = AddDictionaryWordViewModel::class.simpleName
        const val BUNDLE_NEED_UPDATE_WORDS =
            "org.easydictionary.app.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordViewModel.BUNDLE_NEED_UPDATE_WORDS"
    }

    private val _state = MutableStateFlow(AddDictionaryWordUiState())
    override val state: StateFlow<AddDictionaryWordUiState> = _state

    private val _effects =
        MutableSharedFlow<AddDictionaryWordEffect>(extraBufferCapacity = 1, replay = 1)
    override val effects: Flow<AddDictionaryWordEffect> = _effects

    private fun loadAndMarkTags() {
        Log.d(
            TAG,
            "loadAndMarkTags for dictionary = ${state.value.dictionary?.id} and word ${state.value.editWord?.id}"
        )
        val dictionaryId = state.value.dictionary?.id ?: return
        val wordId = state.value.editWord?.id ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            combine(
                getTagsForDictionaryUseCase(dictionaryId),
                getTagsForWordUseCase(wordId)
            ) { allTags, wordTags ->
                when {
                    allTags is DomainResult.Error -> allTags
                    wordTags is DomainResult.Error -> allTags
                    allTags is DomainResult.Success && wordTags is DomainResult.Success -> {
                        val mapped = allTags.data.map { tag ->
                            val foundId = wordTags.data.find { it.id == tag.id }?.id
                            tag.copy(wordId = foundId, selected = foundId != null)
                        }
                        DomainResult.Success(mapped)
                    }

                    else -> DomainResult.Error("Unknown state")
                }
            }.catch {
                Log.d(TAG, "catch ${it.message}")
                _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _state.update { it.copy(isLoading = false) }
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        Log.d(TAG, "Populated tags ${result.data.size}")
                        _state.update { it.copy(tags = result.data) }
                    }

                    is DomainResult.Error -> _effects.tryEmit(
                        AddDictionaryWordEffect.ShowError(
                            result.message
                        )
                    )
                }
            }
        }
    }

    private fun loadTags() {
        Log.d(TAG, "loadTags for dictionary = ${state.value.dictionary?.id}")
        val dictionaryId = state.value.dictionary?.id ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getTagsForDictionaryUseCase(dictionaryId).catch {
                Log.d(TAG, "catch ${it.message}")
                _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _state.update { it.copy(isLoading = false) }
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        Log.d(TAG, "Populated tags ${result.data.size}")
                        _state.update { it.copy(tags = result.data) }
                    }

                    is DomainResult.Error -> _effects.tryEmit(
                        AddDictionaryWordEffect.ShowError(
                            result.message
                        )
                    )
                }
            }
        }
    }

    override fun onOriginalChanged(original: String?) {
        _state.update {
            it.copy(
                original = original
            )
        }
    }

    override fun onPhoneticChanged(phonetic: String?) {
        _state.update {
            it.copy(
                phonetic = phonetic
            )
        }
    }

    override fun onTypeChanged(type: String?) {
        _state.update {
            it.copy(
                wordType = type
            )
        }
    }

    override fun onNewTagChanged(tag: String?) {
        _state.update {
            it.copy(
                newTag = tag
            )
        }
    }

    override fun onTagSelected(
        tag: WordTag,
        selected: Boolean
    ) {
        Log.d(TAG, "tag ${tag.name} selected $selected")
        _state.update { s ->
            s.copy(
                tags = s.tags.map { t ->
                    if (t.id == tag.id) t.copy(selected = selected) else t
                }
            )
        }
    }

    override fun isEditMode() = state.value.editWord != null

    override fun setDictionary(dictionary: DictionaryDetailShort?) {
        _state.update {
            it.copy(
                dictionary = dictionary
            )
        }
        loadPhonetics()
        loadWordTypes()
    }

    override fun setWord(word: WordDetail?) {
        _state.update {
            it.copy(
                editWord = word,
                original = word?.original,
                phonetic = word?.phonetic
            )
        }
        word?.translations?.forEach {
            addTranslation(it)
        }
        if (isEditMode()) {
            loadAndMarkTags()
        } else {
            loadTags()
        }
    }

    private fun loadPhonetics() {
        viewModelScope.launch {
            getPhoneticsUseCase(Unit)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "collected ${result.data.size} phonetics")
                            _state.update {
                                it.copy(
                                    phonetics = result.data
                                )
                            }
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            AddDictionaryWordEffect.ShowError(
                                result.message
                            )
                        )
                    }
                }
        }
    }

    override fun addTranslation(translation: TranslationNotCreated) {
        Log.d(
            TAG,
            "addTranslation ${translation.translate} for existing word ${isEditMode()} with size ${state.value.translations.size}"
        )
        val exist = state.value.translations.find { it.translate == translation.translate } != null
        if (exist) {
            Log.e(TAG, "translation ${translation.translate} is already exist")
            return
        }
        if (!isEditMode()) {
            _state.update {
                it.copy(
                    translations = state.value.translations + ComposedTranslation(
                        category = translation.category,
                        translate = translation.translate,
                        description = translation.description
                    )
                )
            }
        } else {
            val wordId = state.value.editWord?.id ?: return
            _state.update { it.copy(isLoading = true) }
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
                        _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
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
                                        translations = state.value.translations + ComposedTranslation(
                                            category = translation.category,
                                            translate = translation.translate,
                                            description = translation.description,
                                            id = result.data
                                        )
                                    )
                                }
                            }

                            is DomainResult.Error -> _effects.tryEmit(
                                AddDictionaryWordEffect.ShowError(
                                    result.message
                                )
                            )
                        }
                    }
            }
        }
    }

    override fun addTranslation(translation: TranslationWithCategory) {
        val exist =
            state.value.translations.find { "${it.id}-${it.translate}" == "${translation.id}-${translation.translate}" } != null
        if (exist) {
            Log.e(TAG, "translation ${translation.translate} is already exist")
            return
        }
        _state.update {
            it.copy(
                translations = state.value.translations + ComposedTranslation(
                    category = translation.category,
                    translate = translation.translate,
                    description = translation.description,
                    id = translation.id,
                    wordId = state.value.editWord?.id
                )
            )
        }
    }

    override fun deleteTranslation(translation: ComposedTranslation) {
        if (translation.id == null) {
            _state.update { it.copy(translations = state.value.translations.minus(translation)) }
        } else {
            deleteTranslationForever(translation)
        }
    }

    private fun loadWordTypes() {
        Log.d(TAG, "loadWordTypes")
        viewModelScope.launch {
            getWordTypesUseCase(Unit)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "collected ${result.data.size} word types")
                            _state.update {
                                it.copy(
                                    wordTypes = result.data
                                )
                            }
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            AddDictionaryWordEffect.ShowError(
                                result.message
                            )
                        )
                    }
                }
        }
    }

    override fun createWord() {
        val (dictionaryId, _, isOk) = validateData()
        if(!isOk) return
        val original = state.value.original?.trim() ?: ""
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            addWordToDictionaryUseCase(
                AddWordToDictionaryParams(
                    dictionaryId,
                    original,
                    state.value.phonetic,
                    state.value.wordType,
                    state.value.translations.map {
                        TranslationNotCreated(
                            category = it.category,
                            translate = it.translate,
                            description = it.description
                        )
                    },
                    tags = state.value.tags
                )
            ).catch {
                Log.d(TAG, "catch ${it.message}")
                _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _state.update { it.copy(isLoading = false) }
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        _effects.tryEmit(AddDictionaryWordEffect.WordCreated)
                    }

                    is DomainResult.Error -> _effects.tryEmit(
                        AddDictionaryWordEffect.ShowError(
                            result.message
                        )
                    )
                }
            }
        }
    }

    override fun updateWord() {
        val (dictionaryId, wordId, isOk) = validateData()
        if(!isOk) return
        val original = state.value.original?.trim() ?: ""
        val type = state.value.wordType?.trim() ?: ""
        val tagIds = state.value.tags.filter { it.selected }.map { it.id }
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            updateWordUseCase(
                UpdateWordUseCaseParams(
                    wordId,
                    dictionaryId,
                    original,
                    state.value.phonetic,
                    type,
                    tagIds
                )
            ).catch {
                Log.d(TAG, "catch ${it.message}")
                _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _state.update { it.copy(isLoading = false) }
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        _effects.tryEmit(AddDictionaryWordEffect.WordUpdated)
                    }

                    is DomainResult.Error -> _effects.tryEmit(
                        AddDictionaryWordEffect.ShowError(
                            result.message
                        )
                    )
                }
            }
        }
    }

    private fun validateData(): Triple<Int, Int, Boolean> {
        val dictionaryId = state.value.dictionary?.id ?: return Triple(-1, -1, false)
        var wordId: Int = -1
        if (isEditMode()) {
            wordId = state.value.editWord?.id ?: return Triple(-1, -1, false)
        }
        if (state.value.original?.trim().isNullOrEmpty()) {
            throw AddDictionaryWordValidationException.OriginalFieldException
        }
        if (state.value.translations.isEmpty()) {
            throw AddDictionaryWordValidationException.TranslationEmptyException
        }
        return Triple(dictionaryId, wordId, true)
    }

    override fun deleteWord() {
        if (!isEditMode()) {
            Log.e(TAG, "Can't delete word because you are not in edit mode")
            return
        }
        val wordId = state.value.editWord?.id ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            deleteWordUseCase(wordId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            _effects.tryEmit(AddDictionaryWordEffect.WordDeleted)
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            AddDictionaryWordEffect.ShowError(
                                result.message
                            )
                        )
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
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            deleteTranslationUseCase(translationId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
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
                                    translations = it.translations.minus(translation)
                                )
                            }
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            AddDictionaryWordEffect.ShowError(
                                result.message
                            )
                        )
                    }
                }
        }
    }

    override fun updateTranslation(translation: ComposedTranslation) {
        _state.update {
            it.copy(
                translations = it.translations.map { tr -> if (tr.id == translation.id) translation else tr }
            )
        }
    }

    override fun createNewTag() {
        if (state.value.newTag?.trim().isNullOrEmpty()) {
            throw AddDictionaryWordValidationException.TagEmptyException
        }
        val dictionaryId = state.value.dictionary?.id ?: return
        val name = state.value.newTag?.trim() ?: ""
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            createNewTagUseCase(
                CreateNewTagParams(
                    dictionaryId = dictionaryId,
                    name = name
                )
            ).catch {
                Log.d(TAG, "catch ${it.message}")
                _effects.tryEmit(AddDictionaryWordEffect.ShowError(it.message ?: "Error"))
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _state.update { it.copy(isLoading = false) }
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        _effects.tryEmit(AddDictionaryWordEffect.TagCreated)
                        Log.d(TAG, "Tag created $name with id ${result.data}")
                        addCreatedTag(result.data, dictionaryId, name)
                    }

                    is DomainResult.Error -> _effects.tryEmit(
                        AddDictionaryWordEffect.ShowError(
                            result.message
                        )
                    )
                }
            }
        }
    }

    private fun addCreatedTag(tagId: Int, dictionaryId: Int, name: String) {
        _state.update {
            it.copy(
                tags = it.tags + WordTag(
                    id = tagId,
                    dictionaryId = dictionaryId,
                    name = name,
                    wordId = it.editWord?.id,
                    selected = true
                )
            )
        }
    }

}
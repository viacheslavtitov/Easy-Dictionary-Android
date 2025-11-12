package org.easydictionary.app.domain.viewmodels.user.dictionary.translations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.models.translation.ComposedTranslation
import org.easydictionary.app.domain.models.translation.Translation
import org.easydictionary.app.domain.models.translation.TranslationNotCreated
import org.easydictionary.app.domain.usecases.category.AddCategoryParams
import org.easydictionary.app.domain.usecases.category.AddCategoryUseCase
import org.easydictionary.app.domain.usecases.category.GetUserDictionaryCategoriesUseCase
import org.easydictionary.app.domain.usecases.word.translations.EditTranslationParams
import org.easydictionary.app.domain.usecases.word.translations.EditTranslationUseCase
import javax.inject.Inject

sealed interface AddTranslationVariantEffect {
    data class TranslationUpdated(val translation: ComposedTranslation) : AddTranslationVariantEffect
    data class TranslationCreated(val translation: TranslationNotCreated) : AddTranslationVariantEffect
    data object TranslationDeleted : AddTranslationVariantEffect
    data class ShowError(val message: String) : AddTranslationVariantEffect
}

data class AddTranslationVariantUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val dictionaryId: Int? = null,
    val editModel: ComposedTranslation? = null,
    val translate: String? = null,
    val description: String? = null,
    val category: Category? = null,
)

interface AddTranslationVariantContract {
    val state: StateFlow<AddTranslationVariantUiState>
    val effects: Flow<AddTranslationVariantEffect>

    fun setDictionaryId(dictionaryId: Int)
    fun setEditModel(translationVariant: ComposedTranslation?)
    fun editTranslation()
    fun createTranslation()
    fun deleteTranslation()
    fun createCategory(categoryName: String)

    fun onCategoryChanged(category: Category?)
    fun onDescriptionChanged(description: String?)
    fun onTranslateChanged(translate: String?)
    fun isEditMode(): Boolean
}

sealed class AddTranslationVariantValidationException(message: String) : Exception(message) {
    object TranslationFieldException : Exception("Translation field is not valid or empty")
}

@HiltViewModel
class AddTranslationVariantViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getUserDictionaryCategoriesUseCase: GetUserDictionaryCategoriesUseCase,
    private val editTranslationUseCase: EditTranslationUseCase,
) : ViewModel(), AddTranslationVariantContract {

    companion object {
        private val TAG = AddTranslationVariantViewModel::class.simpleName
        const val BUNDLE_NEW_TRANSLATION = "BUNDLE_NEW_TRANSLATION"
        const val BUNDLE_NEED_UPDATE_TRANSLATION =
            "org.easydictionary.app.domain.viewmodels.user.dictionary.translations.AddTranslationVariantViewModel.BUNDLE_NEED_UPDATE_TRANSLATION"
        const val BUNDLE_NEED_DELETE_TRANSLATION =
            "org.easydictionary.app.domain.viewmodels.user.dictionary.translations.AddTranslationVariantViewModel.BUNDLE_NEED_DELETE_TRANSLATION"
    }

    private val _state = MutableStateFlow(AddTranslationVariantUiState())
    override val state: StateFlow<AddTranslationVariantUiState> = _state

    private val _effects =
        MutableSharedFlow<AddTranslationVariantEffect>(extraBufferCapacity = 1, replay = 1)
    override val effects: Flow<AddTranslationVariantEffect> = _effects

    private fun loadCategories() {
        val dictionaryId = state.value.dictionaryId ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getUserDictionaryCategoriesUseCase(dictionaryId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddTranslationVariantEffect.ShowError(it.message ?: "Error"))
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "collected ${result.data.size} categories")
                            _state.update {
                                it.copy(
                                    categories = result.data
                                )
                            }
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            AddTranslationVariantEffect.ShowError(
                                result.message
                            )
                        )
                    }
                }
        }
    }

    override fun setEditModel(translationVariant: ComposedTranslation?) {
        _state.update { it.copy(editModel = translationVariant) }
        Log.d(TAG, "load exist model $translationVariant")
    }

    override fun createCategory(categoryName: String) {
        Log.d(TAG, "createCategory($categoryName)")
        val dictionaryId = state.value.dictionaryId ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            addCategoryUseCase(AddCategoryParams(dictionaryId, categoryName))
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(AddTranslationVariantEffect.ShowError(it.message ?: "Error"))
                }.onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
                }.collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "Category created")
                            loadCategories()
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            AddTranslationVariantEffect.ShowError(
                                result.message
                            )
                        )
                    }
                }
        }
    }

    override fun onCategoryChanged(category: Category?) {
        _state.update { it.copy(category = category) }
    }

    override fun onDescriptionChanged(description: String?) {
        _state.update { it.copy(description = description) }
    }

    override fun onTranslateChanged(translate: String?) {
        _state.update { it.copy(translate = translate) }
    }

    override fun isEditMode() = state.value.editModel != null

    override fun setDictionaryId(dictionaryId: Int) {
        _state.update { it.copy(dictionaryId = dictionaryId) }
        loadCategories()
    }

    override fun editTranslation() {
        Log.d(TAG, "editTranslation(${state.value.translate})")
        if (!isEditMode()) {
            Log.e(TAG, "Can't edit translation(${state.value.translate}) because you are not in edit mode")
            return
        }
        if(state.value.translate?.trim().isNullOrEmpty()) {
            throw AddTranslationVariantValidationException.TranslationFieldException
        }
        val id = state.value.editModel?.id ?: return
        val wordId = state.value.editModel?.wordId ?: return
        val translate = state.value.translate ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            editTranslationUseCase(
                EditTranslationParams(
                    wordId = wordId,
                    translationId = id,
                    translate = translate,
                    description = state.value.description,
                    categoryId = state.value.category?.id
                )
            ).catch {
                Log.d(TAG, "catch ${it.message}")
                _effects.tryEmit(AddTranslationVariantEffect.ShowError(it.message ?: "Error"))
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _state.update { it.copy(isLoading = false) }
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        Log.d(TAG, "Translation updated")
                        _effects.tryEmit(AddTranslationVariantEffect.TranslationUpdated(ComposedTranslation(
                            wordId = wordId,
                            id = id,
                            translate = translate,
                            description = state.value.description,
                            category = state.value.category
                        )))
                    }

                    is DomainResult.Error -> _effects.tryEmit(
                        AddTranslationVariantEffect.ShowError(
                            result.message
                        )
                    )
                }
            }
        }
    }

    override fun createTranslation() {
        val translate = state.value.translate?.trim() ?: throw AddTranslationVariantValidationException.TranslationFieldException
        if(translate.isEmpty()) throw AddTranslationVariantValidationException.TranslationFieldException
        _effects.tryEmit(AddTranslationVariantEffect.TranslationCreated(TranslationNotCreated(
            category = state.value.category,
            translate = translate,
            description = state.value.description
        )))
    }

    override fun deleteTranslation() {
        if (!isEditMode()) {
            Log.e(
                TAG,
                "Can't delete translation(${state.value.editModel?.translate}) because you are not in edit mode"
            )
            return
        }
        viewModelScope.launch {
            _effects.tryEmit(AddTranslationVariantEffect.TranslationDeleted)
        }
    }
}
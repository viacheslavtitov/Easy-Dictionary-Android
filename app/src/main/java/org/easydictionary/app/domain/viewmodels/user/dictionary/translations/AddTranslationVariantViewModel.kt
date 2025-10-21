package org.easydictionary.app.domain.viewmodels.user.dictionary.translations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.models.translation.ComposedTranslation
import org.easydictionary.app.domain.usecases.category.AddCategoryParams
import org.easydictionary.app.domain.usecases.category.AddCategoryUseCase
import org.easydictionary.app.domain.usecases.category.GetUserDictionaryCategoriesUseCase
import org.easydictionary.app.domain.usecases.word.translations.EditTranslationParams
import org.easydictionary.app.domain.usecases.word.translations.EditTranslationUseCase
import javax.inject.Inject

@HiltViewModel
class AddTranslationVariantViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getUserDictionaryCategoriesUseCase: GetUserDictionaryCategoriesUseCase,
    private val editTranslationUseCase: EditTranslationUseCase,
) : ViewModel() {

    companion object {
        private val TAG = AddTranslationVariantViewModel::class.simpleName
        const val BUNDLE_NEW_TRANSLATION = "BUNDLE_NEW_TRANSLATION"
        const val BUNDLE_NEED_UPDATE_TRANSLATION = "org.easydictionary.app.domain.viewmodels.user.dictionary.translations.AddTranslationVariantViewModel.BUNDLE_NEED_UPDATE_TRANSLATION"
    }

    private val _loadingDataUI = MutableStateFlow<Boolean>(false)
    val loadingDataUI: StateFlow<Boolean> = _loadingDataUI.asStateFlow()

    private val _errorUI = MutableStateFlow<String>("")
    val errorUI: StateFlow<String> = _errorUI.asStateFlow()
    private val _categories =
        MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> =
        _categories.asStateFlow()

    private var editModel: ComposedTranslation? = null
    private var dictionaryId: Int? = null
    private val _translationUpdated = MutableSharedFlow<ComposedTranslation>()
    val translationUpdated: SharedFlow<ComposedTranslation> = _translationUpdated

    fun loadCategories() {
        val dictionaryId = dictionaryId ?: return
        _loadingDataUI.value = true
        viewModelScope.launch {
            getUserDictionaryCategoriesUseCase(dictionaryId)
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
                            Log.d(TAG, "collected ${result.data.size} categories")
                            _categories.value = result.data
                        }

                        is DomainResult.Error -> _errorUI.value = result.message
                    }
                }
        }
    }

    fun setEditModel(translationVariant: ComposedTranslation?) {
        editModel = translationVariant
        Log.d(TAG, "load exist model $translationVariant")
    }

    fun createCategory(categoryName: String) {
        Log.d(TAG, "createCategory($categoryName)")
        val dictionaryId = dictionaryId ?: return
        _loadingDataUI.value = true
        viewModelScope.launch {
            addCategoryUseCase(AddCategoryParams(dictionaryId, categoryName))
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _errorUI.value = it.message ?: "Error"
                }.onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }.collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "Category created")
                            loadCategories()
                        }

                        is DomainResult.Error -> _errorUI.value = result.message
                    }
                }
        }
    }

    fun isEditMode() = editModel != null

    fun getEditModel() = editModel

    fun setDictionaryId(dictionaryId: Int) {
        this.dictionaryId = dictionaryId
    }

    fun editTranslation(
        translate: String,
        description: String?,
        category: Category?
    ) {
        Log.d(TAG, "editTranslation($translate)")
        if (!isEditMode()) {
            Log.e(TAG, "Can't edit translation($translate) because you are not in edit mode")
            return
        }
        val id = editModel?.id ?: return
        val wordId = editModel?.wordId ?: return
        _loadingDataUI.value = true
        viewModelScope.launch {
            editTranslationUseCase(
                EditTranslationParams(
                    wordId = wordId,
                    translationId = id,
                    translate = translate,
                    description = description,
                    categoryId = category?.id
                )
            ).catch {
                Log.d(TAG, "catch ${it.message}")
                _errorUI.value = it.message ?: "Error"
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                _loadingDataUI.value = false
            }.collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        Log.d(TAG, "Translation updated")
                        _translationUpdated.emit(ComposedTranslation(
                            wordId = wordId,
                            id = id,
                            translate = translate,
                            description = description,
                            category = category
                        ))
                    }

                    is DomainResult.Error -> _errorUI.value = result.message
                }
            }
        }
    }

}
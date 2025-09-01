package org.easydictionary.app.domain.viewmodels.user.dictionary.translations

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.models.translation.ComposedTranslation
import org.easydictionary.app.domain.models.words.variants.TranslationCategory
import org.easydictionary.app.domain.models.words.variants.TranslationVariant
import org.easydictionary.app.domain.usecases.category.AddCategoryUseCase
import org.easydictionary.app.domain.usecases.category.GetUserDictionaryCategoriesUseCase
import org.easydictionary.app.domain.usecases.translations.GetCreateTranslationsUseCase
import org.easydictionary.app.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class AddTranslationVariantViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getUserDictionaryCategoriesUseCase: GetUserDictionaryCategoriesUseCase,
    private val getCreateTranslationsUseCase: GetCreateTranslationsUseCase,
    private val uiStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private val TAG = AddTranslationVariantViewModel::class.simpleName
        private const val KEY_STATE_TRANSLATION = "translation"
        private const val KEY_STATE_EXAMPLE = "example"
        private const val KEY_STATE_CATEGORY = "category"
        const val BUNDLE_NEW_TRANSLATION = "BUNDLE_NEW_TRANSLATION"
    }

    private val _loadingDataUI = MutableStateFlow<Boolean>(false)
    val loadingDataUI: StateFlow<Boolean> = _loadingDataUI.asStateFlow()

    private val _errorUI = MutableStateFlow<String>("")
    val errorUI: StateFlow<String> = _errorUI.asStateFlow()
    private val _categories =
        MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> =
        _categories.asStateFlow()

    val translationSavedUIState: StateFlow<String> =
        uiStateHandle.getStateFlow(KEY_STATE_TRANSLATION, "")
    val exampleSavedUIState: StateFlow<String> = uiStateHandle.getStateFlow(KEY_STATE_EXAMPLE, "")
    val categorySavedUIState: StateFlow<Int> = uiStateHandle.getStateFlow(KEY_STATE_CATEGORY, -1)

    private var editModel: ComposedTranslation? = null
    private var dictionaryId: Int? = null

    fun loadCategories() {
        val dictionaryId = dictionaryId ?: return
        _loadingDataUI.value = true
        viewModelScope.launch {
            getUserDictionaryCategoriesUseCase.invoke(dictionaryId)
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
            addCategoryUseCase.invoke(dictionaryId, categoryName)
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

    fun validateTranslation(context: Context?, translation: String?) =
        flow<FetchDataState<Boolean>> {
            if (context == null) {
                emit(FetchDataState.DataState(false))
                return@flow
            }
            if (translation.isNullOrEmpty()) {
                emit(FetchDataState.ErrorStateString(context.getString(R.string.field_required)))
                emit(FetchDataState.DataState(false))
                return@flow
            }
            emit(FetchDataState.DataState(true))
        }

    fun generateTranslation(
        translation: String,
        example: String?,
        category: TranslationCategory?
    ): TranslationVariant {
        return TranslationVariant(
            _id = null,
            wordId = "",
            categoryId = category?._id,
            translation = translation.trim(),
            example = example
        )
    }

    fun updateTranslation(
        context: Context?,
        translation: String,
        example: String?,
        category: TranslationCategory?
    ) = flow<FetchDataState<Boolean>> {
//        if (context == null) {
//            emit(FetchDataState.DataState(false))
//            return@flow
//        }
//        Log.d(TAG, "updateTranslation($translation)")
//        val dictionaryId = editModel?.dictionaryId
//        editModel = TranslationVariant(
//            _id = editModel!!._id,
//            wordId = editModel!!.wordId,
//            categoryId = category?._id,
//            translation = translation.trim(),
//            example = example
//        )
//        editModel?.dictionaryId = dictionaryId
//        if (isEditMode() && editModel!!._id == null) {
//            emit(FetchDataState.DataState(true))
//            return@flow
//        }
//        if (editModel!!.dictionaryId?.isEmpty() == true) {
//            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_update_translation)))
//            emit(FetchDataState.DataState(false))
//            return@flow
//        }
//        emit(FetchDataState.StartLoadingState)
//
//        val result = getCreateTranslationsUseCase.updateTranslation(
//            editModel!!,
//            dictionaryId = editModel!!.dictionaryId ?: ""
//        )
//        emit(FetchDataState.FinishLoadingState)
//        if (!result) {
//            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_update_translation)))
//            emit(FetchDataState.DataState(false))
//        } else {
//            emit(FetchDataState.DataState(true))
//        }
    }

    fun isEditMode() = editModel != null

    fun getEditModel() = editModel

    fun saveTranslation(value: String?) {
        uiStateHandle[KEY_STATE_TRANSLATION] = value
    }

    fun saveExample(value: String?) {
        uiStateHandle[KEY_STATE_EXAMPLE] = value
    }

    fun saveCategory(value: Int?) {
        uiStateHandle[KEY_STATE_CATEGORY] = value
    }

    fun setDictionaryId(dictionaryId: Int) {
        this.dictionaryId = dictionaryId
    }

}
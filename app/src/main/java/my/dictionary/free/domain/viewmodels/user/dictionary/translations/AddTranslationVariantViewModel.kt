package my.dictionary.free.domain.viewmodels.user.dictionary.translations

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.words.variants.TranslationCategory
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import javax.inject.Inject

@HiltViewModel
class AddTranslationVariantViewModel @Inject constructor(
    private val getCreateTranslationCategoriesUseCase: GetCreateTranslationCategoriesUseCase,
) : ViewModel() {

    companion object {
        private val TAG = AddTranslationVariantViewModel::class.simpleName
    }

    val categoriesUIState: MutableSharedFlow<TranslationCategory> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val validateTranslation: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val _shouldClearCategoriesUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val shouldClearCategoriesUIState: StateFlow<Boolean> =
        _shouldClearCategoriesUIState.asStateFlow()

    private val _successCreateCategoryUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val successCreateCategoryUIState: StateFlow<Boolean> =
        _successCreateCategoryUIState.asStateFlow()

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    fun loadCategories(context: Context?) {
        if (context == null) return
        Log.d(TAG, "loadCategories()")
        viewModelScope.launch {
            getCreateTranslationCategoriesUseCase.getCategories()
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _displayErrorUIState.value =
                        it.message ?: context.getString(R.string.unknown_error)
                }
                .onStart {
                    Log.d(TAG, "onStart")
                    _shouldClearCategoriesUIState.value = true
                    _loadingUIState.value = true
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingUIState.value = false
                    _shouldClearCategoriesUIState.value = false
                }
                .collect {
                    Log.d(
                        TAG,
                        "category loaded = ${it.categoryName}"
                    )
                    categoriesUIState.tryEmit(it)
                }
        }
    }

    fun createCategory(context: Context?, categoryName: String?) {
        if (context == null) return
        if (categoryName.isNullOrEmpty()) return
        Log.d(TAG, "createCategory($categoryName)")
        viewModelScope.launch {
            _successCreateCategoryUIState.value = false
            val result = getCreateTranslationCategoriesUseCase.createCategory(categoryName)
            if (!result.first) {
                val error = result.second ?: context.getString(R.string.error_create_dictionary)
                _displayErrorUIState.value = error
            } else {
                _successCreateCategoryUIState.value = true
            }
        }
    }

    fun validateTranslation(context: Context?, translation: String?): Boolean {
        if (context == null) return false
        if (translation.isNullOrEmpty()) {
            validateTranslation.tryEmit(context.getString(R.string.field_required))
            return false
        }
        return true
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

}
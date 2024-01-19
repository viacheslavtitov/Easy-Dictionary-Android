package my.dictionary.free.domain.viewmodels.user.dictionary.translations

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.words.variants.TranslationCategory
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationsUseCase
import javax.inject.Inject

@HiltViewModel
class AddTranslationVariantViewModel @Inject constructor(
    private val getCreateTranslationCategoriesUseCase: GetCreateTranslationCategoriesUseCase,
    private val getCreateTranslationsUseCase: GetCreateTranslationsUseCase,
    private val uiStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private val TAG = AddTranslationVariantViewModel::class.simpleName
        private const val KEY_STATE_TRANSLATION = "translation"
        private const val KEY_STATE_EXAMPLE = "example"
        private const val KEY_STATE_CATEGORY = "category"
    }

    val translationSavedUIState: StateFlow<String> = uiStateHandle.getStateFlow(KEY_STATE_TRANSLATION, "")
    val exampleSavedUIState: StateFlow<String> = uiStateHandle.getStateFlow(KEY_STATE_EXAMPLE, "")
    val categorySavedUIState: StateFlow<Int> = uiStateHandle.getStateFlow(KEY_STATE_CATEGORY, -1)

    private val _categoriesUIState = Channel<TranslationCategory>()
    val categoriesUIState: StateFlow<TranslationCategory> = _categoriesUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TranslationCategory.empty())

    private val _validateTranslation = Channel<String>()
    val validateTranslation: StateFlow<String> = _validateTranslation.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

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

    private val _displayErrorUIState = Channel<String>()
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    //edit flows
    private val _updateUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val updateUIState: StateFlow<Boolean> = _updateUIState.asStateFlow()

    private val _exampleUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val exampleUIState: StateFlow<String> = _exampleUIState.asStateFlow()

    private val _translationUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val translationUIState: StateFlow<String> = _translationUIState.asStateFlow()

    private val _categoryUIState = Channel<TranslationCategory>()
    val categoryUIState: StateFlow<TranslationCategory> = _categoryUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TranslationCategory.empty())

    private var editModel: TranslationVariant? = null

    fun loadCategories(context: Context?) {
        if (context == null) return
        Log.d(TAG, "loadCategories()")
        viewModelScope.launch {
            val categories = arrayListOf<TranslationCategory>()
            getCreateTranslationCategoriesUseCase.getCategories()
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _displayErrorUIState.send(
                        it.message ?: context.getString(R.string.unknown_error))
                }
                .onStart {
                    Log.d(TAG, "onStart")
                    _shouldClearCategoriesUIState.value = true
                    _loadingUIState.value = true
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    editModel?.categoryId.let { id ->
                        val existCategory = categories.find { it._id == id }
                        existCategory?.let { category ->
                            editModel?.category = category
                            Log.d(TAG, "found category $category")
                            _categoryUIState.send(category)
                        }
                    }
                    _loadingUIState.value = false
                    _shouldClearCategoriesUIState.value = false
                }
                .collect {
                    Log.d(
                        TAG,
                        "category loaded = ${it.categoryName}"
                    )
                    categories.add(it)
                    _categoriesUIState.send(it)
                }
        }
    }

    fun setEditModel(translationVariant: TranslationVariant?) {
        editModel = translationVariant
        translationVariant?.let { translation ->
            viewModelScope.launch {
                Log.d(TAG, "load exist model $translation")
                _translationUIState.value = translation.translation
                translation.example?.let {
                    _exampleUIState.value = it
                }
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
                _displayErrorUIState.send(error)
            } else {
                _successCreateCategoryUIState.value = true
            }
        }
    }

    fun validateTranslation(context: Context?, translation: String?): Boolean {
        if (context == null) return false
        if (translation.isNullOrEmpty()) {
            viewModelScope.launch {
                _validateTranslation.send(context.getString(R.string.field_required))
            }
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

    fun updateTranslation(
        context: Context?,
        translation: String,
        example: String?,
        category: TranslationCategory?
    ) {
        if (context == null) return
        Log.d(TAG, "updateTranslation($translation)")
        _updateUIState.value = false
        if(isEditMode() && editModel!!._id == null) {
            editModel = TranslationVariant(
                _id = editModel!!._id,
                wordId = editModel!!.wordId,
                categoryId = category?._id,
                translation = translation.trim(),
                example = example
            )
            _updateUIState.value = true
            return
        }
        viewModelScope.launch {
            _loadingUIState.value = true
            _updateUIState.value = false
            val result = getCreateTranslationsUseCase.updateTranslation(
                translation = TranslationVariant(
                    _id = editModel!!._id,
                    wordId = editModel!!.wordId,
                    categoryId = category?._id,
                    translation = translation.trim(),
                    example = example
                ),
                dictionaryId = editModel!!.dictionaryId ?: ""
            )
            _loadingUIState.value = false
            if (!result) {
                _displayErrorUIState.send(context.getString(R.string.error_update_translation))
            } else {
                _updateUIState.value = true
            }
        }
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

}
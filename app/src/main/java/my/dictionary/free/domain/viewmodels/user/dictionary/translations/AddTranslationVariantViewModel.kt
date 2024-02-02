package my.dictionary.free.domain.viewmodels.user.dictionary.translations

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import my.dictionary.free.R
import my.dictionary.free.domain.models.words.variants.TranslationCategory
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationsUseCase
import my.dictionary.free.view.FetchDataState
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

    private var editModel: TranslationVariant? = null

    fun loadCategories(context: Context?) = flow<FetchDataState<TranslationCategory>> {
        if (context == null) return@flow
        Log.d(TAG, "loadCategories()")
        val categories = arrayListOf<TranslationCategory>()
        emit(FetchDataState.StartLoadingState)
        getCreateTranslationCategoriesUseCase.getCategories()
            .catch {
                Log.d(TAG, "catch ${it.message}")
                emit(FetchDataState.ErrorState(it))
            }
            .onCompletion {
                Log.d(TAG, "onCompletion")
                emit(FetchDataState.FinishLoadingState)
            }
            .collect {
                Log.d(
                    TAG,
                    "category loaded = ${it.categoryName}"
                )
                categories.add(it)
                emit(FetchDataState.DataState(it))
            }
    }

    fun setEditModel(translationVariant: TranslationVariant?) {
        editModel = translationVariant
        Log.d(TAG, "load exist model $translationVariant")
    }

    fun getTranslation() = getEditModel()?.translation
    fun getExample() = getEditModel()?.example

    fun createCategory(context: Context?, categoryName: String?) = flow<FetchDataState<Boolean>> {
        if (context == null) return@flow
        if (categoryName.isNullOrEmpty()) return@flow
        Log.d(TAG, "createCategory($categoryName)")
        emit(FetchDataState.StartLoadingState)
        val result = getCreateTranslationCategoriesUseCase.createCategory(categoryName)
        if (!result.first) {
            val error = result.second ?: context.getString(R.string.error_create_dictionary)
            emit(FetchDataState.ErrorStateString(error))
            emit(FetchDataState.DataState(false))
        } else {
            emit(FetchDataState.DataState(true))
        }
        emit(FetchDataState.FinishLoadingState)
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
        if (context == null) {
            emit(FetchDataState.DataState(false))
            return@flow
        }
        Log.d(TAG, "updateTranslation($translation)")
        if (isEditMode() && editModel!!._id == null) {
            editModel = TranslationVariant(
                _id = editModel!!._id,
                wordId = editModel!!.wordId,
                categoryId = category?._id,
                translation = translation.trim(),
                example = example
            )
            emit(FetchDataState.DataState(true))
            return@flow
        }
        emit(FetchDataState.StartLoadingState)
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
        emit(FetchDataState.FinishLoadingState)
        if (!result) {
            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_update_translation)))
            emit(FetchDataState.DataState(false))
        } else {
            emit(FetchDataState.DataState(true))
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
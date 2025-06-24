package org.easy.dictionary.app.domain.viewmodels.user.dictionary.words.filter

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import org.easy.dictionary.app.domain.models.dictionary.Dictionary
import org.easy.dictionary.app.domain.models.words.variants.TranslationCategory
import org.easy.dictionary.app.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import org.easy.dictionary.app.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class DictionaryWordsFilterViewModel @Inject constructor(
    private val getCreateTranslationCategoriesUseCase: GetCreateTranslationCategoriesUseCase
) : ViewModel() {
    companion object {
        private val TAG = DictionaryWordsFilterViewModel::class.simpleName
    }

    fun loadCategories(context: Context?, dictionary: Dictionary?) =
        flow<FetchDataState<TranslationCategory>> {
            if (context == null) {
                return@flow
            }
            if (dictionary == null) {
                return@flow
            }
            Log.d(TAG, "loadCategories()")
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
                    emit(FetchDataState.DataState(it))
                }
        }
}
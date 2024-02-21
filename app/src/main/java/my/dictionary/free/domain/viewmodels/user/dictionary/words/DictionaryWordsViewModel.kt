package my.dictionary.free.domain.viewmodels.user.dictionary.words

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import my.dictionary.free.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class DictionaryWordsViewModel @Inject constructor(
    private val wordsUseCase: WordsUseCase,
    private val getCreateDictionaryUseCase: GetCreateDictionaryUseCase,
    private val getCreateTranslationCategoriesUseCase: GetCreateTranslationCategoriesUseCase,
) : ViewModel() {

    companion object {
        private val TAG = DictionaryWordsViewModel::class.simpleName
    }

    val titleUIState: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var dictionary: Dictionary? = null

    fun loadWords(context: Context?, dictionaryId: String?) = flow<FetchDataState<Word>> {
        if (context == null) {
            return@flow
        }
        if (dictionaryId.isNullOrEmpty()) {
            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_load_data)))
            return@flow
        }
        Log.d(TAG, "loadWords()")
        emit(FetchDataState.StartLoadingState)
        wordsUseCase.getWordsByDictionaryId(dictionaryId)
            .catch {
                Log.e(TAG, "catch ${it.message}")
                emit(FetchDataState.ErrorState(it))
            }.onCompletion {
                Log.d(TAG, "onCompletion")
                if (dictionary == null) {
                    getCreateDictionaryUseCase.getDictionaryById(context, dictionaryId)
                        .catch {
                            Log.e(TAG, "catch ${it.message}")
                            emit(FetchDataState.ErrorState(it))
                        }
                        .onCompletion {
                            Log.d(TAG, "onCompletion")
                            emit(FetchDataState.FinishLoadingState)
                        }
                        .collect {
                            Log.d(
                                TAG,
                                "collect dictionary ${it.dictionaryFrom.lang} - ${it.dictionaryTo.lang}"
                            )
                            dictionary = it
                            titleUIState.tryEmit(
                                "${it.dictionaryFrom.langFull} - ${it.dictionaryTo.langFull}"
                            )
                        }
                } else {
                    emit(FetchDataState.FinishLoadingState)
                    dictionary?.let {
                        titleUIState.tryEmit(
                            "${it.dictionaryFrom.langFull} - ${it.dictionaryTo.langFull}"
                        )
                    }
                }
            }
            .collect { words ->
                words.forEach {
                    for (translation in it.translates) {
                        if (translation.categoryId != null) {
                            translation.category =
                                getCreateTranslationCategoriesUseCase.getDirectCategoryById(
                                    translation.categoryId
                                )
                        }
                    }
                    Log.d(
                        TAG,
                        "collect word ${it.original} | translates ${it.translates.size} | tags ${it.tags.size}"
                    )
                    emit(FetchDataState.DataState(it))
                }
            }
    }

    fun deleteWords(context: Context?, words: List<Word>?) = flow<FetchDataState<Nothing>> {
        if (context == null || words.isNullOrEmpty()) {
            return@flow
        }
        val dictionaryId = dictionary?._id ?: return@flow
        Log.d(TAG, "deleteWords(${words.size})")
        emit(FetchDataState.StartLoadingState)
        val result = wordsUseCase.deleteWords(dictionaryId, words)
        emit(FetchDataState.FinishLoadingState)
        Log.d(TAG, "delete result is ${result.first}")
        if (!result.first) {
            val error =
                result.second ?: context.getString(R.string.error_delete_word)
            emit(FetchDataState.ErrorStateString(error))
        }
    }

    fun getDictionary() = dictionary

}
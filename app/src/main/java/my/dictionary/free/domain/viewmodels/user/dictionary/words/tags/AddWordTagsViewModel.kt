package my.dictionary.free.domain.viewmodels.user.dictionary.words.tags

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.WordTag
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class AddWordTagsViewModel @Inject constructor(
    private val dictionaryUseCase: GetCreateDictionaryUseCase
) : ViewModel() {
    companion object {
        private val TAG = AddWordTagsViewModel::class.simpleName
    }

    private var word: Word? = null
    private var dictionary: Dictionary? = null

    fun loadData(context: Context?, word: Word?, dictionary: Dictionary?) =
        flow<FetchDataState<Nothing>> {
            if (context == null) return@flow
            if (dictionary == null) {
                emit(FetchDataState.ErrorStateString(context.getString(R.string.error_load_data)))
                return@flow
            }
            Log.d(
                TAG,
                "loadData(${word?.original}, ${dictionary.dictionaryFrom} - ${dictionary.dictionaryTo})"
            )
            this@AddWordTagsViewModel.word = word
            this@AddWordTagsViewModel.dictionary = dictionary
        }

    fun addTag(context: Context?, tagName: String?) = flow<FetchDataState<WordTag>> {
        if (context == null) return@flow
        if (dictionary == null) return@flow
        if (tagName.isNullOrEmpty()) return@flow
        if (dictionary?.tags?.find { it.tagName == tagName } != null) {
            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_tag_exist)))
            return@flow
        }
        Log.d(TAG, "addTag($tagName)")
        emit(FetchDataState.StartLoadingState)
        val createdTagResult = dictionaryUseCase.createDictionaryTag(dictionary!!, tagName)
        Log.d(TAG, "created tag result is ${createdTagResult.first}")
        if (!createdTagResult.first) {
            val error =
                createdTagResult.second ?: context.getString(R.string.error_delete_word)
            emit(FetchDataState.ErrorStateString(error))
        } else {
            val tag = WordTag(_id = createdTagResult.second, userUUID = "", tagName = tagName)
            dictionary?.tags?.add(tag)
            emit(FetchDataState.DataState(tag))
        }
        emit(FetchDataState.FinishLoadingState)
    }
}
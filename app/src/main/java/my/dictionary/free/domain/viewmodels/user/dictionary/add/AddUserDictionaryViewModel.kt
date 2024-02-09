package my.dictionary.free.domain.viewmodels.user.dictionary.add

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.dictionary.DictionaryItem
import my.dictionary.free.domain.models.language.Flags
import my.dictionary.free.domain.models.language.Language
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.usecases.languages.GetDictionaryLanguagesUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
import my.dictionary.free.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class AddUserDictionaryViewModel @Inject constructor(
    private val dictionaryUseCase: GetCreateDictionaryUseCase,
    private val languagesUseCase: GetDictionaryLanguagesUseCase,
    private val preferenceUtils: PreferenceUtils,
    private val uiStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private val TAG = AddUserDictionaryViewModel::class.simpleName
        private const val KEY_STATE_LANGUAGE_FROM = "languageFrom"
        private const val KEY_STATE_LANGUAGE_TO = "languageTo"
        private const val KEY_STATE_DIALECT = "dialect"
    }

    private var languageFrom: Language? = null
    private var languageTo: Language? = null
    private var dialect: String? = null
    private var editDictionary: Dictionary? = null

    val languageFromSavedUIState: StateFlow<Language> = uiStateHandle.getStateFlow(
        KEY_STATE_LANGUAGE_FROM, Language("", "", Flags("", ""))
    )
    val languageToSavedUIState: StateFlow<Language> = uiStateHandle.getStateFlow(
        KEY_STATE_LANGUAGE_TO, Language("", "", Flags("", ""))
    )
    val dialectSavedUIState: StateFlow<String> = uiStateHandle.getStateFlow(
        KEY_STATE_DIALECT, ""
    )

    fun createDictionary(context: Context?, dialectValue: String? = null) =
        flow<FetchDataState<Boolean>> {
            if (languageFrom == null || languageTo == null || context == null) return@flow
            val userUUID =
                preferenceUtils.getString(PreferenceUtils.CURRENT_USER_UUID) ?: return@flow
            emit(FetchDataState.StartLoadingState)
            if (isEditMode()) {
                val result = dictionaryUseCase.updateDictionary(
                    Dictionary(
                        _id = editDictionary?._id,
                        userUUID = userUUID,
                        dictionaryFrom = DictionaryItem(
                            lang = languageFrom!!.key
                        ),
                        dictionaryTo = DictionaryItem(
                            lang = languageTo!!.key
                        ),
                        dialect = dialectValue ?: ""
                    )
                )
                if (!result) {
                    emit(FetchDataState.ErrorStateString(context.getString(R.string.error_update_dictionary)))
                }
                emit(FetchDataState.DataState(result))
            } else {
                val result = dictionaryUseCase.createDictionary(
                    Dictionary(
                        userUUID = userUUID,
                        dictionaryFrom = DictionaryItem(
                            lang = languageFrom!!.key
                        ),
                        dictionaryTo = DictionaryItem(
                            lang = languageTo!!.key
                        ),
                        dialect = dialectValue ?: ""
                    )
                )
                if (!result.first) {
                    emit(FetchDataState.ErrorStateString(context.getString(R.string.error_create_dictionary)))
                }
                emit(FetchDataState.DataState(result.first))
            }
            emit(FetchDataState.FinishLoadingState)
        }

    fun isEditMode() = editDictionary != null

    fun setDictionary(context: Context?, dictionary: Dictionary?) {
        if (context == null) return
        Log.d(TAG, "load passed dictionary $dictionary")
        editDictionary = dictionary
        dictionary?.let { dict ->
            viewModelScope.launch {
                saveLangFrom(languagesUseCase.findLanguageByKey(context, dict.dictionaryFrom.lang))
                saveLanguageTo(languagesUseCase.findLanguageByKey(context, dict.dictionaryTo.lang))
                if (dict.dialect?.isNullOrEmpty() == false) {
                    saveDialect(dict.dialect)
                    Log.d(TAG, "emit dialect ${dict.dialect}")
                }
            }
        }
    }

    fun saveLangFrom(value: Language?) {
        languageFrom = value
        uiStateHandle[KEY_STATE_LANGUAGE_FROM] = value
    }

    fun saveLanguageTo(value: Language?) {
        languageTo = value
        uiStateHandle[KEY_STATE_LANGUAGE_TO] = value
    }

    fun saveDialect(value: String?) {
        dialect = value
        uiStateHandle[KEY_STATE_DIALECT] = value
    }

}
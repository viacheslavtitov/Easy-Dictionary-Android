package my.dictionary.free.domain.viewmodels.user.dictionary.add

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.dictionary.DictionaryItem
import my.dictionary.free.domain.models.dictionary.VerbTense
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
        private const val KEY_STATE_TENSES = "tenses"
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
    val tensesSavedUIState: StateFlow<List<VerbTense>> = uiStateHandle.getStateFlow(
        KEY_STATE_TENSES, emptyList()
    )

    fun createDictionary(context: Context?, dialectValue: String? = null, tenses: List<VerbTense>) =
        flow<FetchDataState<Boolean>> {
            if (languageFrom == null || languageTo == null || context == null) return@flow
            val userUUID =
                preferenceUtils.getString(PreferenceUtils.CURRENT_USER_UUID) ?: return@flow
            emit(FetchDataState.StartLoadingState)
            val editedTense = mutableListOf<VerbTense>()
            editedTense.addAll(tenses)
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
                        dialect = dialectValue ?: "",
                        tenses = editedTense
                    )
                )
                if (!result) {
                    emit(FetchDataState.ErrorStateString(context.getString(R.string.error_update_dictionary)))
                } else {
                    editDictionary?.let { dict ->
                        val shouldDeleteTenseVerbs = arrayListOf<VerbTense>()
                        dict.tenses.forEach { verb ->
                            val exist = tenses.find { it._id == verb._id && it.name == verb.name }
                            if (exist == null) {
                                shouldDeleteTenseVerbs.add(verb)
                            }
                        }
                        if (shouldDeleteTenseVerbs.isNotEmpty()) {
                            dictionaryUseCase.deleteVerbTenseFromDictionary(
                                dict._id!!,
                                shouldDeleteTenseVerbs
                            )
                        }
                        val shouldAddTenseVerbs = arrayListOf<VerbTense>()
                        tenses.forEach { verb ->
                            if (verb._id == null) {
                                shouldAddTenseVerbs.add(verb)
                            }
                        }
                        if (shouldAddTenseVerbs.isNotEmpty()) {
                            dictionaryUseCase.addVerbTenseToDictionary(
                                dict._id!!,
                                shouldAddTenseVerbs.maxOf { it.name }
                            )
                        }
                    }
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
                        dialect = dialectValue ?: "",
                        tenses = editedTense
                    )
                )
                if (result.first == null) {
                    emit(FetchDataState.ErrorStateString(context.getString(R.string.error_create_dictionary)))
                    emit(FetchDataState.DataState(false))
                } else {
                    result.first?.let { dictionaryId ->
                        editedTense.forEach {
                            val verbResult =
                                dictionaryUseCase.addVerbTenseToDictionary(dictionaryId, it.name)
                            Log.d(
                                TAG,
                                "verb tense ${it.name} was added ${verbResult.first} ${verbResult.second}"
                            )
                        }
                    }
                    emit(FetchDataState.DataState(true))
                }
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
                saveVerbTenses(dict.tenses)
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

    fun saveVerbTenses(tenses: List<VerbTense>) {
        uiStateHandle[KEY_STATE_TENSES] = tenses
    }

}
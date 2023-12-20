package my.dictionary.free.domain.viewmodels.user.dictionary.add

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.dictionary.DictionaryItem
import my.dictionary.free.domain.models.language.Language
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.usecases.languages.GetDictionaryLanguagesUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject

@HiltViewModel
class AddUserDictionaryViewModel @Inject constructor(
    private val dictionaryUseCase: GetCreateDictionaryUseCase,
    private val languagesUseCase: GetDictionaryLanguagesUseCase,
    private val preferenceUtils: PreferenceUtils
) : ViewModel() {

    var languageFrom: Language? = null
    var languageTo: Language? = null
    var dialect: String? = null
    private var editDictionary: Dictionary? = null

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    private val _successCreateDictionaryUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val successCreateDictionaryUIState: StateFlow<Boolean> = _successCreateDictionaryUIState.asStateFlow()

    private val _langFromUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val langFromUIState: StateFlow<String> = _langFromUIState.asStateFlow()

    private val _langToUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val langToUIState: StateFlow<String> = _langToUIState.asStateFlow()

    private val _dialectUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val dialectUIState: StateFlow<String> = _dialectUIState.asStateFlow()

    fun createDictionary(context: Context?, dialectValue: String? = null) {
        if (languageFrom == null || languageTo == null || context == null) return
        val userUUID = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_UUID) ?: return
        viewModelScope.launch {
            _successCreateDictionaryUIState.value = false
            if(isEditMode()) {
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
                    val error = context.getString(R.string.error_update_dictionary)
                    _displayErrorUIState.value = error
                } else {
                    _successCreateDictionaryUIState.value = true
                }
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
                    val error = result.second ?: context.getString(R.string.error_create_dictionary)
                    _displayErrorUIState.value = error
                } else {
                    _successCreateDictionaryUIState.value = true
                }
            }
        }
    }

    fun isEditMode() = editDictionary != null

    fun setDictionary(context: Context?, dictionary: Dictionary?) {
        if(context == null) return
        editDictionary = dictionary
        dictionary?.let { dict ->
            viewModelScope.launch {
                languageFrom = languagesUseCase.findLanguageByKey(context, dict.dictionaryFrom.lang)
                languageTo = languagesUseCase.findLanguageByKey(context, dict.dictionaryTo.lang)
                languageFrom?.value?.let {
                    _langFromUIState.value = it
                }
                languageTo?.value?.let {
                    _langToUIState.value = it
                }
                if(dict.dialect?.isNullOrEmpty() == false) {
                    dialect = dict.dialect
                    _dialectUIState.value = dict.dialect
                }
            }
        }
    }

}
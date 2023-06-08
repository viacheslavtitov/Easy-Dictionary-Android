package my.dictionary.free.domain.viewmodels.user.dictionary.add

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.dictionary.DictionaryItem
import my.dictionary.free.domain.models.language.Language
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject

@HiltViewModel
class AddUserDictionaryViewModel @Inject constructor() : ViewModel() {

    var languageFrom: Language? = null
    var languageTo: Language? = null
    var dialect: String? = null

    @Inject
    lateinit var dictionaryUseCase: GetCreateDictionaryUseCase

    @Inject
    lateinit var preferenceUtils: PreferenceUtils

    fun createDictionary(dialectValue: String? = null) {
        if (languageFrom == null || languageTo == null) return
        val userUUID = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_UUID) ?: return
        CoroutineScope(Dispatchers.IO).launch {
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
        }
    }

}
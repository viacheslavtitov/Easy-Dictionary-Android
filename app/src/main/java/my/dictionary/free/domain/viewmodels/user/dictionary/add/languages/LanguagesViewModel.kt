package my.dictionary.free.domain.viewmodels.user.dictionary.add.languages

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.dictionary.free.domain.models.language.Language
import my.dictionary.free.domain.usecases.languages.GetDictionaryLanguagesUseCase
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject constructor() : ViewModel() {

    val languages = MutableLiveData<List<Language>>()

    @Inject
    lateinit var languagesUseCase: GetDictionaryLanguagesUseCase

    fun loadLanguages(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val languageList = languagesUseCase.getLanguages(context)
            withContext(Dispatchers.Main) {
                languages.value = languageList
            }
        }
    }

    fun queryLanguages(context: Context, query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val languageList = languagesUseCase.getLanguages(context, query)
            withContext(Dispatchers.Main) {
                languages.value = languageList
            }
        }
    }
}
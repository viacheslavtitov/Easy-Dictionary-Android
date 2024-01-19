package my.dictionary.free.domain.viewmodels.user.dictionary.add.languages

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.dictionary.free.domain.models.language.Language
import my.dictionary.free.domain.usecases.languages.GetDictionaryLanguagesUseCase
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    private val languagesUseCase: GetDictionaryLanguagesUseCase
) : ViewModel() {

    private val _languages = Channel<List<Language>>()
    val languages = _languages.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun loadLanguages(context: Context) {
        viewModelScope.launch {
            val languageList = languagesUseCase.getLanguages(context)
            _languages.send(languageList)
        }
    }

    fun queryLanguages(context: Context, query: String) {
        viewModelScope.launch {
            val languageList = languagesUseCase.getLanguages(context, query)
            _languages.send(languageList)
        }
    }
}
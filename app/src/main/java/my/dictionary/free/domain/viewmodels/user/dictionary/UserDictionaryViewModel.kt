package my.dictionary.free.domain.viewmodels.user.dictionary

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import javax.inject.Inject

@HiltViewModel
class UserDictionaryViewModel @Inject constructor() : ViewModel() {

    val dictionaries = MutableLiveData<List<Dictionary>>()

    @Inject
    lateinit var dictionaryUseCase: GetCreateDictionaryUseCase

    fun loadDictionaries(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val dictionaryList = dictionaryUseCase.getDictionaries(context)
            withContext(Dispatchers.Main) {
                dictionaries.value = dictionaryList
            }
        }
    }

}
package my.dictionary.free.domain.viewmodels.user.dictionary

import android.util.Log
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

    val languages = MutableLiveData<List<Dictionary>>()

    @Inject
    lateinit var dictionaryUseCase: GetCreateDictionaryUseCase

    fun loadDictionaries() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = dictionaryUseCase.getDictionaries()
            withContext(Dispatchers.Main) {
                result.forEach {
                    Log.d("MyTag", "from=${it.langFrom} to=${it.langTo}")
                }
                languages.value = result
            }
        }
    }

}
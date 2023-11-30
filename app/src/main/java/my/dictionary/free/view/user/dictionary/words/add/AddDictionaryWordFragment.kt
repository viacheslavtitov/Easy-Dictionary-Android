package my.dictionary.free.view.user.dictionary.words.add

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.words.DictionaryWordsFragment

@AndroidEntryPoint
class AddDictionaryWordFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = AddDictionaryWordFragment::class.simpleName
        const val BUNDLE_DICTIONARY_ID =
            "my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment.BUNDLE_DICTIONARY_ID"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: AddDictionaryWordViewModel by viewModels()

    private lateinit var translationsRecyclerView: RecyclerView
    private lateinit var textInputLayoutWord: TextInputLayout
    private lateinit var textInputEditTextWord: TextInputEditText
    private lateinit var textInputLayoutPhonetic: TextInputLayout
    private lateinit var textInputEditTextPhonetic: TextInputEditText

    private var dictionaryId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_new_word, null)
        translationsRecyclerView = view.findViewById(R.id.translations_recycler_view)
        textInputLayoutWord = view.findViewById(R.id.text_input_word)
        textInputEditTextWord = view.findViewById(R.id.edit_text_word)
        textInputLayoutPhonetic = view.findViewById(R.id.text_input_phonetic)
        textInputEditTextPhonetic = view.findViewById(R.id.edit_text_phonetic)
        view.findViewById<View>(R.id.add_variants_container).setOnClickListener {

        }
        textInputLayoutPhonetic.setEndIconOnClickListener {

        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.displayErrorUIState.drop(1).collect { errorMessage ->
                        displayError(errorMessage, translationsRecyclerView)
                    }
                }
                launch {
                    viewModel.loadingUIState.collect { visible ->

                    }
                }
                launch {
                    viewModel.phoneticsUIState.drop(1).collectLatest { phonetics ->
                        Log.d(TAG, "phonetics updated: ${phonetics.size}")
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_add_dictionary_word, { menu, mi -> }, {
            when (it) {
                R.id.nav_save_word -> {
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        dictionaryId = arguments?.getString(BUNDLE_DICTIONARY_ID, null)
        viewModel.loadData(context, dictionaryId)
    }

}
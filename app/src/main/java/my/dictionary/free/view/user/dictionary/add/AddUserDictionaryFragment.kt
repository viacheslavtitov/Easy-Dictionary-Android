package my.dictionary.free.view.user.dictionary.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.language.LangType
import my.dictionary.free.domain.models.language.Language
import my.dictionary.free.domain.models.navigation.LanguagesScreen
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.add.AddUserDictionaryViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.add.languages.LanguagesFragment

@AndroidEntryPoint
class AddUserDictionaryFragment : AbstractBaseFragment() {

    companion object {
        const val BUNDLE_DICTIONARY_CREATED_KEY =
            "my.dictionary.free.view.user.dictionary.add.AddUserDictionaryFragment.BUNDLE_DICTIONARY_CREATED_KEY"
        const val BUNDLE_DICTIONARY_CREATED_RESULT =
            "my.dictionary.free.view.user.dictionary.add.AddUserDictionaryFragment.BUNDLE_DICTIONARY_CREATED_RESULT"
        const val BUNDLE_DICTIONARY =
            "my.dictionary.free.view.user.dictionary.add.AddUserDictionaryFragment.BUNDLE_DICTIONARY"
    }

    private lateinit var textInputLayoutLangFrom: TextInputLayout
    private lateinit var textInputLayoutLangTo: TextInputLayout
    private lateinit var textInputLayoutDialect: TextInputLayout
    private lateinit var textInputEditTextDialect: TextInputEditText
    private lateinit var langFromBtn: Button
    private lateinit var langToBtn: Button

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: AddUserDictionaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_new_dictionary, null)
        textInputLayoutLangFrom = view.findViewById(R.id.text_input_lang_from)
        textInputLayoutLangTo = view.findViewById(R.id.text_input_lang_to)
        textInputLayoutDialect = view.findViewById(R.id.text_input_dialect)
        textInputEditTextDialect = view.findViewById(R.id.edit_text_dialect)
        langFromBtn = view.findViewById(R.id.btn_lang_from)
        langFromBtn.setOnClickListener {
            sharedViewModel.navigateTo(LanguagesScreen(LangType.FROM))
        }
        langToBtn = view.findViewById(R.id.btn_lang_to)
        langToBtn.setOnClickListener {
            sharedViewModel.navigateTo(LanguagesScreen(LangType.TO))
        }
        fillData()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.displayErrorUIState.drop(1).collect { errorMessage ->
                        val aboveView = getView() ?: return@collect
                        displayError(errorMessage, aboveView)
                    }
                }
                launch {
                    viewModel.successCreateDictionaryUIState.collect { success ->
                        if (success) {
                            val bundle = Bundle().apply {
                                putBoolean(BUNDLE_DICTIONARY_CREATED_KEY, true)
                            }
                            setFragmentResult(BUNDLE_DICTIONARY_CREATED_RESULT, bundle)
                            findNavController().popBackStack()
                        }
                    }
                }
                launch {
                    viewModel.langFromUIState.collect { value ->
                        if(value.isNotEmpty()) {
                            langFromBtn.text = value
                        }
                    }
                }
                launch {
                    viewModel.loadingUIState.collect { visible ->
                        sharedViewModel.loading(visible)
                    }
                }
                launch {
                    viewModel.langToUIState.collect { value ->
                        if(value.isNotEmpty()) {
                            langToBtn.text = value
                        }
                    }
                }
                launch {
                    viewModel.dialectUIState.collect { value ->
                        if(value.isNotEmpty()) {
                            textInputEditTextDialect.setText(value)
                        }
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_add_user_dictionary, { menu, mi -> }, {
            when (it) {
                R.id.nav_save_dictionary -> {
                    createDictionary()
                    return@addMenuProvider true
                }

                else -> false
            }
        })

        val dictionary = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_DICTIONARY,
            Dictionary::class.java
        ) else arguments?.getParcelable(BUNDLE_DICTIONARY) as? Dictionary
        viewModel.setDictionary(context, dictionary)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(LanguagesFragment.BUNDLE_LANGUAGE_SELECT_RESULT) { requestKey, bundle ->
            val language = if (hasTiramisu()) bundle.getParcelable(
                LanguagesFragment.BUNDLE_LANGUAGE_SELECT_KEY,
                Language::class.java
            ) else bundle.getParcelable(LanguagesFragment.BUNDLE_LANGUAGE_SELECT_KEY) as? Language
            val langType = bundle.getInt(LanguagesFragment.BUNDLE_LANGUAGE_TYPE_KEY, 0)
            val languageType =
                LangType.values().firstOrNull { it.ordinal == langType }
                    ?: LangType.FROM
            when (languageType) {
                LangType.FROM -> {
                    langFromBtn.text = language?.value
                    viewModel.languageFrom = language
                }

                LangType.TO -> {
                    langToBtn.text = language?.value
                    viewModel.languageTo = language
                }
            }
            viewModel.dialect = textInputEditTextDialect.text?.toString()
        }
    }

    private fun fillData() {
        langFromBtn.text = viewModel.languageFrom?.value ?: getString(R.string.select_language_from)
        langToBtn.text = viewModel.languageTo?.value ?: getString(R.string.select_language_to)
        textInputEditTextDialect.setText(viewModel.dialect)
    }

    private fun createDictionary() {
        val dialect = textInputEditTextDialect.text?.toString()
        viewModel.createDictionary(context, dialect)
    }
}
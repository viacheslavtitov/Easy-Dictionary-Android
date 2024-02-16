package my.dictionary.free.view.user.dictionary.add

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.dictionary.VerbTense
import my.dictionary.free.domain.models.language.LangType
import my.dictionary.free.domain.models.language.Language
import my.dictionary.free.domain.models.navigation.LanguagesScreen
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.add.AddUserDictionaryViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.FetchDataState
import my.dictionary.free.view.dialogs.DialogBuilders
import my.dictionary.free.view.dialogs.InputDialogListener
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.ext.findAndDismissDialog
import my.dictionary.free.view.user.dictionary.add.languages.LanguagesFragment

@AndroidEntryPoint
class AddUserDictionaryFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = AddUserDictionaryFragment::class.simpleName
        const val BUNDLE_DICTIONARY =
            "my.dictionary.free.view.user.dictionary.add.AddUserDictionaryFragment.BUNDLE_DICTIONARY"
    }

    private lateinit var textInputLayoutLangFrom: TextInputLayout
    private lateinit var textInputLayoutLangTo: TextInputLayout
    private lateinit var textInputLayoutDialect: TextInputLayout
    private lateinit var textInputEditTextDialect: TextInputEditText
    private lateinit var langFromBtn: Button
    private lateinit var langToBtn: Button
    private lateinit var rootView: View
    private lateinit var verbTensesRecyclerView: RecyclerView

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: AddUserDictionaryViewModel by viewModels()

    private var verbTensesAdapter: VerbTensesAdapter? = null

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
        verbTensesRecyclerView = view.findViewById(R.id.verb_tenses_recycler_view)
        rootView = view.findViewById(R.id.root)
        verbTensesAdapter = VerbTensesAdapter(arrayListOf(), onVerbTenseEditListener)
        verbTensesRecyclerView.layoutManager = LinearLayoutManager(context)
        verbTensesRecyclerView.adapter = verbTensesAdapter
        langFromBtn.setOnClickListener {
            sharedViewModel.navigateTo(LanguagesScreen(LangType.FROM))
        }
        view.findViewById<View>(R.id.btn_add_verb_time).setOnClickListener {
            val dialog = DialogBuilders.InputDialogBuilder
                .cancelButtonTitle(getString(R.string.cancel))
                .title(getString(R.string.add_tense))
                .okButtonTitle(getString(R.string.ok))
                .listener(object : InputDialogListener {
                    var tenseName: String? = null
                    override fun onTextChanged(newText: String?) {
                        tenseName = newText
                    }

                    override fun onCancelClicked() {
                    }

                    override fun onOkButtonClicked() {
                        tenseName?.let {
                            if (it.isNotEmpty()) {
                                addTense(VerbTense(null, it))
                            }
                        }
                    }
                }).build()
            childFragmentManager.findAndDismissDialog("InputDialog")
            dialog.show(childFragmentManager, "InputDialog")
        }
        langToBtn = view.findViewById(R.id.btn_lang_to)
        langToBtn.setOnClickListener {
            sharedViewModel.navigateTo(LanguagesScreen(LangType.TO))
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dialectSavedUIState.collect { value ->
                        textInputEditTextDialect.setText(value)
                    }
                }
                launch {
                    viewModel.languageFromSavedUIState.collect { language ->
                        langFromBtn.text =
                            language.value.ifEmpty { getString(R.string.select_language_from) }
                    }
                }
                launch {
                    viewModel.languageToSavedUIState.collect { language ->
                        langToBtn.text =
                            language.value.ifEmpty { getString(R.string.select_language_to) }
                    }
                }
                launch {
                    viewModel.tensesSavedUIState.collect { tenses ->
                        if(tenses.isNotEmpty()) verbTensesAdapter?.clear()
                        tenses.forEach {
                            verbTensesAdapter?.add(it)
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
                    viewModel.saveLangFrom(language)
                }

                LangType.TO -> {
                    langToBtn.text = language?.value
                    viewModel.saveLanguageTo(language)
                }
            }
            viewModel.saveDialect(textInputEditTextDialect.text?.toString())
        }
    }

    private fun createDictionary() {
        lifecycleScope.launch {
            val dialect = textInputEditTextDialect.text?.toString()
            val tenses = verbTensesAdapter?.getData() ?: arrayListOf()
            viewModel.createDictionary(context, dialect, tenses).collect {
                when (it) {
                    is FetchDataState.StartLoadingState -> {
                        sharedViewModel.loading(true)
                    }

                    is FetchDataState.FinishLoadingState -> {
                        sharedViewModel.loading(false)
                    }

                    is FetchDataState.ErrorState -> {
                        displayError(
                            it.exception.message ?: context?.getString(R.string.unknown_error),
                            rootView
                        )
                    }

                    is FetchDataState.DataState -> {
                        if (it.data) {
                            findNavController().popBackStack()
                        }
                    }

                    is FetchDataState.ErrorStateString -> {
                        displayError(it.error, rootView)
                    }
                }
            }
        }
    }

    override fun onStop() {
        val dialect = textInputEditTextDialect.text?.toString()
        viewModel.saveDialect(dialect)
        viewModel.saveVerbTenses(verbTensesAdapter?.getData() ?: emptyList())
        super.onStop()
    }

    private val onVerbTenseEditListener = object : OnVerbTenseEditListener {

        override fun onDelete(entity: VerbTense) {
            Log.d(TAG, "tense ${entity.name} was deleted")
        }

    }

    private fun addTense(entity: VerbTense) {
        verbTensesAdapter?.add(entity)
    }
}
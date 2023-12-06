package my.dictionary.free.view.user.dictionary.words.add

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.navigation.AddTranslationVariantsScreen
import my.dictionary.free.domain.models.words.variants.TranslationCategory
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.ext.hideKeyboard
import my.dictionary.free.view.widget.phonetic.OnPhoneticClickListener
import my.dictionary.free.view.widget.phonetic.PhoneticsView


@AndroidEntryPoint
class AddDictionaryWordFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = AddDictionaryWordFragment::class.simpleName
        const val BUNDLE_DICTIONARY_ID =
            "my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment.BUNDLE_DICTIONARY_ID"
        const val BUNDLE_TRANSLATION_VARIANT =
            "my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment.BUNDLE_TRANSLATION_VARIANT"
        const val BUNDLE_TRANSLATION_VARIANT_CATEGORY =
            "my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment.BUNDLE_TRANSLATION_VARIANT_CATEGORY"
        const val BUNDLE_TRANSLATION_VARIANT_RESULT =
            "my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment.BUNDLE_TRANSLATION_VARIANT_RESULT"
        private const val PHONETICS_ANIMATION_TIME: Long = 400
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: AddDictionaryWordViewModel by viewModels()

    private lateinit var translationsRecyclerView: RecyclerView
    private lateinit var textInputLayoutWord: TextInputLayout
    private lateinit var textInputEditTextWord: TextInputEditText
    private lateinit var textInputLayoutPhonetic: TextInputLayout
    private lateinit var textInputEditTextPhonetic: TextInputEditText
    private lateinit var phoneticsView: PhoneticsView
    private lateinit var rootView: ViewGroup

    private var dictionaryId: String? = null
    private var phonetics: List<String>? = null
    private val translationVariantsAdapter = TranslationVariantsAdapter()

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
        phoneticsView = view.findViewById(R.id.phonetic_view)
        rootView = view.findViewById(R.id.root)
        phoneticsView.setOnClickListener(phoneticClickListener)
        view.findViewById<View>(R.id.add_variants_container).setOnClickListener {
            context?.hideKeyboard(textInputEditTextWord)
            context?.hideKeyboard(textInputEditTextPhonetic)
            if (!phonetics.isNullOrEmpty()) {
                togglePhoneticsView(false)
            }
            sharedViewModel.navigateTo(AddTranslationVariantsScreen(textInputEditTextWord.text?.toString()))
        }
        textInputLayoutPhonetic.setEndIconOnClickListener {
            context?.hideKeyboard(textInputEditTextWord)
            context?.hideKeyboard(textInputEditTextPhonetic)
            if (!phonetics.isNullOrEmpty()) {
                togglePhoneticsView(phoneticsView.visibility == View.GONE)
            } else {
                displayError(
                    getString(R.string.error_not_found_phonetics),
                    translationsRecyclerView
                )
            }
        }
        textInputEditTextWord.onFocusChangeListener =
            OnFocusChangeListener { view, focused ->
                if (focused) {
                    if (!phonetics.isNullOrEmpty()) {
                        togglePhoneticsView(false)
                    }
                }
            }
        textInputEditTextPhonetic.onFocusChangeListener =
            OnFocusChangeListener { view, focused ->
                if (focused) {
                    if (!phonetics.isNullOrEmpty()) {
                        togglePhoneticsView(false)
                    }
                }
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
                    viewModel.validateWord.collect { error ->
                        textInputLayoutWord.error = error
                    }
                }
                launch {
                    viewModel.successCreateWordUIState.collect { success ->
                        if(success) {
                            findNavController().popBackStack()
                        }
                    }
                }
                launch {
                    viewModel.phoneticsUIState.drop(1).collectLatest { phoneticList ->
                        Log.d(TAG, "phonetics updated: ${phoneticList.size}")
                        phonetics = phoneticList
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_add_dictionary_word, { menu, mi -> }, {
            when (it) {
                R.id.nav_save_word -> {
                    val word = textInputEditTextWord.text?.toString()
                    val translations = translationVariantsAdapter.getData()
                    if(viewModel.validate(context, word, translations)) {
                        val phonetic = textInputEditTextPhonetic.text?.toString()
                        viewModel.save(context, word, translations, phonetic)
                    }
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        dictionaryId = arguments?.getString(BUNDLE_DICTIONARY_ID, null)
        translationsRecyclerView.layoutManager = LinearLayoutManager(context)
        translationsRecyclerView.adapter = translationVariantsAdapter
        viewModel.loadData(context, dictionaryId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(BUNDLE_TRANSLATION_VARIANT_RESULT) { requestKey, bundle ->
            val translation: TranslationVariant? =
                if (hasTiramisu()) bundle.getParcelable(
                    BUNDLE_TRANSLATION_VARIANT,
                    TranslationVariant::class.java
                ) else bundle.getParcelable(BUNDLE_TRANSLATION_VARIANT)
            val category: TranslationCategory? =
                if (hasTiramisu()) bundle.getParcelable(
                    BUNDLE_TRANSLATION_VARIANT_CATEGORY,
                    TranslationCategory::class.java
                ) else bundle.getParcelable(BUNDLE_TRANSLATION_VARIANT_CATEGORY)
            translation?.let {
                it.category = category
                translationVariantsAdapter.add(it)
            }
        }
    }

    private fun togglePhoneticsView(show: Boolean) {
        if (show && phoneticsView.visibility == View.VISIBLE) return
        if (!show && phoneticsView.visibility == View.GONE) return
        val transition: Transition = Slide(Gravity.BOTTOM)
        transition.duration = PHONETICS_ANIMATION_TIME
        transition.addTarget(R.id.phonetic_view)
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }

            override fun onTransitionEnd(transition: Transition) {
                phonetics?.let {
                    phoneticsView.setPhonetics(it)
                }
            }

            override fun onTransitionCancel(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }

        })
        TransitionManager.beginDelayedTransition(rootView, transition)
        phoneticsView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private val phoneticClickListener = object : OnPhoneticClickListener {
        override fun onPhoneticClick(position: Int, symbol: String) {
            val cursorPosition = textInputEditTextPhonetic.selectionStart
            val oldText = textInputEditTextPhonetic.text?.toString() ?: ""
            val textBeforeCursor = oldText.substring(0, cursorPosition)
            val textAfterCursor = oldText.substring(cursorPosition, oldText.length)
            val newText = "$textBeforeCursor$symbol$textAfterCursor"
            textInputEditTextPhonetic.setText(newText)
            textInputEditTextPhonetic.setSelection(cursorPosition + 1)
        }
    }
}
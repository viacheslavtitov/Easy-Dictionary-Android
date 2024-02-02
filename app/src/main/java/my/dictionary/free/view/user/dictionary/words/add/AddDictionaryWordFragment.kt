package my.dictionary.free.view.user.dictionary.words.add

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatSpinner
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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.navigation.AddTagNavigation
import my.dictionary.free.domain.models.navigation.AddTranslationVariantNavigation
import my.dictionary.free.domain.models.navigation.AddTranslationVariantsScreen
import my.dictionary.free.domain.models.navigation.AddWordTagsScreen
import my.dictionary.free.domain.models.navigation.EditTranslationVariantsScreen
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.WordTag
import my.dictionary.free.domain.models.words.variants.TranslationCategory
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.FetchDataState
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.ext.hideKeyboard
import my.dictionary.free.view.user.dictionary.words.tags.AddWordTagsFragment
import my.dictionary.free.view.widget.bubble.BubbleLayout
import my.dictionary.free.view.widget.bubble.BubbleView
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
        const val BUNDLE_WORD =
            "my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment.BUNDLE_WORD"
        private const val PHONETICS_ANIMATION_TIME: Long = 400
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: AddDictionaryWordViewModel by viewModels()

    private lateinit var translationsRecyclerView: RecyclerView
    private lateinit var textInputLayoutWord: TextInputLayout
    private lateinit var textInputEditTextWord: TextInputEditText
    private lateinit var textInputLayoutPhonetic: TextInputLayout
    private lateinit var textInputEditTextPhonetic: TextInputEditText
    private lateinit var spinnerChooseWordType: AppCompatSpinner
    private lateinit var phoneticsView: PhoneticsView
    private lateinit var rootView: ViewGroup
    private lateinit var tagsLayout: BubbleLayout

    private var dictionaryId: String? = null
    private var phonetics: List<String>? = null
    private var wordTypeAdapter: WordTypeSpinnerAdapter? = null

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
        spinnerChooseWordType = view.findViewById(R.id.choose_word_type)
        phoneticsView = view.findViewById(R.id.phonetic_view)
        rootView = view.findViewById(R.id.root)
        tagsLayout = view.findViewById(R.id.bubbles_layout)
        tagsLayout.setReadOnly(true)
        phoneticsView.setOnClickListener(phoneticClickListener)
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
                    viewModel.validateWord.drop(1).collect { error ->
                        textInputLayoutWord.error = error
                    }
                }
                launch {
                    viewModel.nameUIState.drop(1).collectLatest { value ->
                        textInputEditTextWord.setText(value)
                    }
                }
                launch {
                    viewModel.typeUIState.collectLatest { position ->
                        spinnerChooseWordType.setSelection(position)
                    }
                }
                launch {
                    viewModel.phoneticUIState.drop(1).collectLatest { value ->
                        textInputEditTextPhonetic.setText(value)
                    }
                }
                launch {
                    sharedViewModel.actionNavigation.drop(1).collect { action ->
                        when (action) {
                            is AddTagNavigation -> {
                                context?.hideKeyboard(textInputEditTextWord)
                                context?.hideKeyboard(textInputEditTextPhonetic)
                                if (!phonetics.isNullOrEmpty()) {
                                    togglePhoneticsView(false)
                                }
                                viewModel.getDictionary()?.let { dictionary ->
                                    val originalWord = textInputEditTextWord.text?.toString()
                                    val tags = tagsLayout.getTags(false)
                                    val word = viewModel.getEditedWord() ?: Word(
                                        _id = null,
                                        dictionaryId = dictionary._id ?: "",
                                        original = originalWord ?: "",
                                        type = 0,
                                        phonetic = null,
                                        translates = emptyList(),
                                        tags = tags
                                    )
                                    sharedViewModel.navigateTo(AddWordTagsScreen(word, dictionary))
                                }
                            }

                            is AddTranslationVariantNavigation -> {
                                context?.hideKeyboard(textInputEditTextWord)
                                context?.hideKeyboard(textInputEditTextPhonetic)
                                if (!phonetics.isNullOrEmpty()) {
                                    togglePhoneticsView(false)
                                }
                                sharedViewModel.navigateTo(
                                    AddTranslationVariantsScreen(
                                        textInputEditTextWord.text?.toString()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_add_dictionary_word, { menu, mi -> }, {
            when (it) {
                R.id.nav_save_word -> {
                    val word = textInputEditTextWord.text?.toString()
                    val translations = translationVariantsAdapter.getData()
                    lifecycleScope.launch {
                        viewModel.validate(context, word, translations).collect {
                            when (it) {
                                is FetchDataState.StartLoadingState -> {
                                    sharedViewModel.loading(true)
                                }

                                is FetchDataState.FinishLoadingState -> {
                                    sharedViewModel.loading(false)
                                }

                                is FetchDataState.ErrorState -> {
                                    displayError(
                                        it.exception.message
                                            ?: context?.getString(R.string.unknown_error),
                                        translationsRecyclerView
                                    )
                                }

                                is FetchDataState.DataState -> {
                                    if (it.data) {
                                        val phonetic = textInputEditTextPhonetic.text?.toString()
                                        val typePosition =
                                            spinnerChooseWordType.selectedItemPosition
                                        val tags = tagsLayout.getTags(false)
                                        saveWord(word, typePosition, translations, phonetic, tags)
                                    }
                                }

                                is FetchDataState.ErrorStateString -> {
                                    displayError(it.error, translationsRecyclerView)
                                }
                            }
                        }
                    }
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        dictionaryId = arguments?.getString(BUNDLE_DICTIONARY_ID, null)
        val word = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_WORD,
            Word::class.java
        ) else arguments?.getParcelable(BUNDLE_WORD) as? Word
        translationsRecyclerView.layoutManager = LinearLayoutManager(context)
        translationsRecyclerView.adapter = translationVariantsAdapter
        spinnerChooseWordType.adapter = wordTypeAdapter
        loadData(word)
    }

    private fun loadData(word: Word?) {
        Log.d(TAG, "loadData(${word?.original})")
        lifecycleScope.launch {
            viewModel.loadData(context, dictionaryId, word)
                .onCompletion {
                    loadTranslations()
                }
                .collect {
                    when (it) {
                        is FetchDataState.StartLoadingState -> {
                            sharedViewModel.loading(true)
                        }

                        is FetchDataState.FinishLoadingState -> {
                            sharedViewModel.loading(false)
                        }

                        is FetchDataState.ErrorState -> {
                            displayError(
                                it.exception.message
                                    ?: context?.getString(R.string.unknown_error),
                                translationsRecyclerView
                            )
                        }

                        is FetchDataState.DataState -> {
                            val phoneticList = it.data
                            Log.d(TAG, "phonetics updated: ${phoneticList.size}")
                            phonetics = phoneticList
                        }

                        is FetchDataState.ErrorStateString -> {
                            displayError(it.error, translationsRecyclerView)
                        }
                    }
                }
        }
    }

    private fun loadTranslations() {
        Log.d(TAG, "loadTranslations()")
        translationVariantsAdapter.clear()
        lifecycleScope.launch {
            viewModel.loadWordData()
                .onCompletion {
                    viewModel.getEditedWord()?.tags?.let { tags ->
                        tagsLayout.removeAllViews()
                        tags.forEach { tag ->
                            addTag(tag)
                        }
                    }
                }
                .collect {
                    when (it) {
                        is FetchDataState.StartLoadingState -> {
                            sharedViewModel.loading(true)
                        }

                        is FetchDataState.FinishLoadingState -> {
                            sharedViewModel.loading(false)
                        }

                        is FetchDataState.ErrorState -> {
                            displayError(
                                it.exception.message
                                    ?: context?.getString(R.string.unknown_error),
                                translationsRecyclerView
                            )
                        }

                        is FetchDataState.DataState -> {
                            translationVariantsAdapter.add(it.data)
                        }

                        is FetchDataState.ErrorStateString -> {
                            displayError(it.error, translationsRecyclerView)
                        }
                    }
                }
        }
    }

    private fun saveWord(
        word: String?,
        typePosition: Int,
        translations: List<TranslationVariant>,
        phonetic: String?,
        tags: List<WordTag>
    ) {
        lifecycleScope.launch {
            viewModel.save(context, word, typePosition, translations, phonetic, tags).collect {
                when (it) {
                    is FetchDataState.StartLoadingState -> {
                        sharedViewModel.loading(true)
                    }

                    is FetchDataState.FinishLoadingState -> {
                        sharedViewModel.loading(false)
                    }

                    is FetchDataState.ErrorState -> {
                        displayError(
                            it.exception.message
                                ?: context?.getString(R.string.unknown_error),
                            translationsRecyclerView
                        )
                    }

                    is FetchDataState.DataState -> {
                        if (it.data) {
                            findNavController().popBackStack()
                        }
                    }

                    is FetchDataState.ErrorStateString -> {
                        displayError(it.error, translationsRecyclerView)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            val types = it.resources.getStringArray(R.array.word_types).toList()
            val typesWithEmpty = mutableListOf("")
            typesWithEmpty.addAll(types)
            wordTypeAdapter = WordTypeSpinnerAdapter(it, typesWithEmpty)
        }
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
                viewModel.addTranslation(it)
            }
        }
        setFragmentResultListener(AddWordTagsFragment.BUNDLE_TAGS_RESULT_KEY) { requestKey, bundle ->
            val tags: ArrayList<WordTag> =
                bundle.getParcelableArrayList(AddWordTagsFragment.BUNDLE_TAGS_KEY) ?: ArrayList()
            if (tags.isNotEmpty()) {
                tagsLayout.removeAllViews()
            }
            tags.forEach {
                addTag(it)
            }
            viewModel.getEditedWord()?.tags?.let {
                it.clear()
                it.addAll(tags)
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

    private val onTranslationVariantEditListener = object : OnTranslationVariantEditListener {
        override fun onEdit(entity: TranslationVariant) {
            viewModel.deleteTranslation(entity)
            sharedViewModel.navigateTo(
                EditTranslationVariantsScreen(
                    textInputEditTextWord.text?.toString(),
                    dictionaryId ?: "",
                    entity
                )
            )
        }

        override fun onDelete(entity: TranslationVariant) {
            viewModel.deleteTranslation(entity)
        }
    }

    private val translationVariantsAdapter =
        TranslationVariantsAdapter(listener = onTranslationVariantEditListener)

    private fun addTag(tag: WordTag) {
        val bubbleView = BubbleView(requireContext())
        bubbleView.setWordTag(tag)
        tagsLayout.addView(bubbleView)
    }
}
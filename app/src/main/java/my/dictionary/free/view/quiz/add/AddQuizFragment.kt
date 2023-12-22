package my.dictionary.free.view.quiz.add

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.navigation.DictionaryChooseScreen
import my.dictionary.free.domain.models.navigation.WordsMultiChooseScreen
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.quiz.add.AddQuizViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.dialogs.DialogBuilders
import my.dictionary.free.view.dialogs.ValueDialogListener
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.ext.findAndDismissDialog
import my.dictionary.free.view.ext.visible
import my.dictionary.free.view.user.dictionary.SwipeDictionaryItem
import my.dictionary.free.view.user.dictionary.choose.DictionaryChooseFragment
import my.dictionary.free.view.user.dictionary.words.DictionaryWordsAdapter
import my.dictionary.free.view.user.dictionary.words.choose.WordsMultiChooseFragment
import my.dictionary.free.view.widget.ListItemDecoration
import my.dictionary.free.view.widget.OnItemSwipedListener

@AndroidEntryPoint
class AddQuizFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = AddQuizFragment::class.simpleName
        private const val DURATION_MIN = 1
        private const val DURATION_MAX = 60
        const val BUNDLE_QUIZ = "my.dictionary.free.view.quiz.add.AddQuizFragment.BUNDLE_QUIZ"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: AddQuizViewModel by viewModels()

    private lateinit var wordsRecyclerView: RecyclerView
    private var nameTextInputLayout: TextInputLayout? = null
    private var nameTextInputEditText: TextInputEditText? = null
    private var dictionaryNameTextView: AppCompatTextView? = null
    private var durationValueTextView: AppCompatTextView? = null
    private var reverseDictionary: AppCompatCheckBox? = null
    private var addDictionaryContainer: View? = null
    private var addDurationContainer: View? = null

    private var wordsAdapter: DictionaryWordsAdapter? = null
    private var selectedDictionary: Dictionary? = null
    private var duration: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_quiz, null)
        nameTextInputLayout = view.findViewById(R.id.text_input_name)
        nameTextInputEditText = view.findViewById(R.id.edit_text_name)
        dictionaryNameTextView = view.findViewById(R.id.dictionary_name)
        addDictionaryContainer = view.findViewById(R.id.add_dictionary)
        addDurationContainer = view.findViewById(R.id.add_duration)
        durationValueTextView = view.findViewById(R.id.duration_value)
        reverseDictionary = view.findViewById(R.id.reverse_dictionary)
        reverseDictionary?.setOnCheckedChangeListener { compoundButton, checked ->
            fillDictionary()
        }
        addDictionaryContainer?.setOnClickListener {
            sharedViewModel.navigateTo(DictionaryChooseScreen())
        }
        dictionaryNameTextView?.setOnClickListener {
            sharedViewModel.navigateTo(DictionaryChooseScreen())
        }
        addDurationContainer?.setOnClickListener {
            showSecondsPickerDialog()
        }
        durationValueTextView?.setOnClickListener {
            showSecondsPickerDialog()
        }
        view.findViewById<View>(R.id.add_words).setOnClickListener {
            if (selectedDictionary != null && selectedDictionary!!._id != null) {
                sharedViewModel.navigateTo(
                    WordsMultiChooseScreen(
                        selectedDictionary!!._id!!,
                        wordsAdapter?.getWords()?.toList() as? ArrayList<Word>
                    )
                )
            } else {
                displayError(getString(R.string.error_set_dictionary_first), wordsRecyclerView)
            }
        }
        wordsRecyclerView = view.findViewById(R.id.words_recycler_view)
        wordsRecyclerView.layoutManager = LinearLayoutManager(context)
        val itemTouchHelper =
            ItemTouchHelper(SwipeDictionaryItem(requireContext(), onItemSwipedListener))
        itemTouchHelper.attachToRecyclerView(wordsRecyclerView)
        wordsRecyclerView.addItemDecoration(ListItemDecoration(context = requireContext()))
        wordsAdapter = DictionaryWordsAdapter(mutableListOf())
        wordsRecyclerView.adapter = wordsAdapter
        return view
    }

    private val onItemSwipedListener = object : OnItemSwipedListener {
        override fun onSwiped(position: Int) {
            Log.d(TAG, "swipe item by position $position")
            wordsAdapter?.temporaryRemoveItem(position)
            wordsAdapter?.finallyRemoveItem()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.displayErrorUIState.drop(1).collect { errorMessage ->
                        displayError(errorMessage, wordsRecyclerView)
                    }
                }
                launch {
                    viewModel.loadingUIState.collect { visible ->

                    }
                }
                launch {
                    viewModel.successCreateQuizUIState.collect { success ->
                        if (success) {
                            findNavController().popBackStack()
                        }
                    }
                }
                launch {
                    viewModel.validateName.collect { error ->
                        nameTextInputLayout?.error = error
                    }
                }
                launch {
                    viewModel.dictionaryUIState.collect { dictionary ->
                        selectedDictionary = dictionary
                        fillDictionary()
                    }
                }
                launch {
                    viewModel.durationUIState.collect { seconds ->
                        duration = seconds
                        fillDuration()
                    }
                }
                launch {
                    viewModel.wordUIState.collect { words ->
                        if (wordsAdapter?.getWords()?.isEmpty() == true) {
                            fillWords(words)
                        }
                    }
                }
                launch {
                    viewModel.nameUIState.collect { name ->
                        nameTextInputEditText?.setText(name)
                    }
                }
                launch {
                    viewModel.reversedUIState.collect { value ->
                        reverseDictionary?.isChecked = value
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_add_quiz, { menu, mi ->
        }, {
            when (it) {
                R.id.save_quiz -> {
                    val name = nameTextInputEditText?.text?.toString()
                    val words = wordsAdapter?.getWords()?.toList() ?: emptyList()
                    if (viewModel.validate(
                            context = context,
                            name = name,
                            duration = duration,
                            dictionary = selectedDictionary,
                            words = words
                        )
                    ) {
                        viewModel.save(
                            context = context,
                            name = name,
                            duration = duration,
                            dictionary = selectedDictionary,
                            reversed = reverseDictionary?.isChecked ?: false,
                            words = words
                        )
                    }
                    return@addMenuProvider true
                }

                else -> false
            }
        })

        val quiz = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_QUIZ,
            Quiz::class.java
        ) else arguments?.getParcelable(BUNDLE_QUIZ) as? Quiz
        Log.d(TAG, quiz?.toString() ?: "quiz is null")
        viewModel.setQuiz(quiz)
        val title =
            if (viewModel.isEditMode()) getString(R.string.edit_quiz) else getString(R.string.add_quiz)
        sharedViewModel.setTitle(title)
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(DictionaryChooseFragment.BUNDLE_DICTIONARY_RESULT) { requestKey, bundle ->
            selectedDictionary = if (hasTiramisu()) bundle.getParcelable(
                DictionaryChooseFragment.BUNDLE_DICTIONARY_KEY,
                Dictionary::class.java
            ) else bundle.getParcelable(DictionaryChooseFragment.BUNDLE_DICTIONARY_KEY) as? Dictionary
            fillDictionary()
            wordsAdapter?.clearData()
        }
        setFragmentResultListener(WordsMultiChooseFragment.BUNDLE_WORDS_RESULT) { requestKey, bundle ->
            val words: ArrayList<Word> =
                bundle.getParcelableArrayList(WordsMultiChooseFragment.BUNDLE_WORDS_KEY)
                    ?: ArrayList()
            fillWords(words)
        }
    }

    override fun onStart() {
        super.onStart()
        fillDictionary()
        fillDuration()
    }

    private fun fillDictionary() {
        addDictionaryContainer?.visible(selectedDictionary == null, View.GONE)
        dictionaryNameTextView?.visible(selectedDictionary != null, View.GONE)
        selectedDictionary?.let { dict ->
            val reversed = reverseDictionary?.isChecked ?: false
            if (reversed) {
                val text = "${dict.dictionaryTo.langFull} - ${dict.dictionaryFrom.langFull}"
                dictionaryNameTextView?.text = text
            } else {
                val text =
                    if (dict.dialect?.isNullOrEmpty() == true) "${dict.dictionaryFrom.langFull} - ${dict.dictionaryTo.langFull}" else "${dict.dictionaryFrom.langFull} - ${dict.dictionaryTo.langFull} (${dict.dialect})"
                dictionaryNameTextView?.text = text
            }
        }
    }

    private fun fillDuration() {
        addDurationContainer?.visible(duration == null, View.GONE)
        durationValueTextView?.visible(duration != null, View.GONE)
        duration?.let { value ->
            durationValueTextView?.text = getString(R.string.seconds_value, value)
        }
    }

    private fun fillWords(words: List<Word>) {
        wordsAdapter?.clearData()
        words.forEach {
            wordsAdapter?.add(it)
        }
    }

    private fun showSecondsPickerDialog() {
        val dialog = DialogBuilders.NumberPickerDialogBuilder
            .cancelButtonTitle(getString(R.string.cancel))
            .minValue(DURATION_MIN)
            .maxValue(DURATION_MAX)
            .title(getString(R.string.set_duration))
            .description(getString(R.string.seconds_dialog_description))
            .iconRes(R.drawable.ic_baseline_time_24)
            .okButtonTitle(getString(R.string.set))
            .listener(object : ValueDialogListener {
                override fun onValueChanged(value: Int) {
                    duration = value
                    fillDuration()
                }

                override fun onCancelClicked() {

                }

                override fun onOkButtonClicked() {

                }
            }).build()
        childFragmentManager.findAndDismissDialog("DurationDialog")
        dialog.show(childFragmentManager, "DurationDialog")
    }
}
package my.dictionary.free.view.quiz.run

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
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
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.tags.CategoryTag
import my.dictionary.free.domain.models.words.tags.Tag
import my.dictionary.free.domain.models.words.tags.WordTag
import my.dictionary.free.domain.models.words.variants.TranslationCategory
import my.dictionary.free.domain.utils.QuizTimer
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.quiz.run.RunQuizViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.FetchDataState
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.ext.hide
import my.dictionary.free.view.ext.hideKeyboard
import my.dictionary.free.view.ext.visible
import my.dictionary.free.view.widget.bubble.BubbleLayout
import my.dictionary.free.view.widget.bubble.BubbleView

@AndroidEntryPoint
class RunQuizFragment : AbstractBaseFragment() {
    companion object {
        private val TAG = RunQuizFragment::class.simpleName
        const val BUNDLE_QUIZ =
            "my.dictionary.free.view.quiz.run.RunQuizFragment.BUNDLE_QUIZ"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: RunQuizViewModel by viewModels()

    private var answerInputLayout: TextInputLayout? = null
    private var answerEditText: TextInputEditText? = null
    private var wordTextView: AppCompatTextView? = null
    private var phoneticTextView: AppCompatTextView? = null
    private var timeTextView: AppCompatTextView? = null
    private var btnNext: MenuItem? = null
    private var rootView: View? = null
    private var tagsContainer: View? = null
    private var categoriesContainer: View? = null
    private var typesContainer: View? = null
    private var tagsBubbleLayout: BubbleLayout? = null
    private var categoriesBubbleLayout: BubbleLayout? = null
    private var typesBubbleLayout: BubbleLayout? = null

    private var quizTimer: QuizTimer? = null
    private var wordTypes: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_run_quiz, null)
        answerInputLayout = view.findViewById(R.id.text_input_result)
        answerEditText = view.findViewById(R.id.edit_text_result)
        wordTextView = view.findViewById(R.id.word)
        phoneticTextView = view.findViewById(R.id.phonetic)
        timeTextView = view.findViewById(R.id.time)
        rootView = view.findViewById(R.id.root)
        tagsContainer = view.findViewById(R.id.tags_container)
        categoriesContainer = view.findViewById(R.id.categories_container)
        typesContainer = view.findViewById(R.id.types_container)
        tagsBubbleLayout = view.findViewById(R.id.tags_layout)
        categoriesBubbleLayout = view.findViewById(R.id.categories_layout)
        typesBubbleLayout = view.findViewById(R.id.types_layout)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        answerEditText?.addTextChangedListener(onAnswerChangeListener)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.validationErrorUIState.drop(1).collect { error ->
                        if (answerEditText?.text?.isNullOrEmpty() == false) {
                            answerInputLayout?.error = error
                        } else {
                            answerInputLayout?.error = ""
                        }
                    }
                }
                launch {
                    viewModel.nextWordUIState.collect { pair ->
                        val word = pair.first
                        val reversed = pair.second
                        Log.d(TAG, "quiz new word: $word")
                        fillQuiz(word, reversed)
                    }
                }
                launch {
                    viewModel.titleQuizUIState.collect { titlePair ->
                        sharedViewModel.setTitle(
                            getString(
                                R.string.quiz_title,
                                titlePair.first,
                                titlePair.second
                            )
                        )
                    }
                }
                launch {
                    viewModel.quizEndedUIState.drop(1).collect { endResult ->
                        val isEnded = endResult.first
                        val countWord = endResult.third
                        val successWord = endResult.second
                        Log.d(TAG, "quiz is ended = $isEnded")
                        if (isEnded) {
                            wordTextView?.text =
                                getString(R.string.quiz_ended_result, successWord, countWord)
                            phoneticTextView?.text = ""
                            answerEditText?.setText("")
                            timeTextView?.text = ""
                            btnNext?.setIcon(R.drawable.ic_baseline_save_24)
                        }
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_run_quiz, { menu, mi ->
            btnNext = menu.findItem(R.id.next)
        }, {
            when (it) {
                R.id.next -> {
                    if (viewModel.isEnded()) {
                        saveQuiz()
                    } else {
                        nextOrSkip()
                    }
                    return@addMenuProvider true
                }

                else -> false
            }
        })
    }

    private fun saveQuiz() {
        lifecycleScope.launch {
            viewModel.saveQuiz(context).collect {
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
                            rootView
                        )
                    }

                    is FetchDataState.DataState -> {
                        Log.d(TAG, "quiz result save: ${it.data}")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        val quiz = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_QUIZ,
            Quiz::class.java
        ) else arguments?.getParcelable(BUNDLE_QUIZ) as? Quiz
        Log.d(TAG, quiz?.toString() ?: "quiz is null")
        quiz?.let {
            Log.d(TAG, "millisInFuture = ${it.timeInSeconds.toLong()}")
            quizTimer?.cancel()
            quizTimer = object : QuizTimer(
                millisInFuture = it.timeInSeconds.toLong() * 1000L
            ) {
                @SuppressLint("RestrictedApi", "SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    super.onTick(millisUntilFinished)
                    Log.d(TAG, "change timer $millisUntilFinished")
                    val seconds = millisUntilFinished / 1000
                    timeTextView?.text = "$seconds"
                }

                override fun onFinish() {
                    super.onFinish()
                    Log.d(TAG, "timer is finished")
                    timeTextView?.text = getString(R.string.fail_time_over)
                }
            }
        }
        wordTypes = context?.resources?.getStringArray(R.array.word_types)?.toList()
        lifecycleScope.launch {
            viewModel.setQuiz(quiz).collect {
                when (it) {
                    is FetchDataState.StartLoadingState -> {
                        sharedViewModel.loading(true)
                    }

                    is FetchDataState.FinishLoadingState -> {
                        sharedViewModel.loading(false)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun fillQuiz(word: Word, reversed: Boolean) {
        answerInputLayout?.error = ""
        answerEditText?.setText("")
        timeTextView?.text = ""
        wordTextView?.text = if (!reversed) word.original else word.translates?.first()?.translation
        val visiblePhonetic = !word.phonetic.isNullOrEmpty() && !reversed
        phoneticTextView?.visible(visiblePhonetic, View.GONE)
        if (visiblePhonetic) {
            if (viewModel.getQuiz()?.hidePhonetic == true) {
                phoneticTextView?.text = "[${word.phonetic?.hide()}]"
                phoneticTextView?.setOnClickListener {
                    phoneticTextView?.text = "[${word.phonetic}]"
                }
            } else {
                phoneticTextView?.text = "[${word.phonetic}]"
            }

        }
        fillTags(word)
        fillCategories(word)
        fillTypes(word)
        quizTimer?.start()
        answerEditText?.requestFocus()
        btnNext?.setIcon(R.drawable.ic_next_word_quiz)
    }

    private fun fillTags(word: Word) {
        tagsBubbleLayout?.let { layout ->
            layout.removeAllViews()
            if (viewModel.getQuiz()?.showTags == true) {
                tagsContainer?.visible(word.tags.isNotEmpty(), View.GONE)
                word.tags.forEach {
                    addTag(it)
                }
            } else {
                tagsContainer?.visible(false, View.GONE)
            }
        }
    }

    private fun fillCategories(word: Word) {
        categoriesBubbleLayout?.let { layout ->
            layout.removeAllViews()
            if (viewModel.getQuiz()?.showCategories == true) {
                val wordCategories = mutableSetOf<TranslationCategory>()
                word.translates.forEach { variant ->
                    viewModel.getTranslationCategories()
                        .find { variant.categoryId == it._id }?.let {
                            wordCategories.add(it)
                        }
                }
                categoriesContainer?.visible(wordCategories.isNotEmpty(), View.GONE)
                wordCategories.forEach {
                    addCategory(it)
                }
            } else {
                categoriesContainer?.visible(false, View.GONE)
            }
        }
    }

    private fun fillTypes(word: Word) {
        typesBubbleLayout?.let { layout ->
            layout.removeAllViews()
            if (viewModel.getQuiz()?.showTypes == true) {
                if (word.type == 0) {
                    typesContainer?.visible(false, View.GONE)
                    return@let
                }
                val wordType = wordTypes?.get(word.type)
                val matchTag =
                    wordTypes?.find { wordTag -> wordType == wordTag }
                if (matchTag != null) {
                    typesContainer?.visible(true, View.GONE)
                    addType(matchTag)
                } else {
                    typesContainer?.visible(false, View.GONE)
                }
            } else {
                typesContainer?.visible(false, View.GONE)
            }
        }
    }

    private fun nextOrSkip() {
        quizTimer?.cancel()
        context?.hideKeyboard(answerEditText)
        val answer = answerEditText?.text?.toString()?.trim() ?: ""
        lifecycleScope.launch {
            viewModel.checkAnswer(context, answer).collect {
                when (it) {
                    is FetchDataState.DataState -> {
                        if (it.data) {
                            timeTextView?.text = getString(R.string.success)
                            Log.d(TAG, "answer is $answer")
                            viewModel.nextWord(answer)
                        } else {
                            Log.d(TAG, "skip current word")
                            viewModel.skipAnswer()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private val onAnswerChangeListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(text: Editable?) {
            val isTimerRunning = quizTimer?.isRunning() ?: false
            if (isTimerRunning) {
                val newAnswer = text?.toString()?.trim()
                lifecycleScope.launch {
                    viewModel.checkAnswer(context, newAnswer).collect {
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
                                    rootView
                                )
                            }

                            is FetchDataState.DataState -> {
                                if (it.data) {
                                    context?.hideKeyboard(answerEditText)
                                    timeTextView?.text = getString(R.string.success)
                                    quizTimer?.cancel()
                                }
                            }

                            is FetchDataState.ErrorStateString -> {
                                displayError(it.error, rootView)
                            }
                        }
                    }
                }
            }
        }

    }

    private fun addTag(tag: WordTag) {
        Log.d(TAG, "add tag ${tag.tagName}")
        val bubbleView = BubbleView(requireContext())
        bubbleView.setWordTag(tag)
        bubbleView.isHide(true)
        tagsBubbleLayout?.addView(bubbleView)
    }

    private fun addCategory(category: TranslationCategory) {
        Log.d(TAG, "add category ${category.categoryName}")
        val bubbleView = BubbleView(requireContext())
        val tag = CategoryTag(category, category.categoryName)
        bubbleView.setWordTag(tag)
        bubbleView.isHide(true)
        categoriesBubbleLayout?.addView(bubbleView)
    }

    private fun addType(type: String) {
        Log.d(TAG, "add type $type")
        val bubbleView = BubbleView(requireContext())
        bubbleView.setWordTag(Tag(type, ""))
        bubbleView.isHide(true)
        typesBubbleLayout?.addView(bubbleView)
    }
}
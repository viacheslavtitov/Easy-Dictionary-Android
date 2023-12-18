package my.dictionary.free.view.quiz.run

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.quiz.run.RunQuizViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.ext.hideKeyboard

@AndroidEntryPoint
class RunQuizFragment : AbstractBaseFragment() {
    companion object {
        private val TAG = RunQuizFragment::class.simpleName
        const val BUNDLE_QUIZ =
            "my.dictionary.free.view.quiz.run.RunQuizFragment.BUNDLE_QUIZ"
        private const val INTERVAL_MILLISECONDS = 1000L
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: RunQuizViewModel by viewModels()

    private var answerInputLayout: TextInputLayout? = null
    private var answerEditText: TextInputEditText? = null
    private var wordTextView: AppCompatTextView? = null
    private var phoneticTextView: AppCompatTextView? = null
    private var timeTextView: AppCompatTextView? = null
    private var btnNext: Button? = null

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
        btnNext = view.findViewById<Button?>(R.id.btn_next)?.also {
            it.setOnClickListener {
                nextOrSkip(it.isActivated)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        answerEditText?.addTextChangedListener(onAnswerChangeListener)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.displayErrorUIState.drop(1).collect { errorMessage ->
                        btnNext?.let {
                            displayError(errorMessage, it)
                        }
                    }
                }
                launch {
                    viewModel.loadingUIState.collect { visible ->

                    }
                }
                launch {
                    viewModel.validationSuccessUIState.drop(1).collect { success ->
                        btnNext?.isActivated = success
                        if(success) {
                            btnNext?.text = getString(R.string.next)
                            timeTextView?.text = getString(R.string.success)
                            quizTimer?.cancel()
                        } else {
                            btnNext?.text = getString(R.string.skip)
                        }
                    }
                }
                launch {
                    viewModel.validationErrorUIState.collect { error ->
                        if(answerEditText?.text?.isNullOrEmpty() == false) {
                            answerInputLayout?.error = error
                        } else {
                            answerInputLayout?.error = ""
                        }
                    }
                }
                launch {
                    viewModel.nextWordUIState.collect { word ->
                        Log.d(TAG, "quiz new word: $word")
                        fillQuiz(word)
                    }
                }
                launch {
                    viewModel.quizEndedUIState.collect { isEnded ->
                        Log.d(TAG, "quiz is ended = $isEnded")
                        if(isEnded) {

                        }
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
            quizTimer = object : CountDownTimer(
                it.timeInSeconds.toLong() * 1000L,
                INTERVAL_MILLISECONDS
            ) {
                @SuppressLint("RestrictedApi", "SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    Log.d(TAG, "change timer $millisUntilFinished")
                    val seconds = millisUntilFinished / 1000
                    timeTextView?.text = "$seconds"
                }

                override fun onFinish() {
                    Log.d(TAG, "timer is finished")
                    btnNext?.isActivated = false
                    btnNext?.text = getString(R.string.skip)
                    timeTextView?.text = getString(R.string.fail_time_over)
                }
            }
        }
        viewModel.setQuiz(quiz)
    }

    private fun fillQuiz(word: Word) {
        answerInputLayout?.error = ""
        answerEditText?.setText("")
        timeTextView?.text = ""
        wordTextView?.text = word.original
        phoneticTextView?.text = if(word.phonetic != null) "[${word.phonetic}]" else ""
        quizTimer?.start()
        answerEditText?.requestFocus()
        btnNext?.isActivated = false
        btnNext?.text = getString(R.string.skip)
    }

    private fun nextOrSkip(skip: Boolean) {
        quizTimer?.cancel()
        context?.hideKeyboard(answerEditText)
        if (skip) {
            Log.d(TAG, "skip current word")
            viewModel.skipAnswer()
        } else {
            Log.d(TAG, "next word")
            viewModel.nextWord()
        }
    }

    private val onAnswerChangeListener = object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(text: Editable?) {
            val newAnswer = text?.toString()?.trim()
            viewModel.checkAnswer(context, newAnswer)
        }

    }

    private var quizTimer: CountDownTimer? = null
}
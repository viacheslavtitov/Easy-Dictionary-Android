package my.dictionary.free.view.user.dictionary.words.choose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.words.DictionaryWordsViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.choose.DictionaryChooseFragment
import my.dictionary.free.view.user.dictionary.words.DictionaryWordsAdapter
import my.dictionary.free.view.user.dictionary.words.DictionaryWordsFragment
import my.dictionary.free.view.widget.ListItemDecoration
import my.dictionary.free.view.widget.OnListItemClickListener
import my.dictionary.free.view.widget.OnListTouchListener

@AndroidEntryPoint
class WordsMultiChooseFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = WordsMultiChooseFragment::class.simpleName
        const val BUNDLE_DICTIONARY_ID =
            "my.dictionary.free.view.user.dictionary.words.choose.WordsMultiChooseFragment.BUNDLE_DICTIONARY_ID"
        const val BUNDLE_WORDS =
            "my.dictionary.free.view.user.dictionary.words.choose.WordsMultiChooseFragment.BUNDLE_WORDS"
        const val BUNDLE_WORDS_RESULT =
            "my.dictionary.free.view.user.dictionary.words.choose.WordsMultiChooseFragment.BUNDLE_WORDS_RESULT"
        const val BUNDLE_WORDS_KEY =
            "my.dictionary.free.view.user.dictionary.words.choose.WordsMultiChooseFragment.BUNDLE_WORDS_KEY"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: DictionaryWordsViewModel by viewModels()

    private lateinit var wordsRecyclerView: RecyclerView
    private var wordsAdapter: DictionaryWordsAdapter? = null
    private var dictionaryId: String? = null
    private var editWords: ArrayList<Word>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_words_multichoose, null)
        wordsRecyclerView = view.findViewById(R.id.recycler_view)
        wordsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        wordsRecyclerView.addItemDecoration(ListItemDecoration(context = requireContext()))
        wordsRecyclerView.addOnItemTouchListener(
            OnListTouchListener(
                requireContext(),
                wordsRecyclerView,
                onWordsClickListener
            )
        )
        wordsAdapter = DictionaryWordsAdapter(mutableListOf())
        wordsRecyclerView.adapter = wordsAdapter
        return view
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
                    viewModel.shouldClearWordsUIState.collect { clear ->
                        if (clear) {
                            Log.d(TAG, "clear words")
                            wordsAdapter?.clearData()
                        } else {
                            editWords?.forEach {
                                wordsAdapter?.selectWord(it)
                            }
                        }
                    }
                }
                launch {
                    viewModel.wordsUIState.collect { word ->
                        Log.d(TAG, "word updated: $word")
                        wordsAdapter?.add(word)
                    }
                }
                launch {
                    viewModel.titleUIState.collect { title ->
                        Log.d(TAG, "set title: $title")
                        sharedViewModel.setTitle(title)
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_words_choose, { menu, mi ->
        }, {
            when (it) {
                R.id.nav_add_word -> {
                    val selectedWords = wordsAdapter?.getSelectedWords()
                    val result = ArrayList<Word>()
                    selectedWords?.forEach {
                        result.add(it)
                    }
                    val bundle = Bundle().apply {
                        putParcelableArrayList(BUNDLE_WORDS_KEY, result)
                    }
                    setFragmentResult(BUNDLE_WORDS_RESULT, bundle)
                    findNavController().popBackStack()
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        dictionaryId = arguments?.getString(BUNDLE_DICTIONARY_ID, null)
        editWords = if (hasTiramisu())
            arguments?.getParcelableArrayList(BUNDLE_WORDS, Word::class.java)
        else arguments?.getParcelableArrayList(BUNDLE_WORDS)
        refreshWords()
    }

    private fun refreshWords() {
        wordsAdapter?.clearData()
        viewModel.loadWords(context, dictionaryId)
    }

    private val onWordsClickListener = object : OnListItemClickListener {
        override fun onListItemClick(childView: View) {
            wordsAdapter?.getItemByPosition(
                wordsRecyclerView.getChildAdapterPosition(childView)
            )?.let { word ->
                selectWord(word)
            }
        }

        override fun onListItemLongClick(childView: View) {
            wordsAdapter?.getItemByPosition(
                wordsRecyclerView.getChildAdapterPosition(childView)
            )?.let { word ->
                selectWord(word)
            }
        }
    }

    private fun selectWord(word: Word) {
        wordsAdapter?.selectWord(word)
        val selectedDictionaryCount = wordsAdapter?.getSelectedWordsCount() ?: 0
        if (selectedDictionaryCount < 1) {
            wordsAdapter?.clearSelectedWords()
        }
    }

}
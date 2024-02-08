package my.dictionary.free.view.user.dictionary.words.choose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.filter.FilterModel
import my.dictionary.free.domain.models.navigation.DictionaryFilterScreen
import my.dictionary.free.domain.models.navigation.DictionaryMultiChooseFilterScreen
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.words.DictionaryWordsViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.FetchDataState
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.words.DictionaryWordsAdapter
import my.dictionary.free.view.user.dictionary.words.filter.DictionaryWordsFilterFragment
import my.dictionary.free.view.user.dictionary.words.translations.add.CategorySpinnerAdapter
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
        val wordTypes = mutableListOf<String>().apply {
            add(" ")
            context?.resources?.getStringArray(R.array.word_types)?.toList()?.let {
                addAll(it)
            }
        }
        wordsAdapter = DictionaryWordsAdapter(mutableListOf(), mutableListOf(), wordTypes)
        wordsRecyclerView.adapter = wordsAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                R.id.nav_filter -> {
                    viewModel.getDictionary()?.let {dictionary ->
                        sharedViewModel.navigateTo(DictionaryMultiChooseFilterScreen(dictionary, wordsAdapter?.getFilteredModel()))
                    }
                    return@addMenuProvider true
                }
                R.id.nav_select_all -> {
                    wordsAdapter?.let {
                        if(it.isAllSelected()) {
                            it.clearSelectedWords()
                        } else {
                           it.selectAll()
                        }
                    }
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        dictionaryId = arguments?.getString(BUNDLE_DICTIONARY_ID, null)
        setFragmentResultListener(DictionaryWordsFilterFragment.BUNDLE_FILTER_RESULT_KEY) { requestKey, bundle ->
            val filterModel: FilterModel? =
                if (hasTiramisu()) bundle.getParcelable(
                    DictionaryWordsFilterFragment.BUNDLE_FILTER_RESULT,
                    FilterModel::class.java
                ) else bundle.getParcelable(DictionaryWordsFilterFragment.BUNDLE_FILTER_RESULT)
            wordsAdapter?.setFilterModel(filterModel)
        }
        editWords = if (hasTiramisu())
            arguments?.getParcelableArrayList(BUNDLE_WORDS, Word::class.java)
        else arguments?.getParcelableArrayList(BUNDLE_WORDS)
        refreshWords()
    }

    private fun refreshWords() {
        lifecycleScope.launch {
            viewModel.loadWords(context, dictionaryId).collect {
                when (it) {
                    is FetchDataState.StartLoadingState -> {
                        wordsAdapter?.clearData()
                        sharedViewModel.loading(true)
                    }

                    is FetchDataState.FinishLoadingState -> {
                        sharedViewModel.loading(false)
                    }

                    is FetchDataState.ErrorState -> {
                        displayError(
                            it.exception.message ?: context?.getString(R.string.unknown_error),
                            wordsRecyclerView
                        )
                    }

                    is FetchDataState.DataState -> {
                        wordsAdapter?.add(it.data, null)
                    }

                    is FetchDataState.ErrorStateString -> {
                        displayError(it.error, wordsRecyclerView)
                    }
                }
            }
        }
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
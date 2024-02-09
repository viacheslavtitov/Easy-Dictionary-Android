package my.dictionary.free.view.user.dictionary.words.filter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.filter.FilterModel
import my.dictionary.free.domain.models.words.tags.CategoryTag
import my.dictionary.free.domain.models.words.tags.Tag
import my.dictionary.free.domain.models.words.tags.WordTag
import my.dictionary.free.domain.models.words.variants.TranslationCategory
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.words.filter.DictionaryWordsFilterViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.FetchDataState
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment
import my.dictionary.free.view.widget.bubble.BubbleLayout
import my.dictionary.free.view.widget.bubble.BubbleView

@AndroidEntryPoint
class DictionaryWordsFilterFragment : AbstractBaseFragment() {
    companion object {
        private val TAG = DictionaryWordsFilterFragment::class.simpleName
        const val BUNDLE_DICTIONARY =
            "my.dictionary.free.view.user.dictionary.words.filter.DictionaryWordsFilterFragment.BUNDLE_DICTIONARY"
        const val BUNDLE_FILTER =
            "my.dictionary.free.view.user.dictionary.words.filter.DictionaryWordsFilterFragment.BUNDLE_FILTER"
        const val BUNDLE_FILTER_RESULT_KEY =
            "my.dictionary.free.view.user.dictionary.words.filter.DictionaryWordsFilterFragment.BUNDLE_FILTER_RESULT_KEY"
        const val BUNDLE_FILTER_RESULT =
            "my.dictionary.free.view.user.dictionary.words.filter.DictionaryWordsFilterFragment.BUNDLE_FILTER_RESULT"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: DictionaryWordsFilterViewModel by viewModels()

    private lateinit var tagsLayout: BubbleLayout
    private lateinit var categoriesLayout: BubbleLayout
    private lateinit var typesLayout: BubbleLayout
    private lateinit var rootView: ViewGroup
    private var filterModel: FilterModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dictionary_words_filter, null)
        tagsLayout = view.findViewById(R.id.tags_layout)
        categoriesLayout = view.findViewById(R.id.categories_layout)
        typesLayout = view.findViewById(R.id.types_layout)
        rootView = view.findViewById(R.id.root)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        addMenuProvider(R.menu.menu_dictionary_words_filter, { menu, mi ->
        }, {
            when (it) {
                R.id.save_filter -> {
                    val tags = tagsLayout.getTags<WordTag>(true)
                    val categories = categoriesLayout.getTags<CategoryTag>(true)
                    val types = typesLayout.getTags<Tag>(true)
                    val filterModel = prepareFilterModel(tags, categories, types)
                    val bundle = Bundle().apply {
                        putParcelable(BUNDLE_FILTER_RESULT, filterModel)
                    }
                    setFragmentResult(BUNDLE_FILTER_RESULT_KEY, bundle)
                    findNavController().popBackStack()
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        filterModel = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_FILTER,
            FilterModel::class.java
        ) else arguments?.getParcelable(BUNDLE_FILTER)
        val dictionary = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_DICTIONARY,
            Dictionary::class.java
        ) else arguments?.getParcelable(BUNDLE_DICTIONARY) as? Dictionary
        tagsLayout.removeAllViews()
        dictionary?.tags?.forEach {
            addTag(it)
        }
        loadCategories(dictionary)
        typesLayout.removeAllViews()
        context?.resources?.getStringArray(R.array.word_types)?.toList()?.forEachIndexed { index, value ->
            addType(value, index.toString())
        }
    }

    private fun loadCategories(dictionary: Dictionary?) {
        categoriesLayout.removeAllViews()
        lifecycleScope.launch {
            viewModel.loadCategories(context, dictionary).collect {
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
                        addCategory(it.data)
                    }

                    is FetchDataState.ErrorStateString -> {
                        displayError(it.error, rootView)
                    }
                }
            }
        }
    }

    private fun addTag(tag: WordTag) {
        val bubbleView = BubbleView(requireContext())
        bubbleView.setWordTag(tag)
        filterModel?.let { model ->
            if(model.tags.isNotEmpty()) {
                val selected = model.tags.find { it.tagName == tag.tagName } != null
                bubbleView.select(selected)
            }
        }
        tagsLayout.addView(bubbleView)
    }

    private fun addCategory(category: TranslationCategory) {
        val bubbleView = BubbleView(requireContext())
        val tag = CategoryTag(category, category.categoryName)
        bubbleView.setWordTag(tag)
        filterModel?.let { model ->
            if(model.categories.isNotEmpty()) {
                val selected = model.categories.find { it.tagName == tag.tagName } != null
                bubbleView.select(selected)
            }
        }
        categoriesLayout.addView(bubbleView)
    }

    private fun addType(type: String, id: String) {
        val bubbleView = BubbleView(requireContext())
        bubbleView.setWordTag(Tag(type, id))
        filterModel?.let { model ->
            if(model.types.isNotEmpty()) {
                val selected = model.types.find { it.tagName == type } != null
                bubbleView.select(selected)
            }
        }
        typesLayout.addView(bubbleView)
    }

    private fun prepareFilterModel(
        tags: List<Tag>,
        categories: List<Tag>,
        types: List<Tag>
    ): FilterModel {
        return FilterModel(tags, categories, types)
    }
}
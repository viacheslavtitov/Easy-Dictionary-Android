package my.dictionary.free.view.user.dictionary.words.tags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.WordTag
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.words.tags.AddWordTagsViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.FetchDataState
import my.dictionary.free.view.dialogs.DialogBuilders
import my.dictionary.free.view.dialogs.InputDialogListener
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.ext.findAndDismissDialog
import my.dictionary.free.view.widget.bubble.BubbleLayout
import my.dictionary.free.view.widget.bubble.BubbleView

@AndroidEntryPoint
class AddWordTagsFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = AddWordTagsFragment::class.simpleName
        const val BUNDLE_WORD =
            "my.dictionary.free.view.user.dictionary.words.tags.AddWordTagsFragment.BUNDLE_WORD"
        const val BUNDLE_DICTIONARY =
            "my.dictionary.free.view.user.dictionary.words.tags.AddWordTagsFragment.BUNDLE_DICTIONARY"
        const val BUNDLE_TAGS_KEY =
            "my.dictionary.free.view.user.dictionary.words.tags.AddWordTagsFragment.BUNDLE_TAGS_KEY"
        const val BUNDLE_TAGS_RESULT_KEY =
            "my.dictionary.free.view.user.dictionary.words.tags.AddWordTagsFragment.BUNDLE_TAGS_RESULT_KEY"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: AddWordTagsViewModel by viewModels()

    private lateinit var bubbleLayout: BubbleLayout
    private lateinit var wordTextView: AppCompatTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_word_tags, null)
        bubbleLayout = view.findViewById(R.id.bubbles_layout)
        wordTextView = view.findViewById(R.id.word_text_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        addMenuProvider(R.menu.menu_tags, { menu, mi -> }, {
            when (it) {
                R.id.nav_save_tags -> {
                    val selectedTags = bubbleLayout.getTags(true)
                    val bundle = Bundle().apply {
                        putParcelableArrayList(BUNDLE_TAGS_KEY, selectedTags)
                    }
                    setFragmentResult(BUNDLE_TAGS_RESULT_KEY, bundle)
                    findNavController().popBackStack()
                    return@addMenuProvider true
                }

                R.id.nav_add_tag -> {
                    val dialog = DialogBuilders.InputDialogBuilder
                        .cancelButtonTitle(getString(R.string.cancel))
                        .title(getString(R.string.add_tag))
                        .okButtonTitle(getString(R.string.ok))
                        .listener(object : InputDialogListener {
                            var tagName: String? = null
                            override fun onTextChanged(newText: String?) {
                                tagName = newText
                            }

                            override fun onCancelClicked() {
                            }

                            override fun onOkButtonClicked() {
                                createTag(tagName)
                            }
                        }).build()
                    childFragmentManager.findAndDismissDialog("InputDialog")
                    dialog.show(childFragmentManager, "InputDialog")
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        val word = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_WORD,
            Word::class.java
        ) else arguments?.getParcelable(BUNDLE_WORD) as? Word
        val dictionary = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_DICTIONARY,
            Dictionary::class.java
        ) else arguments?.getParcelable(BUNDLE_DICTIONARY) as? Dictionary
        lifecycleScope.launch {
            viewModel.loadData(context, word, dictionary).collect {
                when (it) {
                    is FetchDataState.ErrorStateString -> {
                        displayError(it.error, bubbleLayout)
                    }

                    else -> {}
                }
            }
        }
        wordTextView.text = word?.original
        if (!dictionary?.tags.isNullOrEmpty()) {
            bubbleLayout.removeAllViews()
        }
        dictionary?.let {
            for (tag in it.tags) {
                val select = word?.tags?.find { it._id == tag._id } != null
                addTag(tag, select)
            }
        }
    }

    private fun addTag(tag: WordTag, selected: Boolean) {
        val bubbleView = BubbleView(requireContext())
        bubbleView.setWordTag(tag)
        bubbleView.select(selected)
        bubbleLayout.addView(bubbleView)
    }

    private fun createTag(tag: String?) {
        lifecycleScope.launch {
            viewModel.addTag(context, tag).collect {
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
                            bubbleLayout
                        )
                    }

                    is FetchDataState.DataState -> {
                        val createdTag = it.data
                        Log.d(TAG, "tag added: $createdTag")
                        addTag(createdTag, true)
                    }

                    is FetchDataState.ErrorStateString -> {
                        displayError(it.error, bubbleLayout)
                    }
                }
            }
        }
    }
}
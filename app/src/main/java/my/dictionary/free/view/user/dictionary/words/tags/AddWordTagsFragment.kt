package my.dictionary.free.view.user.dictionary.words.tags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.WordTag
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.words.tags.AddWordTagsViewModel
import my.dictionary.free.view.AbstractBaseFragment
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
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.displayErrorUIState.drop(1).collect { errorMessage ->
                        displayError(errorMessage, bubbleLayout)
                    }
                }
                launch {
                    viewModel.loadingUIState.collect { visible ->
                        sharedViewModel.loading(visible)
                    }
                }
                launch {
                    viewModel.createdTagUIState.drop(1).collect { tag ->
                        Log.d(TAG, "tag added: $tag")
                        addTag(tag)
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_tags, { menu, mi -> }, {
            when (it) {
                R.id.nav_save_tags -> {

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
                                viewModel.addTag(context, tagName)
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
        viewModel.loadData(context, word, dictionary)
        wordTextView.text = word?.original
        dictionary?.let {
            for (tag in it.tags) {
                addTag(tag)
            }
        }
    }

    private fun addTag(tag: WordTag) {
        val bubbleView = BubbleView(requireContext())
        bubbleView.setText(tag.tagName)
        bubbleLayout.addView(bubbleView)
    }
}
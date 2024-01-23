package my.dictionary.free.view.user.dictionary.words.tags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import my.dictionary.free.R
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment

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
    private var word: Word? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_word_tags, null)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        addMenuProvider(R.menu.menu_word_category, { menu, mi -> }, {
            when (it) {
                R.id.nav_save_categories -> {

                    return@addMenuProvider true
                }

                else -> false
            }
        })
        word = if (hasTiramisu()) arguments?.getParcelable(
            AddDictionaryWordFragment.BUNDLE_WORD,
            Word::class.java
        ) else arguments?.getParcelable(BUNDLE_WORD) as? Word
    }
}
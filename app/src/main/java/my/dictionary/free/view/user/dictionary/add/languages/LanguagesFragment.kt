package my.dictionary.free.view.user.dictionary.add.languages

import android.app.SearchManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
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
import my.dictionary.free.domain.models.language.Language
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.add.languages.LanguagesViewModel
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.widget.ListItemDecoration

@AndroidEntryPoint
class LanguagesFragment : Fragment() {

    companion object {
        const val BUNDLE_LANGUAGE_SELECT_RESULT =
            "my.dictionary.free.view.user.dictionary.add.languages.LanguagesFragment.BUNDLE_LANGUAGE_SELECT_RESULT"
        const val BUNDLE_LANGUAGE_SELECT_KEY =
            "my.dictionary.free.view.user.dictionary.add.languages.LanguagesFragment.BUNDLE_LANGUAGE_SELECT_KEY"
        const val BUNDLE_LANGUAGE_TYPE_KEY =
            "my.dictionary.free.view.user.dictionary.add.languages.LanguagesFragment.BUNDLE_LANGUAGE_TYPE_KEY"
    }

    private lateinit var recyclerViewLanguages: RecyclerView

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: LanguagesViewModel by viewModels()
    private var langType: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_languages, null)
        recyclerViewLanguages = view.findViewById(R.id.recycler_view)
        recyclerViewLanguages.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewLanguages.addItemDecoration(ListItemDecoration(requireContext()))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.languages.drop(1).collect { languages ->
                        recyclerViewLanguages.adapter = LanguagesAdapter(languages, onLanguageClickListener)
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_languages_search, menuCreated = { menu ->
            val searchManager =
                getSystemService(requireContext(), SearchManager::class.java) as SearchManager
            (menu.findItem(R.id.search).actionView as SearchView).let { searchView ->
                searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(
                        activity?.componentName
                    )
                )
                searchView.setOnQueryTextListener(onLanguagesQueryListener)
            }
        }, callbackMenuClicked = { true }
        )
        langType = arguments?.getInt(BUNDLE_LANGUAGE_TYPE_KEY, 0) ?: 0
        viewModel.loadLanguages(requireContext())
    }

    private val onLanguagesQueryListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            viewModel.queryLanguages(requireContext(), query)
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            viewModel.queryLanguages(requireContext(), newText)
            return true
        }

    }

    private val onLanguageClickListener = object : OnLanguageClickListener {
        override fun onLanguageClick(language: Language) {
            val bundle = Bundle().apply {
                putParcelable(BUNDLE_LANGUAGE_SELECT_KEY, language)
                putInt(BUNDLE_LANGUAGE_TYPE_KEY, langType)
            }
            setFragmentResult(BUNDLE_LANGUAGE_SELECT_RESULT, bundle)
            findNavController().popBackStack()
        }
    }
}
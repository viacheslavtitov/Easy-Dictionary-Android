package my.dictionary.free.view.user.dictionary.choose

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
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.FetchDataState
import my.dictionary.free.view.user.dictionary.UserDictionaryAdapter
import my.dictionary.free.view.widget.OnListItemClickListener
import my.dictionary.free.view.widget.OnListTouchListener

@AndroidEntryPoint
class DictionaryChooseFragment : AbstractBaseFragment() {

    companion object {
        private val TAG = DictionaryChooseFragment::class.simpleName
        const val BUNDLE_DICTIONARY_RESULT = "my.dictionary.free.view.user.dictionary.choose.DictionaryChooseFragment.BUNDLE_DICTIONARY_RESULT"
        const val BUNDLE_DICTIONARY_KEY = "my.dictionary.free.view.user.dictionary.choose.DictionaryChooseFragment.BUNDLE_DICTIONARY_RESULT"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: UserDictionaryViewModel by viewModels()

    private lateinit var dictionariesRecyclerView: RecyclerView
    private var dictionariesAdapter: UserDictionaryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dictionary_choose, container, false)
        dictionariesRecyclerView = view.findViewById(R.id.recycler_view)
        dictionariesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        dictionariesRecyclerView.addOnItemTouchListener(
            OnListTouchListener(
                requireContext(),
                dictionariesRecyclerView,
                onDictionaryClickListener
            )
        )
        dictionariesAdapter = UserDictionaryAdapter(mutableListOf(), mutableListOf())
        dictionariesRecyclerView.adapter = dictionariesAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        refreshDictionaries()
    }

    private fun refreshDictionaries() {
        lifecycleScope.launch {
            viewModel.loadDictionaries(context).collect {
                when (it) {
                    is FetchDataState.StartLoadingState -> {
                        dictionariesAdapter?.clearData()
                        sharedViewModel.loading(true)
                    }

                    is FetchDataState.FinishLoadingState -> {
                        sharedViewModel.loading(false)
                    }

                    is FetchDataState.ErrorState -> {
                        displayError(
                            it.exception.message ?: context?.getString(R.string.unknown_error),
                            dictionariesRecyclerView
                        )
                    }

                    is FetchDataState.DataState -> {
                        dictionariesAdapter?.add(it.data)
                    }

                    is FetchDataState.ErrorStateString -> {
                        displayError(it.error, dictionariesRecyclerView)
                    }
                }
            }
        }
    }

    private val onDictionaryClickListener = object : OnListItemClickListener {
        override fun onListItemClick(childView: View) {
            dictionariesAdapter?.getItemByPosition(
                dictionariesRecyclerView.getChildAdapterPosition(childView)
            )?.let { dictionary ->
                val bundle = Bundle().apply {
                    putParcelable(BUNDLE_DICTIONARY_KEY, dictionary)
                }
                setFragmentResult(BUNDLE_DICTIONARY_RESULT, bundle)
                findNavController().popBackStack()
            }
        }

        override fun onListItemLongClick(childView: View) {
            //ignore
        }
    }
}
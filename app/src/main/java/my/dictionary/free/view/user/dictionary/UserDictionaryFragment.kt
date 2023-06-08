package my.dictionary.free.view.user.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.navigation.AddUserDictionaryScreen
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import my.dictionary.free.view.ext.addMenuProvider

@AndroidEntryPoint
class UserDictionaryFragment : Fragment() {

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: UserDictionaryViewModel by viewModels()

    private lateinit var dictionariesRecyclerView: RecyclerView
    private var dictionariesAdapter: UserDictionaryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_dictionary, null)
        dictionariesRecyclerView = view.findViewById(R.id.recycler_view)
        dictionariesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemTouchHelper = ItemTouchHelper(SwipeDictionaryItem(requireContext()))
        itemTouchHelper.attachToRecyclerView(dictionariesRecyclerView)
        viewModel.dictionaries.observe(requireActivity()) { dictionaries ->
            dictionariesAdapter = UserDictionaryAdapter(dictionaries, onDictionaryClickListener)
            dictionariesRecyclerView.adapter = dictionariesAdapter
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addMenuProvider(R.menu.menu_user_dictionary, { }, {
            when (it) {
                R.id.nav_add_dictionary -> {
                    sharedViewModel.navigateTo(AddUserDictionaryScreen())
                    return@addMenuProvider true
                }
                else -> false
            }
        })
        viewModel.loadDictionaries(requireContext())
    }

    private val onDictionaryClickListener = object : OnDictionaryClickListener {
        override fun onDictionaryClick(dictionary: Dictionary) {

        }
    }
}
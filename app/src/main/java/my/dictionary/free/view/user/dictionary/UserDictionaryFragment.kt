package my.dictionary.free.view.user.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.dictionary.free.R
import my.dictionary.free.domain.models.navigation.AddUserDictionaryScreen
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import my.dictionary.free.view.ext.addMenuProvider

@AndroidEntryPoint
class UserDictionaryFragment : Fragment() {

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: UserDictionaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_dictionary, null)
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
        viewModel.loadDictionaries()
    }
}
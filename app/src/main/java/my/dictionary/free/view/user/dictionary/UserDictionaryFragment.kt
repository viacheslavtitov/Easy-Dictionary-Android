package my.dictionary.free.view.user.dictionary

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import dagger.hilt.android.AndroidEntryPoint
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.navigation.AddUserDictionaryScreen
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.widget.OnItemSwipedListener
import my.dictionary.free.view.widget.SimpleItemDecoration

@AndroidEntryPoint
class UserDictionaryFragment : Fragment() {

    companion object {
        const val UNDO_SNACKBAR_DURATION: Int = 5000
        const val UNDO_SNACKBAR_DURATION_TIMER = 6000L
        const val UNDO_SNACKBAR_INTERVAL_TIMER = 1000L
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: UserDictionaryViewModel by viewModels()

    private lateinit var dictionariesRecyclerView: RecyclerView
    private var dictionariesAdapter: UserDictionaryAdapter? = null
    private var undoRemoveDictionarySnackbar: Snackbar? = null

    private val undoRemoveDictionaryTimer = object: CountDownTimer(UNDO_SNACKBAR_DURATION_TIMER, UNDO_SNACKBAR_INTERVAL_TIMER) {
        @SuppressLint("RestrictedApi", "SetTextI18n")
        override fun onTick(millisUntilFinished: Long) {
            val seconds = millisUntilFinished / 1000
            undoRemoveDictionarySnackbar?.let {
                ((it.view as ViewGroup).getChildAt(0) as SnackbarContentLayout?)?.messageView?.text = "$seconds ${getString(R.string.seconds)}"
            }
        }

        override fun onFinish() {
            undoRemoveDictionarySnackbar = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_dictionary, null)
        dictionariesRecyclerView = view.findViewById(R.id.recycler_view)
        dictionariesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemTouchHelper =
            ItemTouchHelper(SwipeDictionaryItem(requireContext(), onItemSwipedListener))
        itemTouchHelper.attachToRecyclerView(dictionariesRecyclerView)
        dictionariesRecyclerView.addItemDecoration(SimpleItemDecoration(requireContext()))
        viewModel.dictionaries.observe(requireActivity()) { dictionaries ->
            dictionariesAdapter =
                UserDictionaryAdapter(dictionaries.toMutableList(), onDictionaryClickListener)
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

    private val onItemSwipedListener = object : OnItemSwipedListener {
        override fun onSwiped(position: Int) {
            dictionariesAdapter?.temporaryRemoveItem(position)
            undoRemoveDictionarySnackbar = Snackbar.make(dictionariesRecyclerView, R.string.seconds, Snackbar.LENGTH_INDEFINITE)
                .setDuration(UNDO_SNACKBAR_DURATION)
                .setAction(R.string.undo) {
                    dictionariesAdapter?.undoRemovedItem()
                }
            undoRemoveDictionarySnackbar?.show()
            undoRemoveDictionaryTimer.start()
        }
    }

    override fun onStop() {
        undoRemoveDictionarySnackbar?.dismiss()
        super.onStop()
    }
}
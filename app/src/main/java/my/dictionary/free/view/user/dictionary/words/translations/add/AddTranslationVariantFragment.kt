package my.dictionary.free.view.user.dictionary.words.translations.add

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.utils.hasTiramisu
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.domain.viewmodels.user.dictionary.translations.AddTranslationVariantViewModel
import my.dictionary.free.view.AbstractBaseFragment
import my.dictionary.free.view.dialogs.DialogBuilders
import my.dictionary.free.view.dialogs.InputDialogListener
import my.dictionary.free.view.ext.addMenuProvider
import my.dictionary.free.view.ext.findAndDismissDialog
import my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment

@AndroidEntryPoint
class AddTranslationVariantFragment : AbstractBaseFragment() {
    companion object {
        private val TAG = AddTranslationVariantFragment::class.simpleName
        const val BUNDLE_TRANSLATE_WORD =
            "my.dictionary.free.view.user.dictionary.words.translations.add.AddTranslationVariantFragment.BUNDLE_TRANSLATE_WORD"
        const val BUNDLE_TRANSLATION =
            "my.dictionary.free.view.user.dictionary.words.translations.add.AddTranslationVariantFragment.BUNDLE_TRANSLATION"
        const val BUNDLE_DICTIONARY_ID =
            "my.dictionary.free.view.user.dictionary.words.translations.add.AddTranslationVariantFragment.BUNDLE_DICTIONARY_ID"
    }

    private val sharedViewModel: SharedMainViewModel by activityViewModels()
    private val viewModel: AddTranslationVariantViewModel by viewModels()

    private lateinit var translateWordTextView: AppCompatTextView
    private lateinit var textInputLayoutTranslation: TextInputLayout
    private lateinit var textInputEditTextTranslation: TextInputEditText
    private lateinit var textInputLayoutExample: TextInputLayout
    private lateinit var textInputEditTextExample: TextInputEditText
    private lateinit var chooseCategorySpinner: AppCompatSpinner
    private lateinit var rootView: View
    private var categoryAdapter: CategorySpinnerAdapter? = null

    private var translationWord: String? = null
    private var editTranslationVariant: TranslationVariant? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_new_translation_variant, null)
        translateWordTextView = view.findViewById(R.id.translate_word)
        textInputLayoutTranslation = view.findViewById(R.id.text_input_translation)
        textInputEditTextTranslation = view.findViewById(R.id.edit_text_translation)
        textInputLayoutExample = view.findViewById(R.id.text_input_example)
        textInputEditTextExample = view.findViewById(R.id.edit_text_example)
        chooseCategorySpinner = view.findViewById(R.id.choose_category)
        rootView = view.findViewById(R.id.root)
        view.findViewById<View>(R.id.add_category_variant).setOnClickListener {
            val dialog = DialogBuilders.InputDialogBuilder
                .cancelButtonTitle(getString(R.string.cancel))
                .title(getString(R.string.add_category_name))
                .okButtonTitle(getString(R.string.ok))
                .listener(object : InputDialogListener {
                    var categoryName: String? = null
                    override fun onTextChanged(newText: String?) {
                        categoryName = newText
                    }

                    override fun onCancelClicked() {
                    }

                    override fun onOkButtonClicked() {
                        viewModel.createCategory(context, categoryName)
                    }
                }).build()
            childFragmentManager.findAndDismissDialog("InputDialog")
            dialog.show(childFragmentManager, "InputDialog")
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        context?.let {
            categoryAdapter = CategorySpinnerAdapter(it)
            chooseCategorySpinner.adapter = categoryAdapter
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.displayErrorUIState.drop(1).collect { errorMessage ->
                        displayError(errorMessage, rootView)
                    }
                }
                launch {
                    viewModel.loadingUIState.collect { visible ->

                    }
                }
                launch {
                    viewModel.shouldClearCategoriesUIState.collect { clear ->
                        if (clear) {
                            categoryAdapter?.clear()
                        }
                    }
                }
                launch {
                    viewModel.successCreateCategoryUIState.collect { success ->
                        if (success) {
                            viewModel.loadCategories(context)
                        }
                    }
                }
                launch {
                    viewModel.validateTranslation.collect { error ->
                        textInputLayoutTranslation.error = error
                    }
                }
                launch {
                    viewModel.categoriesUIState.collect { category ->
                        categoryAdapter?.add(category)
                    }
                }
                launch {
                    viewModel.exampleUIState.collect { value ->
                        Log.d(TAG, "example loaded $value")
                        textInputEditTextExample.setText(value)
                    }
                }
                launch {
                    viewModel.translationUIState.collect { value ->
                        Log.d(TAG, "translation loaded $value")
                        textInputEditTextTranslation.setText(value)
                    }
                }
                launch {
                    viewModel.categoryUIState.drop(1).collect { category ->
                        val categoryPosition = categoryAdapter?.findPositionItem(category) ?: 0
                        Log.d(TAG, "category loaded by position $categoryPosition")
                        chooseCategorySpinner.setSelection(categoryPosition)
                    }
                }
                launch {
                    viewModel.updateUIState.collect { success ->
                        if (success) {
                            Log.d(TAG, "translation was updated")
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
        addMenuProvider(R.menu.menu_add_translation_variant, { menu, mi -> }, {
            when (it) {
                R.id.nav_save_translation_variant -> {
                    val translation = textInputEditTextTranslation.text?.toString()
                    if (viewModel.validateTranslation(context, translation)) {
                        val categoryTemp =
                            categoryAdapter?.getItemByPosition(chooseCategorySpinner.selectedItemPosition)
                        val category = if (categoryTemp?._id == null) null else categoryTemp
                        if (viewModel.isEditMode()) {
                            viewModel.updateTranslation(
                                context,
                                translation!!,
                                textInputEditTextExample.text?.toString(),
                                category
                            )
                        } else {
                            val result = viewModel.generateTranslation(
                                translation!!,
                                textInputEditTextExample.text?.toString(),
                                category
                            )
                            val bundle = Bundle().apply {
                                putParcelable(
                                    AddDictionaryWordFragment.BUNDLE_TRANSLATION_VARIANT,
                                    result
                                )
                                putParcelable(
                                    AddDictionaryWordFragment.BUNDLE_TRANSLATION_VARIANT_CATEGORY,
                                    category
                                )
                            }
                            setFragmentResult(
                                AddDictionaryWordFragment.BUNDLE_TRANSLATION_VARIANT_RESULT,
                                bundle
                            )
                            findNavController().popBackStack()
                        }
                    }
                    return@addMenuProvider true
                }

                else -> false
            }
        })
        translationWord = arguments?.getString(BUNDLE_TRANSLATE_WORD, null)
        val dictionaryId = arguments?.getString(BUNDLE_DICTIONARY_ID, null)
        translateWordTextView.text = translationWord
        editTranslationVariant = if (hasTiramisu()) arguments?.getParcelable(
            BUNDLE_TRANSLATION,
            TranslationVariant::class.java
        ) else arguments?.getParcelable(BUNDLE_TRANSLATION) as? TranslationVariant
        editTranslationVariant?.dictionaryId = dictionaryId
        viewModel.setEditModel(editTranslationVariant)
        viewModel.loadCategories(context)
    }
}
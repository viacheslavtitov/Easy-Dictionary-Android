package my.dictionary.free.view.dialogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import my.dictionary.free.R

class InputDialogFragment(override val listener: InputDialogListener? = null) :
    TwoButtonDialogFragment() {

    companion object {
        fun create(
            title: String?,
            description: String?,
            iconRes: Int?,
            iconTintColorRes: Int? = R.color.main_dark,
            buttonOkTitle: String?,
            buttonCancelTitle: String?,
            listener: InputDialogListener?
        ): InputDialogFragment {
            val bundle = Bundle()
            bundle.putString(BUNDLE_TITLE, title)
            bundle.putString(BUNDLE_DESCRIPTION, description)
            bundle.putString(BUNDLE_BUTTON_OK_TITLE, buttonOkTitle)
            bundle.putString(BUNDLE_BUTTON_CANCEL_TITLE, buttonCancelTitle)
            bundle.putInt(BUNDLE_ICON_RES, iconRes ?: -1)
            bundle.putInt(BUNDLE_ICON_TINT_COLOR, iconTintColorRes ?: R.color.main_dark)

            val instanceDialog = InputDialogFragment(listener)
            instanceDialog.arguments = bundle
            return instanceDialog
        }
    }

    private var textInputLayout: TextInputLayout? = null
    private var textInputEditText: TextInputEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dialog_input, container, false)
        initView(rootView)
        cancelButton = rootView.findViewById(R.id.btn_cancel)
        textInputLayout = rootView.findViewById(R.id.text_input_layout)
        textInputEditText = rootView.findViewById(R.id.edit_text_layout)
        textInputEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                listener?.onTextChanged(text?.toString())
            }

        })
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

interface IInputDialogBuilder : ITwoButtonDialogBuilder {

    fun listener(listener: InputDialogListener?): IInputDialogBuilder

    override fun build(): InputDialogFragment
}

interface InputDialogListener : TwoButtonsDialogListener {
    fun onTextChanged(newText: String?)
}
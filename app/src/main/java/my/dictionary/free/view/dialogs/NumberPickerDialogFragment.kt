package my.dictionary.free.view.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import my.dictionary.free.R
import my.dictionary.free.view.ext.dp

class NumberPickerDialogFragment(
    override val listener: ValueDialogListener? = null
) : TwoButtonDialogFragment() {

    companion object {
        const val BUNDLE_MAX_VALUE =
            "my.dictionary.free.view.dialogs.NumberPickerDialogFragment.BUNDLE_MAX_VALUE"
        const val BUNDLE_MIN_VALUE =
            "my.dictionary.free.view.dialogs.NumberPickerDialogFragment.BUNDLE_MIN_VALUE"

        private const val DEFAULT_MIN_VALUE = 0
        private const val DEFAULT_MAX_VALUE = 10

        fun create(
            title: String?,
            description: String?,
            iconRes: Int?,
            iconTintColorRes: Int? = R.color.main_dark,
            buttonOkTitle: String?,
            buttonCancelTitle: String?,
            maxValue: Int?,
            minValue: Int?,
            listener: ValueDialogListener?
        ): NumberPickerDialogFragment {
            val bundle = Bundle()
            bundle.putString(BUNDLE_TITLE, title)
            bundle.putString(BUNDLE_DESCRIPTION, description)
            bundle.putString(BUNDLE_BUTTON_OK_TITLE, buttonOkTitle)
            bundle.putString(BUNDLE_BUTTON_CANCEL_TITLE, buttonCancelTitle)
            bundle.putInt(BUNDLE_ICON_RES, iconRes ?: -1)
            bundle.putInt(BUNDLE_ICON_TINT_COLOR, iconTintColorRes ?: R.color.main_dark)
            bundle.putInt(BUNDLE_MAX_VALUE, maxValue ?: DEFAULT_MAX_VALUE)
            bundle.putInt(BUNDLE_MIN_VALUE, minValue ?: DEFAULT_MIN_VALUE)

            val instanceDialog = NumberPickerDialogFragment(listener)
            instanceDialog.arguments = bundle
            return instanceDialog
        }
    }

    private var minNumberValue: Int? = null
    private var maxNumberValue: Int? = null
    private var numberPicker: NumberPicker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        minNumberValue = arguments?.getInt(BUNDLE_MIN_VALUE, DEFAULT_MIN_VALUE)
        maxNumberValue = arguments?.getInt(BUNDLE_MAX_VALUE, DEFAULT_MAX_VALUE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dialog_number_picker, container, false)
        initView(rootView)
        cancelButton = rootView.findViewById(R.id.btn_cancel)
        numberPicker = rootView.findViewById(R.id.number_picker)
        numberPicker?.textColor = R.color.black
        numberPicker?.textSize = 20.dp.toFloat()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        minNumberValue?.let { value ->
            numberPicker?.minValue = value
        }
        maxNumberValue?.let { value ->
            numberPicker?.maxValue = value
        }
        view.findViewById<View>(R.id.btn_ok).setOnClickListener {
            listener?.onValueChanged(numberPicker?.value ?: 0)
            this.dismiss()
        }
    }
}

interface INumberPickerDialogBuilder : ITwoButtonDialogBuilder {
    fun listener(listener: ValueDialogListener?): INumberPickerDialogBuilder
    fun minValue(value: Int?): INumberPickerDialogBuilder
    fun maxValue(value: Int?): INumberPickerDialogBuilder

    override fun build(): NumberPickerDialogFragment
}

interface ValueDialogListener : TwoButtonsDialogListener {
    fun onValueChanged(value: Int)
}
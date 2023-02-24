package my.dictionary.free.view.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import my.dictionary.free.R
import my.dictionary.free.view.ext.visible

class TwoButtonDialogFragment(
    override val listener: TwoButtonsDialogListener? = null
) : SimpleInfoDialogFragment(listener) {

    companion object {
        private const val BUNDLE_BUTTON_CANCEL_TITLE =
            "my.dictionary.free.view.dialogs.TwoButtonDialogFragment.BUNDLE_BUTTON_CANCEL_TITLE"

        fun create(
            title: String?,
            description: String?,
            iconRes: Int?,
            iconTintColorRes: Int? = R.color.main_dark,
            buttonOkTitle: String?,
            buttonCancelTitle: String?,
            listener: TwoButtonsDialogListener?
        ): TwoButtonDialogFragment {
            val bundle = Bundle()
            bundle.putString(BUNDLE_TITLE, title)
            bundle.putString(BUNDLE_DESCRIPTION, description)
            bundle.putString(BUNDLE_BUTTON_OK_TITLE, buttonOkTitle)
            bundle.putString(BUNDLE_BUTTON_CANCEL_TITLE, buttonCancelTitle)
            bundle.putInt(BUNDLE_ICON_RES, iconRes ?: -1)
            bundle.putInt(BUNDLE_ICON_TINT_COLOR, iconTintColorRes ?: R.color.main_dark)

            val instanceDialog = TwoButtonDialogFragment(listener)
            instanceDialog.arguments = bundle
            return instanceDialog
        }
    }

    var cancelButtonTitle: String? = null

    var cancelButton: AppCompatTextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelButtonTitle = arguments?.getString(BUNDLE_BUTTON_CANCEL_TITLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dialog_two_button, container, false)
        initView(rootView)
        cancelButton = rootView.findViewById(R.id.btn_cancel)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancelButton?.let {
            it.visible(!cancelButtonTitle.isNullOrEmpty(), View.GONE)
            cancelButtonTitle?.let { title ->
                cancelButton?.text = title
            }
            it.setOnClickListener {
                listener?.onCancelClicked()
                this.dismiss()
            }
        }
    }
}

interface ITwoButtonDialogBuilder : ISimpleInfoDialogBuilder {

    fun cancelButtonTitle(cancelButtonTitle: String?): ITwoButtonDialogBuilder
    fun listener(listener: TwoButtonsDialogListener?): ITwoButtonDialogBuilder

    override fun build(): TwoButtonDialogFragment
}

interface TwoButtonsDialogListener : SimpleInfoDialogListener {
    fun onCancelClicked()
}
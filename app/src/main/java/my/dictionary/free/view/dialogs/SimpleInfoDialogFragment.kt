package my.dictionary.free.view.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import my.dictionary.free.R
import my.dictionary.free.view.ext.setTint
import my.dictionary.free.view.ext.visible

open class SimpleInfoDialogFragment(
    open val listener: SimpleInfoDialogListener?
) : AbstractBaseDialogFragment() {

    companion object {
        const val BUNDLE_TITLE =
            "my.dictionary.free.view.dialogs.SimpleInfoDialog.BUNDLE_TITLE"
        const val BUNDLE_DESCRIPTION =
            "my.dictionary.free.view.dialogs.SimpleInfoDialog.BUNDLE_DESCRIPTION"
        const val BUNDLE_ICON_RES =
            "my.dictionary.free.view.dialogs.SimpleInfoDialog.BUNDLE_ICON_RES"
        const val BUNDLE_ICON_TINT_COLOR =
            "my.dictionary.free.view.dialogs.SimpleInfoDialog.BUNDLE_ICON_TINT_COLOR"
        const val BUNDLE_BUTTON_OK_TITLE =
            "my.dictionary.free.view.dialogs.SimpleInfoDialog.BUNDLE_BUTTON_OK_TITLE"

        fun create(
            title: String?,
            description: String?,
            iconRes: Int?,
            iconTintColorRes: Int? = R.color.main_dark,
            buttonOkTitle: String?,
            listener: SimpleInfoDialogListener?
        ): SimpleInfoDialogFragment {
            val bundle = Bundle()
            bundle.putString(BUNDLE_TITLE, title)
            bundle.putString(BUNDLE_DESCRIPTION, description)
            bundle.putString(BUNDLE_BUTTON_OK_TITLE, buttonOkTitle)
            bundle.putInt(BUNDLE_ICON_RES, iconRes ?: -1)
            bundle.putInt(BUNDLE_ICON_TINT_COLOR, iconTintColorRes ?: R.color.main_dark)

            val instanceDialog = SimpleInfoDialogFragment(listener)
            instanceDialog.arguments = bundle
            return instanceDialog
        }
    }

    var title: String? = null
    var description: String? = null
    var okButtonTitle: String? = null
    var iconRes: Int? = null
    var iconTintColorRes: Int? = R.color.main_dark

    var iconImageView: AppCompatImageView? = null
    var titleTextView: AppCompatTextView? = null
    var descriptionTextView: AppCompatTextView? = null
    var okButton: AppCompatTextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString(BUNDLE_TITLE)
        description = arguments?.getString(BUNDLE_DESCRIPTION)
        okButtonTitle = arguments?.getString(BUNDLE_BUTTON_OK_TITLE)
        iconRes = arguments?.getInt(BUNDLE_ICON_RES, -1)
        iconTintColorRes = arguments?.getInt(BUNDLE_ICON_TINT_COLOR, -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dialog_simple, container, false)
        initView(rootView)
        return rootView
    }

    fun initView(rootView: View) {
        iconImageView = rootView.findViewById(R.id.icon)
        titleTextView = rootView.findViewById(R.id.title)
        descriptionTextView = rootView.findViewById(R.id.description)
        okButton = rootView.findViewById(R.id.btn_ok)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iconImageView?.visible(iconRes != null && iconRes!! > 0, View.GONE)
        iconRes?.let {
            if (it > 0) {
                iconImageView?.setImageResource(it)
            }
        }
        iconTintColorRes?.let {
            if (it > 0) {
                iconImageView?.setTint(it)
            }
        }

        titleTextView?.visible(!title.isNullOrEmpty(), View.GONE)
        title?.let {
            titleTextView?.text = it
        }

        descriptionTextView?.visible(!description.isNullOrEmpty(), View.GONE)
        description?.let {
            descriptionTextView?.text = it
        }

        okButton?.let {
            it.visible(!okButtonTitle.isNullOrEmpty(), View.GONE)
            okButtonTitle?.let { title ->
                okButton?.text = title
            }
            it.setOnClickListener {
                listener?.onOkButtonClicked()
                this.dismiss()
            }
        }
    }

}

interface ISimpleInfoDialogBuilder {

    fun listener(listener: SimpleInfoDialogListener?): ISimpleInfoDialogBuilder
    fun title(title: String?): ISimpleInfoDialogBuilder
    fun description(description: String?): ISimpleInfoDialogBuilder
    fun okButtonTitle(okButtonTitle: String?): ISimpleInfoDialogBuilder
    fun iconRes(iconRes: Int?): ISimpleInfoDialogBuilder
    fun iconTintColorRes(iconTintColorRes: Int?): ISimpleInfoDialogBuilder

    fun build(): SimpleInfoDialogFragment
}

interface SimpleInfoDialogListener {
    fun onOkButtonClicked()
}
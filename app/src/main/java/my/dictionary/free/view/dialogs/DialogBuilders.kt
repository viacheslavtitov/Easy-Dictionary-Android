package my.dictionary.free.view.dialogs

class DialogBuilders {

    abstract class AbstractDialogBuilder : ISimpleInfoDialogBuilder {

        internal var title: String? = null
        internal var description: String? = null
        internal var okButtonTitle: String? = null
        internal var iconRes: Int? = null
        internal var iconTintColorRes: Int? = null
        internal var listener: SimpleInfoDialogListener? = null

        override fun listener(listener: SimpleInfoDialogListener?): ISimpleInfoDialogBuilder {
            this.listener = listener
            return this
        }

        override fun title(title: String?): ISimpleInfoDialogBuilder {
            this.title = title
            return this
        }

        override fun description(description: String?): ISimpleInfoDialogBuilder {
            this.description = description
            return this
        }

        override fun okButtonTitle(okButtonTitle: String?): ISimpleInfoDialogBuilder {
            this.okButtonTitle = okButtonTitle
            return this
        }

        override fun iconRes(iconRes: Int?): ISimpleInfoDialogBuilder {
            this.iconRes = iconRes
            return this
        }

        override fun iconTintColorRes(iconTintColorRes: Int?): ISimpleInfoDialogBuilder {
            this.iconTintColorRes = iconTintColorRes
            return this
        }
    }

    object SimpleInfoDialogBuilder : AbstractDialogBuilder(), ISimpleInfoDialogBuilder {

        override fun build(): SimpleInfoDialogFragment {
            return SimpleInfoDialogFragment.create(
                title = title,
                description = description,
                iconRes = iconRes,
                iconTintColorRes = iconTintColorRes,
                buttonOkTitle = okButtonTitle,
                listener = listener
            )
        }
    }

    object TwoButtonDialogBuilder : AbstractDialogBuilder(), ITwoButtonDialogBuilder {

        private var buttonCancelTitle: String? = null

        override fun cancelButtonTitle(cancelButtonTitle: String?): ITwoButtonDialogBuilder {
            this.buttonCancelTitle = cancelButtonTitle
            return this
        }

        override fun listener(listener: TwoButtonsDialogListener?): ITwoButtonDialogBuilder {
            this.listener = listener
            return this
        }

        override fun title(title: String?): ITwoButtonDialogBuilder {
            return super.title(title) as ITwoButtonDialogBuilder
        }

        override fun description(description: String?): ITwoButtonDialogBuilder {
            return super.description(description) as ITwoButtonDialogBuilder
        }

        override fun okButtonTitle(okButtonTitle: String?): ITwoButtonDialogBuilder {
            return super.okButtonTitle(okButtonTitle) as ITwoButtonDialogBuilder
        }

        override fun iconRes(iconRes: Int?): ITwoButtonDialogBuilder {
            return super.iconRes(iconRes) as ITwoButtonDialogBuilder
        }

        override fun iconTintColorRes(iconTintColorRes: Int?): ISimpleInfoDialogBuilder {
            return super.iconTintColorRes(iconTintColorRes) as ITwoButtonDialogBuilder
        }

        override fun build(): TwoButtonDialogFragment {
            return TwoButtonDialogFragment.create(
                title = title,
                description = description,
                iconRes = iconRes,
                iconTintColorRes = iconTintColorRes,
                buttonOkTitle = okButtonTitle,
                buttonCancelTitle = buttonCancelTitle,
                listener = listener as TwoButtonsDialogListener?
            )
        }
    }

    object InputDialogBuilder : AbstractDialogBuilder(), IInputDialogBuilder {

        private var buttonCancelTitle: String? = null

        override fun cancelButtonTitle(cancelButtonTitle: String?): IInputDialogBuilder {
            this.buttonCancelTitle = cancelButtonTitle
            return this
        }

        override fun listener(listener: InputDialogListener?): IInputDialogBuilder {
            this.listener = listener
            return this
        }

        override fun listener(listener: TwoButtonsDialogListener?): ITwoButtonDialogBuilder {
            this.listener = listener
            return this
        }

        override fun title(title: String?): IInputDialogBuilder {
            return super.title(title) as IInputDialogBuilder
        }

        override fun description(description: String?): IInputDialogBuilder {
            return super.description(description) as IInputDialogBuilder
        }

        override fun okButtonTitle(okButtonTitle: String?): IInputDialogBuilder {
            return super.okButtonTitle(okButtonTitle) as IInputDialogBuilder
        }

        override fun iconRes(iconRes: Int?): IInputDialogBuilder {
            return super.iconRes(iconRes) as IInputDialogBuilder
        }

        override fun iconTintColorRes(iconTintColorRes: Int?): IInputDialogBuilder {
            return super.iconTintColorRes(iconTintColorRes) as IInputDialogBuilder
        }

        override fun build(): InputDialogFragment {
            return InputDialogFragment.create(
                title = title,
                description = description,
                iconRes = iconRes,
                iconTintColorRes = iconTintColorRes,
                buttonOkTitle = okButtonTitle,
                buttonCancelTitle = buttonCancelTitle,
                listener = listener as InputDialogListener?
            )
        }
    }
}
package my.dictionary.free.view.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import my.dictionary.free.R
import my.dictionary.free.view.AbstractBaseActivity
import my.dictionary.free.view.dialogs.DialogBuilders
import my.dictionary.free.view.dialogs.SimpleInfoDialogListener
import my.dictionary.free.view.dialogs.TwoButtonsDialogListener
import my.dictionary.free.view.ext.visibleSystemBars
import my.dictionary.free.view.main.MainActivity

class SplashActivity : AbstractBaseActivity() {

    companion object {
        private const val SIGN_IN_DIALOG_ERROR = "SIGN_IN_DIALOG_ERROR"
    }

    private var motionLayout: MotionLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        visibleSystemBars(visible = false, type = WindowInsetsCompat.Type.statusBars())
        visibleSystemBars(visible = false, type = WindowInsetsCompat.Type.systemBars())
        motionLayout = findViewById(R.id.motionLayout)
        runAnimation()
    }

    private fun runAnimation() {
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            withContext(Dispatchers.Main) {
                motionLayout?.apply {
                    setTransition(R.id.first, R.id.second)
                    transitionToEnd {
                        setTransition(R.id.second, R.id.third)
                        transitionToEnd {
                            setTransition(R.id.third, R.id.fourth)
                            transitionToEnd {
                                signOrToMain()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun signOrToMain() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // already signed in
            launchMainActivity()
        } else {
            signIn()
        }
    }

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
        )
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun launchMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            launchMainActivity()
        } else {
            when (response?.error?.errorCode) {
                ErrorCodes.NO_NETWORK -> {
                    //Sign in failed due to lack of network connection
                }
                ErrorCodes.PLAY_SERVICES_UPDATE_CANCELLED -> {
                    //A required update to Play Services was cancelled by the user.
                }
                ErrorCodes.DEVELOPER_ERROR -> {
                    //A sign-in operation couldn't be completed due to a developer error.
                }
                ErrorCodes.PROVIDER_ERROR -> {
                    //An external sign-in provider error occurred.
                }
                ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT -> {
                    //Anonymous account linking failed.
                }
                ErrorCodes.EMAIL_MISMATCH_ERROR -> {
                    //Signing in with a different email in the WelcomeBackIdp flow or email link flow.
                }
                ErrorCodes.INVALID_EMAIL_LINK_ERROR -> {
                    //Attempting to sign in with an invalid email link.
                }
                ErrorCodes.EMAIL_LINK_WRONG_DEVICE_ERROR -> {
                    //Attempting to open an email link from a different device.
                }
                ErrorCodes.EMAIL_LINK_PROMPT_FOR_EMAIL_ERROR -> {
                    //We need to prompt the user for their email.
                }
                ErrorCodes.EMAIL_LINK_CROSS_DEVICE_LINKING_ERROR -> {
                    //Cross device linking flow - we need to ask the user if they want to continue linking or just sign in.
                }
                ErrorCodes.EMAIL_LINK_DIFFERENT_ANONYMOUS_USER_ERROR -> {
                    //Attempting to open an email link from the same device, with anonymous upgrade enabled, but the underlying anonymous user has been changed.
                }
                ErrorCodes.ERROR_USER_DISABLED -> {
                    //Attempting to auth with account that is currently disabled in the Firebase console.
                }
                ErrorCodes.ERROR_GENERIC_IDP_RECOVERABLE_ERROR -> {
                    //Recoverable error occurred during the Generic IDP flow.
                }
                ErrorCodes.UNKNOWN_ERROR -> {
                    //An unknown error has occurred.
                }
            }
            val errorMessage = response?.error?.localizedMessage ?: getString(R.string.unknown_error)
            displayAlert(dialogBuilder = DialogBuilders.TwoButtonDialogBuilder
                .cancelButtonTitle(getString(R.string.cancel))
                .title(getString(R.string.error))
                .iconTintColorRes(R.color.error)
                .iconRes(R.drawable.ic_baseline_error_outline_24)
                .description(
                    getString(
                        R.string.sign_in_error_description,
                        errorMessage
                    )
                )
                .okButtonTitle(getString(R.string.retry))
                .listener(object : TwoButtonsDialogListener {
                    override fun onCancelClicked() {
                        finish()
                    }

                    override fun onOkButtonClicked() {
                        signIn()
                    }

                }), tag = SIGN_IN_DIALOG_ERROR
            )
        }
    }
}
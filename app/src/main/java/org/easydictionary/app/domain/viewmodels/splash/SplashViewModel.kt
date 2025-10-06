package org.easydictionary.app.domain.viewmodels.splash

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.easydictionary.app.domain.utils.PreferenceUtils
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferenceUtils: PreferenceUtils
) : ViewModel() {

    companion object {
        private val TAG = SplashViewModel::class.simpleName
    }

    fun isUserSignedIn() = preferenceUtils.checkAccessTokenExist()

}
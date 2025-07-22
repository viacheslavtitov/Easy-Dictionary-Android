package org.easydictionary.app.domain.viewmodels.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    companion object {
        private val TAG = HomeViewModel::class.simpleName
    }
}
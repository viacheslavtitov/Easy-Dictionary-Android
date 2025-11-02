@file:OptIn(ExperimentalCoroutinesApi::class)
package org.easydictionary.app.domain.viewmodels

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.easydictionary.app.MainDispatcherRule
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.auth.Auth
import org.easydictionary.app.domain.usecases.auth.AuthParams
import org.easydictionary.app.domain.usecases.auth.AuthUseCase
import org.easydictionary.app.domain.utils.PreferenceUtils
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.ACCESS_TOKEN_KEY
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.REFRESH_ACCESS_TOKEN_KEY
import org.easydictionary.app.domain.viewmodels.auth.SignInViewModel
import org.easydictionary.app.setUpMockLog
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authUseCase: AuthUseCase
    private lateinit var preferenceUtils: PreferenceUtils

    // SUT
    private lateinit var vm: SignInViewModel

    @Before
    fun setUp() {
        setUpMockLog()
        authUseCase = mockk()
        preferenceUtils = mockk(relaxed = true)
        vm = SignInViewModel(authUseCase, preferenceUtils)
    }

    @Test
    fun `signIn success - do sign in and save token`() = runTest {
        val dto = AuthParams("test@example.com", "Qwerty123", "email", "")
        val access = "access123"
        val refresh = "refresh456"
        val refreshExp = "refreshExp456"
        val domainResult = DomainResult.Success(Auth(access, refresh, refreshExp))

        coEvery { authUseCase.invoke(dto) } returns flowOf(domainResult)

        var successEmitted = false
        val successJob = launch { vm.signedInSuccess.collect { successEmitted = it } }

        vm.signIn(dto.email, dto.password, dto.provider, dto.providerToken)

        assertThat(vm.loadingDataUI.value).isTrue()

        advanceUntilIdle()

        verify { preferenceUtils.putSecureString(ACCESS_TOKEN_KEY, access) }
        verify { preferenceUtils.putSecureString(REFRESH_ACCESS_TOKEN_KEY, refresh) }
        assertThat(successEmitted).isTrue()
        assertThat(vm.loadingDataUI.value).isFalse()

        successJob.cancel()
    }

    @Test
    fun `signIn domain error - validate error message`() = runTest {
        val dto = AuthParams("test@example.com", "Qwerty123", "email", "")
        val err = DomainResult.Error("Bad credentials")
        coEvery { authUseCase.invoke(dto) } returns flowOf(err)

        val errors = mutableListOf<String>()
        val errorJob = launch { vm.errorMessage.collect { errors.add(it) } }

        vm.signIn(dto.email, dto.password, dto.provider, dto.providerToken)
        advanceUntilIdle()

        assertThat(errors).containsExactly("Bad credentials")
        assertThat(vm.loadingDataUI.value).isFalse()
        verify(exactly = 0) { preferenceUtils.putSecureString(any(), any()) }

        errorJob.cancel()
    }

    @Test
    fun `signIn exception - catch error`() = runTest {
        val dto = AuthParams("test@example.com", "Qwerty123", "email", "")
        coEvery { authUseCase.invoke(dto) } returns flow {
            throw IllegalStateException("Any exception")
        }

        val errors = mutableListOf<String>()
        val errorJob = launch { vm.errorMessage.collect { errors.add(it) } }

        vm.signIn(dto.email, dto.password, dto.provider, dto.providerToken)
        advanceUntilIdle()

        assertThat(errors).containsExactly("Any exception")
        assertThat(vm.loadingDataUI.value).isFalse()
        verify(exactly = 0) { preferenceUtils.putSecureString(any(), any()) }

        errorJob.cancel()
    }

    @Test
    fun `displayError directly emit`() = runTest {
        vm.errorMessage.test {
            vm.displayError("Error")
            assertThat(awaitItem()).isEqualTo("Error")
            cancelAndConsumeRemainingEvents()
        }
    }
}

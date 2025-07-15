package org.easydictionary.app.domain.utils

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import android.util.Base64
import org.easydictionary.app.BuildConfig
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Singleton
class PreferenceUtils @Inject constructor(
    context: Context
) {
    companion object {
        private const val NOT_SECRET_SHARED_PREFERENCES = "easy_dictionary_pref"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val IV_SUFFIX = "_iv"
        const val CURRENT_USER_ID = "CURRENT_USER_ID"
        const val CURRENT_USER_UUID = "CURRENT_USER_UUID"
        const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY"
        const val REFRESH_ACCESS_TOKEN_KEY = "REFRESH_ACCESS_TOKEN_KEY"
    }

    /**
     * Instantiate [SharedPreferences] depends on Android version. If Android is Marshmallow or higher
     * it returns [EncryptedSharedPreferences] otherwise SharedPreferences with private mode
     */
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(NOT_SECRET_SHARED_PREFERENCES, Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun getSecretKey(): SecretKey {
        if (!keyStore.containsAlias(BuildConfig.SECURE_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE)

            val keyGenParams = KeyGenParameterSpec.Builder(
                BuildConfig.SECURE_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()

            keyGenerator.init(keyGenParams)
            return keyGenerator.generateKey()
        }
        return (keyStore.getEntry(BuildConfig.SECURE_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }


    fun putSecureString(key: String, value: String) {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

        sharedPreferences.edit {
            putString(key, Base64.encodeToString(encryptedBytes, Base64.DEFAULT))
                .putString(key + IV_SUFFIX, Base64.encodeToString(iv, Base64.DEFAULT))
        }
    }

    fun getSecureString(key: String): String? {
        val encrypted = sharedPreferences.getString(key, null) ?: return null
        val ivBase64 = sharedPreferences.getString(key + IV_SUFFIX, null) ?: return null

        val cipher = Cipher.getInstance(AES_MODE)
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val spec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decryptedBytes = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT))

        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun checkAccessTokenExist() : Boolean {
        return getSecureString(ACCESS_TOKEN_KEY)?.isNotEmpty() == true
    }

    fun getString(
        key: String,
        sharedPreferences: SharedPreferences? = null
    ): String? {
        val spf = sharedPreferences ?: this.sharedPreferences
        return spf.getString(key, null)
    }

    fun setString(
        key: String,
        value: String?,
        sharedPreferences: SharedPreferences? = null
    ) {
        val spf = sharedPreferences ?: this.sharedPreferences
        if (null == value) {
            spf.edit { remove(key) }
            return
        }
        spf.edit { putString(key, value) }
    }

    fun getInt(
        key: String,
        defValue: Int = -1,
        sharedPreferences: SharedPreferences? = null
    ): Int {
        val spf = sharedPreferences ?: this.sharedPreferences
        return spf.getInt(key, defValue)
    }

    fun setInt(
        key: String,
        value: Int?,
        sharedPreferences: SharedPreferences? = null
    ) {
        val spf = sharedPreferences ?: this.sharedPreferences
        if (null == value) {
            spf.edit { remove(key) }
            return
        }
        spf.edit { putInt(key, value) }
    }

    fun getLong(
        key: String,
        defValue: Long = -1,
        sharedPreferences: SharedPreferences? = null
    ): Long {
        val spf = sharedPreferences ?: this.sharedPreferences
        return spf.getLong(key, defValue)
    }

    fun setLong(
        key: String,
        value: Long?,
        sharedPreferences: SharedPreferences? = null
    ) {
        val spf = sharedPreferences ?: this.sharedPreferences
        if (null == value) {
            spf.edit { remove(key) }
            return
        }
        spf.edit { putLong(key, value) }
    }

    fun getBoolean(
        key: String,
        defValue: Boolean = false,
        sharedPreferences: SharedPreferences? = null
    ): Boolean {
        val spf = sharedPreferences ?: this.sharedPreferences
        return spf.getBoolean(key, defValue)
    }

    fun setBoolean(
        key: String,
        value: Boolean?,
        sharedPreferences: SharedPreferences? = null
    ) {
        val spf = sharedPreferences ?: this.sharedPreferences
        if (null == value) {
            spf.edit { remove(key) }
            return
        }
        spf.edit { putBoolean(key, value) }
    }

    fun remove(
        key: String,
        sharedPreferences: SharedPreferences? = null
    ) {
        val spf = sharedPreferences ?: this.sharedPreferences
        spf.edit { remove(key) }
    }

    fun registerListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
        sharedPreferences: SharedPreferences = this.sharedPreferences
    ) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
        sharedPreferences: SharedPreferences = this.sharedPreferences
    ) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }
}
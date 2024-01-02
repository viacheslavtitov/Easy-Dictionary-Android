package my.dictionary.free.domain.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceUtils @Inject constructor(
    context: Context
) {
    companion object {
        private const val SECRET_SHARED_PREFERENCES = "easy_dictionary_encrypt_pref"
        private const val NOT_SECRET_SHARED_PREFERENCES = "easy_dictionary_pref"
        const val CURRENT_USER_ID = "CURRENT_USER_ID"
        const val CURRENT_USER_UUID = "CURRENT_USER_UUID"
    }

    /**
     * Instantiate [SharedPreferences] depends on Android version. If Android is Marshmallow or higher
     * it returns [EncryptedSharedPreferences] otherwise SharedPreferences with private mode
     */
    private val sharedPreferences: SharedPreferences = if(hasMarshmallow()) EncryptedSharedPreferences.create(
        context,
        SECRET_SHARED_PREFERENCES,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    ) else context.getSharedPreferences(NOT_SECRET_SHARED_PREFERENCES, Context.MODE_PRIVATE)

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
            spf.edit().remove(key).apply()
            return
        }
        spf.edit().putString(key, value).apply()
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
            spf.edit().remove(key).apply()
            return
        }
        spf.edit().putInt(key, value).apply()
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
            spf.edit().remove(key).apply()
            return
        }
        spf.edit().putLong(key, value).apply()
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
            spf.edit().remove(key).apply()
            return
        }
        spf.edit().putBoolean(key, value).apply()
    }

    fun remove(
        key: String,
        sharedPreferences: SharedPreferences? = null
    ) {
        val spf = sharedPreferences ?: this.sharedPreferences
        spf.edit().remove(key).apply()
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
        sharedPreferences.edit().clear().apply()
    }
}
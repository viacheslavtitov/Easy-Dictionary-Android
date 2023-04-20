package my.dictionary.free.data.models.users

import com.google.firebase.database.Exclude

class UsersTable(val _id: String, val name: String, val email: String, val providerId: String, val uid: String) {
    companion object {
        const val _NAME = "users"
        const val _ID = "id"
        const val USER_NAME = "name"
        const val EMAIL = "email"
        const val PROVIDER_ID = "providerId"
        const val UID = "uid"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            USER_NAME to name,
            EMAIL to email,
            PROVIDER_ID to providerId,
            UID to uid,
        )
    }
}
package my.dictionary.free.data.repositories

import android.util.Log
import com.google.firebase.database.*
import my.dictionary.free.BuildConfig
import my.dictionary.free.data.models.dictionary.DictionaryTable
import my.dictionary.free.data.models.users.UsersTable
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DatabaseRepository @Inject constructor(private val database: FirebaseDatabase) {

    init {
        if (BuildConfig.DEBUG) {
            try {
                database.setLogLevel(Logger.Level.DEBUG)
            } catch (ex: com.google.firebase.database.DatabaseException) {
                //skip
            }
        }
    }

    suspend fun getUserByUID(uid: String): UsersTable? {
        return suspendCoroutine { cont ->
            database.reference.child(UsersTable._NAME).orderByChild(UsersTable.UID).equalTo(uid)
                .limitToFirst(1).get()
                .addOnSuccessListener {
                    if (it.children.count() > 0) {
                        it.children.firstOrNull()?.let { value ->
                            val map = value.value as HashMap<*, *>
                            val result = UsersTable(
                                _id = map[UsersTable._ID] as String?,
                                name = map[UsersTable.USER_NAME] as String,
                                email = map[UsersTable.EMAIL] as String,
                                providerId = map[UsersTable.PROVIDER_ID] as String,
                                uid = map[UsersTable.UID] as String
                            )
                            cont.resume(result)
                        }
                    } else {
                        cont.resume(null)
                    }
                }.addOnFailureListener {
//                it.message ?: "Failed to get user by id $uid"
                    cont.resume(null)
                }.addOnCanceledListener {
                    cont.resume(null)
                }
        }
    }

    suspend fun insertUser(user: UsersTable, preferenceUtils: PreferenceUtils): Boolean {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val usersKey = reference.child(UsersTable._NAME).push().key
            if (usersKey == null) {
                cont.resume(false)
            }
            usersKey?.let { key ->
                val table = UsersTable(
                    _id = key,
                    name = user.name,
                    email = user.email,
                    providerId = user.providerId,
                    uid = user.uid
                )
                preferenceUtils.setString(PreferenceUtils.CURRENT_USER_ID, key)
                preferenceUtils.setString(PreferenceUtils.CURRENT_USER_UUID, user.uid)
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/$key" to table.toMap()
                )
                reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(true)
                }.addOnFailureListener {
//                    cancel(it.message ?: "Failed to update user ${user.name} ${user.email}")
                    cont.resume(false)
                }.addOnCanceledListener {
                    cont.resume(false)
                }
            }
        }
    }

    suspend fun updateUser(user: UsersTable, preferenceUtils: PreferenceUtils): Boolean {
        return suspendCoroutine { cont ->
            val reference = database.reference
            if (user._id == null) {
                cont.resume(false)
            }
            preferenceUtils.setString(PreferenceUtils.CURRENT_USER_ID, user._id)
            preferenceUtils.setString(PreferenceUtils.CURRENT_USER_UUID, user.uid)

            val userChild = reference.child(UsersTable._NAME).child("${user._id}")

            userChild.child(UsersTable._ID).setValue(user._id).result
            userChild.child(UsersTable.UID).setValue(user.uid).result
            userChild.child(UsersTable.EMAIL).setValue(user.email).result
            userChild.child(UsersTable.USER_NAME).setValue(user.name).result
            userChild.child(UsersTable.PROVIDER_ID).setValue(user.providerId).result
            cont.resume(true)
        }
    }

    suspend fun insertOrUpdateUser(user: UsersTable, preferenceUtils: PreferenceUtils): Boolean {
        val existUser = getUserByUID(user.uid)
        if (existUser != null && !user.equals(existUser)) return updateUser(existUser, preferenceUtils)
        if(existUser != null) return true
        return insertUser(user, preferenceUtils)
    }

    suspend fun createDictionary(userId: String, dictionary: DictionaryTable): Boolean {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val dictionaryKey = reference.child(DictionaryTable._NAME).push().key
            if (dictionaryKey == null) {
                cont.resume(false)
            }
            dictionaryKey?.let { key ->
                val table = DictionaryTable(
                    _id = key,
                    userUUID = dictionary.userUUID,
                    langFrom = dictionary.langFrom,
                    langTo = dictionary.langTo,
                    dialect = dictionary.dialect
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${DictionaryTable._NAME}/$key" to table.toMap()
                )
                reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(true)
                }.addOnFailureListener {
//                    cancel(it.message ?: "Failed to update user ${user.name} ${user.email}")
                    cont.resume(false)
                }.addOnCanceledListener {
                    cont.resume(false)
                }
            }
        }
    }

    suspend fun getDictionariesByUserUUID(
        userId: String,
    ): List<DictionaryTable> {
        return suspendCoroutine { cont ->
            database.reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                .get()
                .addOnSuccessListener {
                    val dictionaryList = arrayListOf<DictionaryTable>()
                    it.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val dictionary = DictionaryTable(
                            _id = map[DictionaryTable._ID] as String?,
                            userUUID = map[DictionaryTable.USER_UUID] as String,
                            langFrom = map[DictionaryTable.LANG_FROM] as String,
                            langTo = map[DictionaryTable.LANG_TO] as String,
                            dialect = map[DictionaryTable.DIALECT] as String
                        )
                        dictionaryList.add(dictionary)
                    }
                    cont.resume(dictionaryList)
                }.addOnFailureListener {
//                it.message ?: "Failed to get user's dictionary by id $uid"
                    cont.resume(emptyList())
                }.addOnCanceledListener {
                    cont.resume(emptyList())
                }
        }
    }
}
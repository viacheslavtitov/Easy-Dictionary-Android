package my.dictionary.free.data.repositories

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import my.dictionary.free.BuildConfig
import my.dictionary.free.data.models.dictionary.DictionaryTable
import my.dictionary.free.data.models.words.variants.TranslationCategoryTable
import my.dictionary.free.data.models.users.UsersTable
import my.dictionary.free.data.models.words.WordTable
import my.dictionary.free.data.models.words.variants.TranslationVariantTable
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DatabaseRepository @Inject constructor(private val database: FirebaseDatabase) {

    companion object {
        private val TAG = DatabaseRepository::class.simpleName
    }

    private val ioScope = Dispatchers.IO

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

            userChild.child(UsersTable._ID).setValue(user._id).isComplete
            userChild.child(UsersTable.UID).setValue(user.uid).isComplete
            userChild.child(UsersTable.EMAIL).setValue(user.email).isComplete
            userChild.child(UsersTable.USER_NAME).setValue(user.name).isComplete
            userChild.child(UsersTable.PROVIDER_ID).setValue(user.providerId).isComplete
            cont.resume(true)
        }
    }

    suspend fun insertOrUpdateUser(user: UsersTable, preferenceUtils: PreferenceUtils): Boolean {
        val existUser = getUserByUID(user.uid)
        if (existUser != null && !user.equals(existUser)) return updateUser(
            existUser,
            preferenceUtils
        )
        if (existUser != null) return true
        return insertUser(user, preferenceUtils)
    }

    suspend fun createDictionary(
        userId: String,
        dictionary: DictionaryTable
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val dictionaryKey = reference.child(DictionaryTable._NAME).push().key
            if (dictionaryKey == null) {
                cont.resume(Pair(false, null))
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
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
            }
        }
    }

    suspend fun createCategory(
        userId: String,
        category: TranslationCategoryTable
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val categoryKey = reference.child(TranslationCategoryTable._NAME).push().key
            if (categoryKey == null) {
                cont.resume(Pair(false, null))
            }
            categoryKey?.let { key ->
                val table = TranslationCategoryTable(
                    _id = key,
                    userUUID = category.userUUID,
                    categoryName = category.categoryName
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${TranslationCategoryTable._NAME}/$key" to table.toMap()
                )
                reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
            }
        }
    }

    suspend fun createTranslation(
        userId: String,
        dictionaryId: String,
        translation: TranslationVariantTable
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val translationKey = reference.child(TranslationVariantTable._NAME).push().key
            if (translationKey == null) {
                cont.resume(Pair(false, null))
            }
            translationKey?.let { key ->
                val table = TranslationVariantTable(
                    _id = key,
                    wordId = translation.wordId,
                    categoryId = translation.categoryId,
                    translate = translation.translate,
                    description = translation.description,
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${DictionaryTable._NAME}/${dictionaryId}/${WordTable._NAME}/${translation.wordId}/${TranslationVariantTable._NAME}/$key" to table.toMap()
                )
                reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
            }
        }
    }

    suspend fun getDictionariesByUserUUID(
        userId: String,
    ): Flow<DictionaryTable> {
        Log.d(TAG, "getDictionariesByUserUUID")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val dictionary = DictionaryTable(
                            _id = map[DictionaryTable._ID] as String?,
                            userUUID = map[DictionaryTable.USER_UUID] as String,
                            langFrom = map[DictionaryTable.LANG_FROM] as String,
                            langTo = map[DictionaryTable.LANG_TO] as String,
                            dialect = map[DictionaryTable.DIALECT] as String
                        )
                        trySend(dictionary)
                    }
                    close()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled")
                    cancel()
                }
            }
            reference.addValueEventListener(valueEventListener)
            awaitClose {
                Log.d(TAG, "awaitClose")
                reference.removeEventListener(valueEventListener)
            }
        }.flowOn(ioScope)
    }

    suspend fun getCategories(
        userId: String,
    ): Flow<TranslationCategoryTable> {
        Log.d(TAG, "getCategories")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(TranslationCategoryTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val category = TranslationCategoryTable(
                            _id = map[TranslationCategoryTable._ID] as String?,
                            userUUID = map[TranslationCategoryTable.USER_UUID] as String,
                            categoryName = map[TranslationCategoryTable.CATEGORY_NAME] as String
                        )
                        trySend(category)
                    }
                    close()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled")
                    cancel()
                }
            }
            reference.addValueEventListener(valueEventListener)
            awaitClose {
                Log.d(TAG, "awaitClose")
                reference.removeEventListener(valueEventListener)
            }
        }.flowOn(ioScope)
    }

    suspend fun getDictionaryById(
        userId: String,
        dictionaryId: String,
    ): Flow<DictionaryTable> {
        Log.d(
            TAG,
            "getDictionaryById ${UsersTable._NAME}/${userId}/${DictionaryTable._NAME}/${dictionaryId}"
        )
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.value?.toString()}")
                    try {
                        val map = snapshot.value as HashMap<*, *>
                        val dictionary = DictionaryTable(
                            _id = map[DictionaryTable._ID] as String?,
                            userUUID = map[DictionaryTable.USER_UUID] as String,
                            langFrom = map[DictionaryTable.LANG_FROM] as String,
                            langTo = map[DictionaryTable.LANG_TO] as String,
                            dialect = map[DictionaryTable.DIALECT] as String
                        )
                        trySend(dictionary)
                    } catch (ex: ClassCastException) {
                        Log.e(TAG, "Failed to cast data", ex)
                    } catch (ex: NullPointerException) {
                        Log.e(TAG, "Failed to get data", ex)
                    }
                    close()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled")
                    cancel()
                }
            }
            reference.addValueEventListener(valueEventListener)
            awaitClose {
                Log.d(TAG, "awaitClose")
                reference.removeEventListener(valueEventListener)
            }
        }.flowOn(ioScope)
    }

    suspend fun deleteDictionaries(
        userId: String,
        dictionaryIds: List<String>
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            dictionaryIds.forEach {
                Log.d(TAG, "delete dictionary by id = $it")
                childRemoves["/${DictionaryTable._NAME}/$it"] = null
            }
            database.reference.child(UsersTable._NAME).child(userId).updateChildren(childRemoves)
                .addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
        }
    }

    suspend fun deleteWord(
        userId: String,
        dictionaryId: String,
        wordId: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            childRemoves["/${WordTable._NAME}/$wordId"] = null
            database.reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME).child(dictionaryId).updateChildren(childRemoves)
                .addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
        }
    }

    suspend fun createWord(
        userId: String,
        word: Word
    ): Triple<Boolean, String?, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val wordKey = reference.child(WordTable._NAME).push().key
            if (wordKey == null) {
                cont.resume(Triple(false, null, null))
            }
            wordKey?.let { key ->
                val table = WordTable(
                    _id = key,
                    dictionaryId = word.dictionaryId,
                    original = word.original,
                    phonetic = word.phonetic
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${DictionaryTable._NAME}/${word.dictionaryId}/${WordTable._NAME}/$key" to table.toMap()
                )
                reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(Triple(true, null, wordKey))
                }.addOnFailureListener {
                    cont.resume(Triple(false, it.message, null))
                }.addOnCanceledListener {
                    cont.resume(Triple(false, null, null))
                }
            }
        }
    }

    suspend fun getTranslationVariantByWordId(
        userId: String,
        dictionaryId: String,
        wordId: String
    ): Flow<List<TranslationVariantTable>> {
        Log.d(TAG, "getTranslationVariantByWordId")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId).child(WordTable._NAME)
                .child(wordId).child(TranslationVariantTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    val translations = arrayListOf<TranslationVariantTable>()
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val translateVariant = TranslationVariantTable(
                            _id = map[TranslationVariantTable._ID] as String?,
                            translate = map[TranslationVariantTable.TRANSLATE] as String,
                            description = map[TranslationVariantTable.DESCRIPTION] as String?,
                            wordId = map[TranslationVariantTable.WORD_ID] as String,
                            categoryId = map[TranslationVariantTable.CATEGORY_ID] as String?,
                        )
                        translations.add(translateVariant)
                    }
                    trySend(translations)
                    close()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled")
                    cancel()
                }
            }
            reference.addValueEventListener(valueEventListener)
            awaitClose {
                Log.d(TAG, "awaitClose")
                reference.removeEventListener(valueEventListener)
            }
        }.flowOn(ioScope)
    }

    suspend fun getWordsByDictionaryId(
        userId: String,
        dictionaryId: String,
    ): Flow<WordTable> {
        Log.d(TAG, "getWordsByDictionaryId")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId).child(WordTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val word = WordTable(
                            _id = map[WordTable._ID] as String?,
                            dictionaryId = map[WordTable.DICTIONARY_ID] as String,
                            original = map[WordTable.ORIGINAL] as String,
                            phonetic = map[WordTable.PHONETIC] as String
                        )
                        trySend(word)
                    }
                    close()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled")
                    cancel()
                }
            }
            reference.addValueEventListener(valueEventListener)
            awaitClose {
                Log.d(TAG, "awaitClose")
                reference.removeEventListener(valueEventListener)
            }
        }.flowOn(ioScope)
    }
}
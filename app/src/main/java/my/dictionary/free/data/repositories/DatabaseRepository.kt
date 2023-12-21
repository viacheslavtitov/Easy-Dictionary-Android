package my.dictionary.free.data.repositories

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import my.dictionary.free.BuildConfig
import my.dictionary.free.data.models.dictionary.DictionaryTable
import my.dictionary.free.data.models.quiz.QuizResultTable
import my.dictionary.free.data.models.quiz.QuizTable
import my.dictionary.free.data.models.quiz.QuizWordResultTable
import my.dictionary.free.data.models.quiz.QuizWordsTable
import my.dictionary.free.data.models.users.UsersTable
import my.dictionary.free.data.models.words.WordTable
import my.dictionary.free.data.models.words.variants.TranslationCategoryTable
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
//                database.setLogLevel(Logger.Level.DEBUG)
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

    suspend fun updateDictionary(userId: String, dictionary: DictionaryTable): Boolean {
        return suspendCoroutine { cont ->
            val reference = database.reference

            val userChild =
                reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                    .child(dictionary._id!!)

            userChild.child(DictionaryTable._ID).setValue(dictionary._id).isComplete
            userChild.child(DictionaryTable.USER_UUID).setValue(dictionary.userUUID).isComplete
            userChild.child(DictionaryTable.LANG_FROM).setValue(dictionary.langFrom).isComplete
            userChild.child(DictionaryTable.LANG_TO).setValue(dictionary.langTo).isComplete
            userChild.child(DictionaryTable.DIALECT).setValue(dictionary.dialect).isComplete
            cont.resume(true)
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

    suspend fun updateTranslation(
        userId: String,
        dictionaryId: String,
        wordId: String,
        translation: TranslationVariantTable
    ): Boolean {
        return suspendCoroutine { cont ->
            val reference = database.reference

            val userChild =
                reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                    .child(dictionaryId)
                    .child(WordTable._NAME).child(wordId).child(TranslationVariantTable._NAME)
                    .child(translation._id!!)

            userChild.child(TranslationVariantTable.CATEGORY_ID)
                .setValue(translation.categoryId).isComplete
            userChild.child(TranslationVariantTable.TRANSLATE)
                .setValue(translation.translate).isComplete
            userChild.child(TranslationVariantTable.DESCRIPTION)
                .setValue(translation.description).isComplete
            cont.resume(true)
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

    suspend fun getQuizzesByUserUUID(
        userId: String,
    ): Flow<QuizTable> {
        Log.d(TAG, "getQuizzesByUserUUID")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(QuizTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val quiz = QuizTable(
                            _id = map[QuizTable._ID] as String?,
                            userId = map[QuizTable.USER_ID] as String,
                            dictionaryId = map[QuizTable.DICTIONARY_ID] as String,
                            name = map[QuizTable.NAME] as String,
                            timeInSeconds = (map[QuizTable.TIME_IN_SECONDS] as Long).toInt()
                        )
                        trySend(quiz)
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

    suspend fun getQuizById(
        userId: String,
        quizId: String,
    ): Flow<QuizTable> {
        Log.d(TAG, "getQuizById")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(QuizTable._NAME).child(quizId)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.value?.toString()}")
                    try {
                        val map = snapshot.value as HashMap<*, *>
                        val quiz = QuizTable(
                            _id = map[QuizTable._ID] as String?,
                            userId = map[QuizTable.USER_ID] as String,
                            dictionaryId = map[QuizTable.DICTIONARY_ID] as String,
                            name = map[QuizTable.NAME] as String,
                            timeInSeconds = (map[QuizTable.TIME_IN_SECONDS] as Long).toInt()
                        )
                        trySend(quiz)
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

    suspend fun createQuiz(
        userId: String,
        quiz: QuizTable
    ): Triple<Boolean, String?, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val quizKey = reference.child(QuizTable._NAME).push().key
            if (quizKey == null) {
                cont.resume(Triple(false, null, null))
            }
            quizKey?.let { key ->
                val table = QuizTable(
                    _id = key,
                    userId = userId,
                    dictionaryId = quiz.dictionaryId,
                    name = quiz.name,
                    timeInSeconds = quiz.timeInSeconds
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${QuizTable._NAME}/$key" to table.toMap()
                )
                reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(Triple(true, null, key))
                }.addOnFailureListener {
                    cont.resume(Triple(false, it.message, null))
                }.addOnCanceledListener {
                    cont.resume(Triple(false, null, null))
                }
            }
        }
    }

    suspend fun updateQuiz(
        userId: String,
        quiz: QuizTable
    ): Boolean {
        return suspendCoroutine { cont ->
            val reference = database.reference

            val userChild = reference.child(UsersTable._NAME).child(userId).child(QuizTable._NAME)
                .child(quiz._id!!)

            userChild.child(QuizTable._ID).setValue(quiz._id).isComplete
            userChild.child(QuizTable.USER_ID).setValue(quiz.userId).isComplete
            userChild.child(QuizTable.DICTIONARY_ID).setValue(quiz.dictionaryId).isComplete
            userChild.child(QuizTable.NAME).setValue(quiz.name).isComplete
            userChild.child(QuizTable.TIME_IN_SECONDS).setValue(quiz.timeInSeconds).isComplete
            cont.resume(true)
        }
    }

    suspend fun addWordToQuiz(
        userId: String,
        quizId: String, wordId: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val wordKey = reference.child(WordTable._NAME).push().key
            if (wordKey == null) {
                cont.resume(Pair(false, null))
            }
            wordKey?.let { key ->
                val table = QuizWordsTable(
                    _id = key,
                    quizId = quizId,
                    wordId = wordId
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${QuizTable._NAME}/${table.quizId}/${QuizWordsTable._NAME}/$key" to table.toMap()
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

    suspend fun deleteWordFromQuiz(
        userId: String,
        quizId: String, wordIds: List<String>
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            wordIds.forEach {
                Log.d(TAG, "delete word by id = $it from quiz id $quizId")
                childRemoves["/${QuizWordsTable._NAME}/$it"] = null
            }
            database.reference.child(UsersTable._NAME).child(userId).child(QuizTable._NAME)
                .child(quizId).updateChildren(childRemoves)
                .addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
        }
    }

    suspend fun getQuizWords(
        userId: String,
        quizId: String,
    ): Flow<List<QuizWordsTable>> {
        Log.d(TAG, "getQuizzesByUserUUID")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(QuizTable._NAME).child(quizId).child(QuizWordsTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    val quizWords = mutableListOf<QuizWordsTable>()
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val quizWord = QuizWordsTable(
                            _id = map[QuizWordsTable._ID] as String?,
                            quizId = map[QuizWordsTable.QUIZ_ID] as String,
                            wordId = map[QuizWordsTable.WORD_ID] as String
                        )
                        quizWords.add(quizWord)
                    }
                    trySend(quizWords)
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

    suspend fun getCategoryById(
        userId: String,
        categoryId: String,
    ): Flow<TranslationCategoryTable> {
        Log.d(TAG, "getCategories")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(TranslationCategoryTable._NAME).child(categoryId)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    val map = snapshot.value as HashMap<*, *>
                    val category = TranslationCategoryTable(
                        _id = map[TranslationCategoryTable._ID] as String?,
                        userUUID = map[TranslationCategoryTable.USER_UUID] as String,
                        categoryName = map[TranslationCategoryTable.CATEGORY_NAME] as String
                    )
                    trySend(category)
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

    suspend fun deleteQuizzes(
        userId: String,
        quizIds: List<String>
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            quizIds.forEach {
                Log.d(TAG, "delete quiz by id = $it")
                childRemoves["/${QuizTable._NAME}/$it"] = null
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

    suspend fun deleteWords(
        userId: String,
        dictionaryId: String,
        wordsIds: List<String>
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            wordsIds.forEach {
                Log.d(TAG, "delete word by id = $it")
                childRemoves["/${WordTable._NAME}/$it"] = null
            }
            database.reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                .child(dictionaryId).updateChildren(childRemoves)
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
            database.reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                .child(dictionaryId).updateChildren(childRemoves)
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

    suspend fun updateWord(
        userId: String,
        word: WordTable
    ): Boolean {
        return suspendCoroutine { cont ->
            val reference = database.reference

            val userChild =
                reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                    .child(word.dictionaryId)
                    .child(WordTable._NAME)
                    .child(word._id!!)

            userChild.child(WordTable._ID).setValue(word._id).isComplete
            userChild.child(WordTable.DICTIONARY_ID).setValue(word.dictionaryId).isComplete
            userChild.child(WordTable.ORIGINAL).setValue(word.original).isComplete
            userChild.child(WordTable.PHONETIC).setValue(word.phonetic).isComplete
            cont.resume(true)
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

    suspend fun deleteTranslationsFromWord(
        userId: String,
        dictionaryId: String,
        wordId: String, translationIds: List<String>
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            translationIds.forEach {
                Log.d(TAG, "delete translation by id = $it from word id $wordId")
                childRemoves["/${TranslationVariantTable._NAME}/$it"] = null
            }
            database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId).child(WordTable._NAME)
                .child(wordId).updateChildren(childRemoves)
                .addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
        }
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

    suspend fun getWordById(
        userId: String,
        dictionaryId: String,
        wordId: String,
    ): Flow<WordTable> {
        Log.d(TAG, "getWordById")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId).child(WordTable._NAME)
                .child(wordId)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    try {
                        val map = snapshot.value as HashMap<*, *>
                        val word = WordTable(
                            _id = map[WordTable._ID] as String?,
                            dictionaryId = map[WordTable.DICTIONARY_ID] as String,
                            original = map[WordTable.ORIGINAL] as String,
                            phonetic = map[WordTable.PHONETIC] as String
                        )
                        trySend(word)
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

    suspend fun saveQuizResults(
        userId: String,
        quizTable: QuizResultTable
    ): Triple<Boolean, String?, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val quizResultKey = reference.child(QuizResultTable._NAME).push().key
            if (quizResultKey == null) {
                cont.resume(Triple(false, null, null))
            }
            quizResultKey?.let { key ->
                val table = QuizResultTable(
                    _id = key,
                    quizId = quizTable.quizId,
                    wordsCount = quizTable.wordsCount,
                    rightAnswers = quizTable.rightAnswers,
                    unixDateTimeStamp = quizTable.unixDateTimeStamp
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${QuizTable._NAME}/${table.quizId}/${QuizResultTable._NAME}/$key" to table.toMap()
                )
                reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(Triple(true, null, key))
                }.addOnFailureListener {
                    cont.resume(Triple(false, it.message, null))
                }.addOnCanceledListener {
                    cont.resume(Triple(false, null, null))
                }
            }
        }
    }

    suspend fun addWordToQuizResult(
        userId: String,
        quizResultId: String,
        quizWordResult: QuizWordResultTable
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val wordKey = reference.child(QuizWordResultTable._NAME).push().key
            if (wordKey == null) {
                cont.resume(Pair(false, null))
            }
            wordKey?.let { key ->
                val table = QuizWordResultTable(
                    _id = key,
                    quizId = quizWordResult.quizId,
                    wordId = quizWordResult.wordId,
                    originalWord = quizWordResult.originalWord,
                    answer = quizWordResult.answer
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${QuizTable._NAME}/${table.quizId}/${QuizResultTable._NAME}/${quizResultId}/${QuizWordResultTable._NAME}/$key" to table.toMap()
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

    suspend fun deleteQuizResult(
        userId: String,
        quizId: String,
        quizResultId: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            childRemoves["/${QuizResultTable._NAME}/$quizResultId"] = null
            database.reference.child(UsersTable._NAME).child(userId).child(QuizTable._NAME)
                .child(quizId).updateChildren(childRemoves)
                .addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
        }
    }

    suspend fun getHistoriesOfQuiz(
        userId: String,
        quizId: String,
    ): Flow<List<QuizResultTable>> {
        Log.d(TAG, "getHistoryOfQuiz")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(QuizTable._NAME).child(quizId)
                .child(QuizResultTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    val result = arrayListOf<QuizResultTable>()
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val quiz = QuizResultTable(
                            _id = map[QuizResultTable._ID] as String?,
                            quizId = map[QuizResultTable.QUIZ_ID] as String,
                            wordsCount = (map[QuizResultTable.WORDS_COUNT] as Long).toInt(),
                            rightAnswers = (map[QuizResultTable.RIGHT_ANSWERS] as Long).toInt(),
                            unixDateTimeStamp = map[QuizResultTable.UNIX_DATE_TIME_STAMP] as Long
                        )
                        result.add(quiz)
                    }
                    trySend(result)
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
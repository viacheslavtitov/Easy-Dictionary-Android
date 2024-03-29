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
import my.dictionary.free.data.models.dictionary.VerbTenseTable
import my.dictionary.free.data.models.quiz.QuizResultTable
import my.dictionary.free.data.models.quiz.QuizTable
import my.dictionary.free.data.models.quiz.QuizWordResultTable
import my.dictionary.free.data.models.quiz.QuizWordsTable
import my.dictionary.free.data.models.users.UsersTable
import my.dictionary.free.data.models.words.WordTable
import my.dictionary.free.data.models.words.WordTagTable
import my.dictionary.free.data.models.words.variants.TranslationCategoryTable
import my.dictionary.free.data.models.words.variants.TranslationVariantTable
import my.dictionary.free.data.models.words.verb_tense.WordVerbTenseTable
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

    private suspend fun updateUser(user: UsersTable, preferenceUtils: PreferenceUtils): Boolean {
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

    suspend fun addVerbTenseToDictionary(
        userId: String,
        dictionaryId: String,
        tenseName: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val verbKey =
                database.reference.child(UsersTable._NAME).child(userId)
                    .child(DictionaryTable._NAME).child(dictionaryId).child(VerbTenseTable.NAME)
                    .push().key
            if (verbKey == null) {
                cont.resume(Pair(false, null))
            }
            verbKey?.let { key ->
                val table = VerbTenseTable(
                    _id = key,
                    name = tenseName
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${DictionaryTable._NAME}/${dictionaryId}/${VerbTenseTable._NAME}/$key" to table.toMap()
                )
                database.reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
            }
        }
    }

    suspend fun deleteVerbTenseFromDictionary(
        userId: String,
        dictionaryId: String, verbTenseIds: List<String>
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            verbTenseIds.forEach {
                if (it.isNotEmpty()) {
                    Log.d(TAG, "delete verb tense by id = $it from dictionary id $dictionaryId")
                    childRemoves["/${VerbTenseTable._NAME}/$it"] = null
                }
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

    suspend fun createDictionary(
        userId: String,
        dictionary: DictionaryTable
    ): Pair<String?, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val dictionaryKey =
                reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                    .push().key
            if (dictionaryKey == null) {
                cont.resume(Pair(null, null))
            }
            dictionaryKey?.let { key ->
                val table = DictionaryTable(
                    _id = key,
                    userUUID = dictionary.userUUID,
                    langFrom = dictionary.langFrom,
                    langTo = dictionary.langTo,
                    dialect = dictionary.dialect,
                    tenses = dictionary.tenses
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${DictionaryTable._NAME}/$key" to table.toMap()
                )
                reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(Pair(key, null))
                }.addOnFailureListener {
                    cont.resume(Pair(null, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(null, null))
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
            val categoryKey = reference.child(UsersTable._NAME).child(userId)
                .child(TranslationCategoryTable._NAME).push().key
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
            val translationKey =
                reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                    .child(dictionaryId).child(WordTable._NAME).child(translation.wordId)
                    .child(TranslationVariantTable._NAME).push().key
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
        if (translation._id.isNullOrEmpty()) {
            return false
        } else return suspendCoroutine { cont ->
            database.reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                .child(dictionaryId)
                .child(WordTable._NAME).child(wordId).child(TranslationVariantTable._NAME)
                .child(translation._id)
                .updateChildren(
                    mapOf(
                        TranslationVariantTable.CATEGORY_ID to translation.categoryId,
                        TranslationVariantTable.TRANSLATE to translation.translate,
                        TranslationVariantTable.DESCRIPTION to translation.description
                    )
                ) { error, reference ->
                    if (error != null && error.message.isNotEmpty()) {
                        cont.resume(false)
                    } else {
                        cont.resume(true)
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
                            dialect = map[DictionaryTable.DIALECT] as String,
                            tenses = mutableListOf()
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

    suspend fun getVerbTensesByDictionaryId(
        userId: String,
        dictionaryId: String,
    ): Flow<List<VerbTenseTable>> {
        Log.d(TAG, "getVerbTensesByDictionaryId $dictionaryId")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId).child(VerbTenseTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    val result = arrayListOf<VerbTenseTable>()
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val verbTable = VerbTenseTable(
                            _id = map[VerbTenseTable._ID] as String?,
                            name = map[VerbTenseTable.NAME] as String,
                        )
                        result.add(verbTable)
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
                            reversed = map[QuizTable.REVERSED] as? Boolean ?: false,
                            hidePhonetic = map[QuizTable.HIDE_PHONETIC] as? Boolean ?: false,
                            showTags = map[QuizTable.SHOW_TAGS] as? Boolean ?: false,
                            showCategories = map[QuizTable.SHOW_CATEGORIES] as? Boolean ?: false,
                            showTypes = map[QuizTable.SHOW_TYPES] as? Boolean ?: false,
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
                            reversed = map[QuizTable.REVERSED] as? Boolean ?: false,
                            hidePhonetic = map[QuizTable.HIDE_PHONETIC] as? Boolean ?: false,
                            showTags = map[QuizTable.SHOW_TAGS] as? Boolean ?: false,
                            showCategories = map[QuizTable.SHOW_CATEGORIES] as? Boolean ?: false,
                            showTypes = map[QuizTable.SHOW_TYPES] as? Boolean ?: false,
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
            val quizKey =
                reference.child(UsersTable._NAME).child(userId).child(QuizTable._NAME).push().key
            if (quizKey == null) {
                cont.resume(Triple(false, null, null))
            }
            quizKey?.let { key ->
                val table = QuizTable(
                    _id = key,
                    userId = userId,
                    dictionaryId = quiz.dictionaryId,
                    name = quiz.name,
                    reversed = quiz.reversed,
                    hidePhonetic = quiz.hidePhonetic,
                    showTags = quiz.showTags,
                    showCategories = quiz.showCategories,
                    showTypes = quiz.showTypes,
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
            userChild.child(QuizTable.REVERSED).setValue(quiz.reversed).isComplete
            userChild.child(QuizTable.HIDE_PHONETIC).setValue(quiz.hidePhonetic).isComplete
            userChild.child(QuizTable.SHOW_TAGS).setValue(quiz.showTags).isComplete
            userChild.child(QuizTable.SHOW_CATEGORIES).setValue(quiz.showCategories).isComplete
            userChild.child(QuizTable.SHOW_TYPES).setValue(quiz.showTypes).isComplete
            cont.resume(true)
        }
    }

    suspend fun addWordToQuiz(
        userId: String,
        quizId: String, wordId: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val wordKey =
                reference.child(UsersTable._NAME).child(userId).child(QuizTable._NAME).child(quizId)
                    .child(WordTable._NAME).push().key
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
                            dialect = map[DictionaryTable.DIALECT] as String,
                            tenses = mutableListOf()
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
            val wordKey =
                reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                    .child(word.dictionaryId).child(WordTable._NAME).push().key
            if (wordKey == null) {
                cont.resume(Triple(false, null, null))
            }
            wordKey?.let { key ->
                val table = WordTable(
                    _id = key,
                    dictionaryId = word.dictionaryId,
                    original = word.original,
                    type = word.type,
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
            userChild.child(WordTable.TYPE).setValue(word.type).isComplete
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
        Log.d(TAG, "getTranslationVariantByWordId $wordId")
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
                        val wordTags = arrayListOf<String>()
                        val translations = arrayListOf<TranslationVariantTable>()
                        val tenses = arrayListOf<WordVerbTenseTable>()
                        map[WordVerbTenseTable._NAME]?.let {
                            (it as? ArrayList<String>)?.let { tags ->
                                wordTags.addAll(tags)
                            }
                        }
                        (map[TranslationVariantTable._NAME] as? HashMap<*, *>)?.let { translation ->
                            translation.keys.forEach { key ->
                                val obj = translation[key] as Map<*, *>
                                val translateVariant = TranslationVariantTable(
                                    _id = obj[TranslationVariantTable._ID] as String?,
                                    translate = obj[TranslationVariantTable.TRANSLATE] as String,
                                    description = obj[TranslationVariantTable.DESCRIPTION] as String?,
                                    wordId = obj[TranslationVariantTable.WORD_ID] as String,
                                    categoryId = obj[TranslationVariantTable.CATEGORY_ID] as String?,
                                )
                                translations.add(translateVariant)
                            }
                        }
                        (map[WordVerbTenseTable._NAME] as? HashMap<*, *>)?.let { verbTenses ->
                            verbTenses.keys.forEach { key ->
                                val obj = verbTenses[key] as Map<*, *>
                                val tense = WordVerbTenseTable(
                                    _id = obj[WordVerbTenseTable._ID] as String,
                                    tenseId = obj[WordVerbTenseTable.TENSE_ID] as String,
                                    wordId = obj[WordVerbTenseTable.WORD_ID] as String,
                                    value = obj[WordVerbTenseTable.VALUE] as String,
                                )
                                tenses.add(tense)
                            }
                        }
                        val word = WordTable(
                            _id = map[WordTable._ID] as String?,
                            dictionaryId = map[WordTable.DICTIONARY_ID] as String,
                            original = map[WordTable.ORIGINAL] as String,
                            type = (map[WordTable.TYPE] as? Long)?.toInt() ?: 0,
                            phonetic = map[WordTable.PHONETIC] as String,
                            wordTagsIds = wordTags,
                            translations = translations,
                            verbTenses = tenses
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

    suspend fun getWordsByDictionaryTestId(
        userId: String,
        dictionaryId: String,
    ): Flow<List<WordTable>> {
        Log.d(TAG, "getWordsByDictionaryId")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId).child(WordTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
                    val result = arrayListOf<WordTable>()
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val wordTags = arrayListOf<String>()
                        val translations = arrayListOf<TranslationVariantTable>()
                        val tenses = arrayListOf<WordVerbTenseTable>()
                        map[WordVerbTenseTable._NAME]?.let {
                            (it as? ArrayList<String>)?.let { tags ->
                                wordTags.addAll(tags)
                            }
                        }
                        (map[TranslationVariantTable._NAME] as? HashMap<*, *>)?.let { translation ->
                            translation.keys.forEach { key ->
                                val obj = translation[key] as Map<*, *>
                                val translateVariant = TranslationVariantTable(
                                    _id = obj[TranslationVariantTable._ID] as String?,
                                    translate = obj[TranslationVariantTable.TRANSLATE] as String,
                                    description = obj[TranslationVariantTable.DESCRIPTION] as String?,
                                    wordId = obj[TranslationVariantTable.WORD_ID] as String,
                                    categoryId = obj[TranslationVariantTable.CATEGORY_ID] as String?,
                                )
                                translations.add(translateVariant)
                            }
                        }
                        (map[WordVerbTenseTable._NAME] as? HashMap<*, *>)?.let { verbTenses ->
                            verbTenses.keys.forEach { key ->
                                val obj = verbTenses[key] as Map<*, *>
                                val tense = WordVerbTenseTable(
                                    _id = obj[WordVerbTenseTable._ID] as String,
                                    tenseId = obj[WordVerbTenseTable.TENSE_ID] as String,
                                    wordId = obj[WordVerbTenseTable.WORD_ID] as String,
                                    value = obj[WordVerbTenseTable.VALUE] as String,
                                )
                                tenses.add(tense)
                            }
                        }
                        result.add(
                            WordTable(
                                _id = map[WordTable._ID] as String?,
                                dictionaryId = map[WordTable.DICTIONARY_ID] as String,
                                original = map[WordTable.ORIGINAL] as String,
                                type = (map[WordTable.TYPE] as? Long)?.toInt() ?: 0,
                                phonetic = map[WordTable.PHONETIC] as String,
                                wordTagsIds = wordTags,
                                translations = translations,
                                verbTenses = tenses
                            )
                        )
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
                        val wordTags = arrayListOf<String>()
                        val translations = arrayListOf<TranslationVariantTable>()
                        val tenses = arrayListOf<WordVerbTenseTable>()
                        map[WordVerbTenseTable._NAME]?.let {
                            (it as? ArrayList<String>)?.let { tags ->
                                wordTags.addAll(tags)
                            }
                        }
                        (map[TranslationVariantTable._NAME] as? HashMap<*, *>)?.let { translation ->
                            translation.keys.forEach { key ->
                                val obj = translation[key] as Map<*, *>
                                val translateVariant = TranslationVariantTable(
                                    _id = obj[TranslationVariantTable._ID] as String?,
                                    translate = obj[TranslationVariantTable.TRANSLATE] as String,
                                    description = obj[TranslationVariantTable.DESCRIPTION] as String?,
                                    wordId = obj[TranslationVariantTable.WORD_ID] as String,
                                    categoryId = obj[TranslationVariantTable.CATEGORY_ID] as String?,
                                )
                                translations.add(translateVariant)
                            }
                        }
                        (map[WordVerbTenseTable._NAME] as? HashMap<*, *>)?.let { verbTenses ->
                            verbTenses.keys.forEach { key ->
                                val obj = verbTenses[key] as Map<*, *>
                                val tense = WordVerbTenseTable(
                                    _id = obj[WordVerbTenseTable._ID] as String,
                                    tenseId = obj[WordVerbTenseTable.TENSE_ID] as String,
                                    wordId = obj[WordVerbTenseTable.WORD_ID] as String,
                                    value = obj[WordVerbTenseTable.VALUE] as String,
                                )
                                tenses.add(tense)
                            }
                        }
                        val word = WordTable(
                            _id = map[WordTable._ID] as String?,
                            dictionaryId = map[WordTable.DICTIONARY_ID] as String,
                            original = map[WordTable.ORIGINAL] as String,
                            type = (map[WordTable.TYPE] as? Long)?.toInt() ?: 0,
                            phonetic = map[WordTable.PHONETIC] as String,
                            wordTagsIds = wordTags,
                            translations = translations,
                            verbTenses = tenses
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
            val wordKey = reference.child(UsersTable._NAME).child(userId).child(QuizTable._NAME)
                .child(quizWordResult.quizId).child(QuizWordResultTable._NAME).push().key
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

    suspend fun createDictionaryTag(
        userId: String,
        dictionaryId: String,
        tagName: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val reference = database.reference
            val tagKey = reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId).child(WordTagTable._NAME)
                .push().key
            if (tagKey == null) {
                cont.resume(Pair(false, null))
            }
            tagKey?.let { key ->
                val table = WordTagTable(
                    _id = key,
                    userUUID = userId,
                    tagName = tagName
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${DictionaryTable._NAME}/${dictionaryId}/${WordTagTable._NAME}/$key" to table.toMap()
                )
                reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(Pair(true, key))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
            }
        }
    }

    suspend fun addTagsToWord(
        userId: String,
        tagIds: List<String>,
        dictionaryId: String,
        wordId: String
    ): Boolean {
        return suspendCoroutine { cont ->
            val wordChild =
                database.reference.child(UsersTable._NAME).child(userId)
                    .child(DictionaryTable._NAME)
                    .child(dictionaryId).child(WordTable._NAME).child(wordId)
            val childAdds = mutableMapOf<String, Any?>()
            tagIds.forEach {
                Log.d(TAG, "add tag $it")
                childAdds["/$it"] = null
            }
            wordChild.child(WordTagTable._NAME).setValue(tagIds).isComplete
            cont.resume(true)
        }
    }

    suspend fun getTagsForDictionary(
        userId: String,
        dictionaryId: String,
    ): Flow<List<WordTagTable>> {
        Log.d(TAG, "getTagsForDictionary($dictionaryId)")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId)
                .child(WordTagTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange tags in dictionary ${snapshot.children.count()}")
                    val tags = arrayListOf<WordTagTable>()
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val tag = WordTagTable(
                            _id = map[WordTagTable._ID] as String?,
                            userUUID = map[WordTagTable.USER_UUID] as String,
                            tagName = map[WordTagTable.TAG_NAME] as String
                        )
                        tags.add(tag)
                    }
                    Log.d(TAG, "trySend tags = ${tags.size}")
                    trySend(tags)
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

//    suspend fun getTagsIdsForWord(
//        userId: String,
//        dictionaryId: String,
//        wordId: String,
//    ): Flow<List<String>> {
//        Log.d(TAG, "getTagsIdsForWord $wordId")
//        return callbackFlow {
//            val reference = database.reference.child(UsersTable._NAME).child(userId)
//                .child(DictionaryTable._NAME).child(dictionaryId)
//                .child(WordTable._NAME).child(wordId)
//                .child(WordTagTable._NAME)
//            val valueEventListener = object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    Log.d(TAG, "onDataChange ${snapshot.children.count()}")
//                    val tags = arrayListOf<String>()
//                    snapshot.children.forEach { data ->
//                        val tagId = data.value as String? ?: ""
//                        tags.add(tagId)
//                    }
//                    trySend(tags)
//                    close()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e(TAG, "onCancelled ${error.message}")
//                    cancel()
//                }
//            }
//            reference.addValueEventListener(valueEventListener)
//            awaitClose {
//                Log.d(TAG, "awaitClose")
//                reference.removeEventListener(valueEventListener)
//            }
//        }.flowOn(ioScope)
//    }

    suspend fun deleteWordTagFromWord(
        userId: String,
        dictionaryId: String,
        wordId: String,
        tagId: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            childRemoves["/${WordTagTable._NAME}/$tagId"] = null
            database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId)
                .child(WordTable._NAME).child(wordId).updateChildren(childRemoves)
                .addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
        }
    }

    suspend fun deleteWordTagFromDictionary(
        userId: String,
        dictionaryId: String,
        tagId: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            childRemoves["/${WordTagTable._NAME}/$tagId"] = null
            database.reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                .child(dictionaryId)
                .updateChildren(childRemoves)
                .addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
        }
    }

    suspend fun addVerbTenseToWord(
        userId: String,
        dictionaryId: String,
        wordId: String,
        tenseId: String,
        tenseValue: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val verbKey =
                database.reference.child(UsersTable._NAME).child(userId)
                    .child(DictionaryTable._NAME).child(dictionaryId).child(WordTable._NAME)
                    .child(wordId)
                    .child(WordVerbTenseTable._NAME)
                    .push().key
            if (verbKey == null) {
                cont.resume(Pair(false, null))
            }
            verbKey?.let { key ->
                val table = WordVerbTenseTable(
                    _id = key,
                    tenseId = tenseId,
                    wordId = wordId,
                    value = tenseValue
                )
                val childUpdates = hashMapOf<String, Any>(
                    "/${UsersTable._NAME}/${userId}/${DictionaryTable._NAME}/${dictionaryId}/${WordTable._NAME}/${wordId}/${WordVerbTenseTable._NAME}//$key" to table.toMap()
                )
                database.reference.updateChildren(childUpdates).addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
            }
        }
    }

    suspend fun getVerbTenseForWord(
        userId: String,
        dictionaryId: String,
        wordId: String,
    ): Flow<List<WordVerbTenseTable>> {
        Log.d(TAG, "getVerbTenseForWord $wordId")
        return callbackFlow {
            val reference = database.reference.child(UsersTable._NAME).child(userId)
                .child(DictionaryTable._NAME).child(dictionaryId)
                .child(WordTable._NAME).child(wordId)
                .child(WordVerbTenseTable._NAME)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "$wordId onDataChange ${snapshot.children.count()}")
                    val tenses = arrayListOf<WordVerbTenseTable>()
                    snapshot.children.forEach { data ->
                        val map = data.value as HashMap<*, *>
                        val tense = WordVerbTenseTable(
                            _id = map[WordVerbTenseTable._ID] as String,
                            tenseId = map[WordVerbTenseTable.TENSE_ID] as String,
                            wordId = map[WordVerbTenseTable.WORD_ID] as String,
                            value = map[WordVerbTenseTable.VALUE] as String,
                        )
                        tenses.add(tense)
                    }
                    Log.d(TAG, "trySend tenses = ${tenses.size}")
                    trySend(tenses)
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

    suspend fun deleteVerbTenseFromWord(
        userId: String,
        dictionaryId: String,
        wordId: String,
        wordTenseId: String
    ): Pair<Boolean, String?> {
        return suspendCoroutine { cont ->
            val childRemoves = mutableMapOf<String, Any?>()
            childRemoves["/${VerbTenseTable._NAME}/$wordTenseId"] = null
            database.reference.child(UsersTable._NAME).child(userId).child(DictionaryTable._NAME)
                .child(dictionaryId).child(WordTable._NAME).child(wordId)
                .updateChildren(childRemoves)
                .addOnSuccessListener {
                    cont.resume(Pair(true, null))
                }.addOnFailureListener {
                    cont.resume(Pair(false, it.message))
                }.addOnCanceledListener {
                    cont.resume(Pair(false, null))
                }
        }
    }

}
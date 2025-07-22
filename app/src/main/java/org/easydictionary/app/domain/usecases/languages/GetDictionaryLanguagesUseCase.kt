package org.easydictionary.app.domain.usecases.languages

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.language.LanguageType
import java.io.IOException
import java.util.*
import javax.inject.Inject

class GetDictionaryLanguagesUseCase @Inject constructor() {

    suspend fun getLanguages(context: Context): List<Language> {
        val languageType =
            LanguageType.values().firstOrNull { it.name == Locale.getDefault().language }
                ?: LanguageType.EN
        var languageFileName = "languages/" + when (languageType) {
            LanguageType.EN -> "languages_en.json"
            LanguageType.UKR -> "languages_ukr.json"
            LanguageType.RU -> "languages_ru.json"
            LanguageType.DE -> "languages_de.json"
            LanguageType.FR -> "languages_fr.json"
            LanguageType.ES -> "languages_es.json"
        }
        try {
            val jsonString = context.assets.open(languageFileName)
                .bufferedReader()
                .use { it.readText() }
            val listCountryType = object : TypeToken<List<Language>>() {}.type
            val languages: List<Language> = Gson().fromJson(jsonString, listCountryType)
//            return languages.sortedBy { it.value }
            return languages
        } catch (ex: IOException) {
            //skip
        }
        return emptyList()
    }

    suspend fun getLanguages(context: Context, query: String): List<Language> {
//        return getLanguages(context).filter { it.value.contains(query, true) }
        return getLanguages(context)
    }

    suspend fun findLanguageByKey(context: Context, langKey: String): Language? {
//        return getLanguages(context).find { it.key == langKey }
        return getLanguages(context).find { it.id == 1 }
    }

}
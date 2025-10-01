package org.easydictionary.app.data.repositories.language

import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easydictionary.app.R
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.language.PhoneticsStaticApiService
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.Phonetic
import org.easydictionary.app.domain.repository.language.PhoneticsRepository
import javax.inject.Inject

class PhoneticsRepositoryImpl @Inject constructor(
    private val resources: Resources,
    private val phoneticsStaticApiService: PhoneticsStaticApiService
) : PhoneticsRepository {

    companion object {
        private const val PHONETICS_TYPE = "ipa"
    }

    override suspend fun getPhonetics(type: String): Flow<DomainResult<List<Phonetic>>> {
        return flowOf(
            when (val result = wrapApi {
                phoneticsStaticApiService.getAll(PHONETICS_TYPE)
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.toListDomain())
                }

                is ApiResult.ApiError -> {
                    DomainResult.Error("${resources.getString(R.string.error)}: ${result.message}")
                }

                is ApiResult.NetworkError -> {
                    DomainResult.Error(resources.getString(R.string.network_error))
                }

                is ApiResult.UnknownError -> {
                    DomainResult.Error(resources.getString(R.string.unknown_error))
                }
            }
        )
    }
}
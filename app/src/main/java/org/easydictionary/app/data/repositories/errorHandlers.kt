package org.easydictionary.app.data.repositories

import android.content.res.Resources
import org.easydictionary.app.R
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.domain.models.DomainResult

fun handleApiErrors(resources: Resources, apiResult: ApiResult<Any>): DomainResult.Error {
    return when(apiResult) {
        is ApiResult.NetworkError -> {
            DomainResult.Error(resources.getString(R.string.network_error))
        }

        is ApiResult.ServiceUnavailable -> {
            DomainResult.Error(resources.getString(R.string.error_service_unavailable))
        }

        else -> {
            DomainResult.Error(resources.getString(R.string.unknown_error))
        }
    }
}
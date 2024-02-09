package my.dictionary.free.view

sealed class FetchDataState<out T> {
    object StartLoadingState : FetchDataState<Nothing>()
    object FinishLoadingState : FetchDataState<Nothing>()
    data class ErrorState(val exception: Throwable) : FetchDataState<Nothing>()
    data class ErrorStateString(val error: String) : FetchDataState<Nothing>()
    data class DataState<T>(val data: T) : FetchDataState<T>()
}
package org.easydictionary.app.domain.viewmodels.user.dictionary.words.add

sealed interface AddDictionaryWordEffect {
    data object WordCreated : AddDictionaryWordEffect
    data object WordUpdated : AddDictionaryWordEffect
    data object WordDeleted : AddDictionaryWordEffect
    data object TagCreated : AddDictionaryWordEffect
    data class ShowError(val message: String) : AddDictionaryWordEffect
}
package org.easydictionary.app.view.user.dictionary.words

import org.easydictionary.app.domain.models.words.Word

interface OnWordClickListener {
    fun onClick(word: Word)
    fun onLongClick(word: Word)
}
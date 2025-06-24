package org.easy.dictionary.app.view.user.dictionary.words

import org.easy.dictionary.app.domain.models.words.Word

interface OnWordClickListener {
    fun onClick(word: Word)
    fun onLongClick(word: Word)
}
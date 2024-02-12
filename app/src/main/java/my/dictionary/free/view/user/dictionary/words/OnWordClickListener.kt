package my.dictionary.free.view.user.dictionary.words

import my.dictionary.free.domain.models.words.Word

interface OnWordClickListener {
    fun onClick(word: Word)
    fun onLongClick(word: Word)
}
package org.easydictionary.app.view.screens.dictionary.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.view.dividers.Divider
import org.easydictionary.app.view.texts.Secondary2TextFieldLabel
import org.easydictionary.app.view.texts.TextFieldLabel
import org.easydictionary.app.view.widget.global.getCurrentColorScheme

@Composable
fun WordListItem(
    word: WordDetail,
    onSelect: (WordDetail) -> Unit
) {
    val backgroundColor = getCurrentColorScheme().secondaryContainer
    var translations = ""
    word.translations.forEachIndexed { index, item ->
        translations += if (index == word.translations.size - 1) {
            " ${item.translate}"
        } else {
            " ${item.translate},"
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable {
                onSelect(word)
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextFieldLabel(word.original, Modifier.wrapContentSize())
            if (word.phonetic?.isNotEmpty() == true) {
                Secondary2TextFieldLabel(
                    label = " - [${word.phonetic}]",
                    modifier = Modifier.wrapContentWidth()
                )
                Secondary2TextFieldLabel(
                    label = " $translations",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                Secondary2TextFieldLabel(
                    label = " - $translations",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
    Divider()
}
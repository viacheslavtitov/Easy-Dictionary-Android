package org.easydictionary.app.view.screens.word

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easydictionary.app.domain.models.word.WordTag

@Composable
fun TagChip(
    tag: WordTag,
    onTagClicked: (WordTag) -> Unit = {}
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            onClick = { onTagClicked(tag) },
            label = { Text(tag.name) },
            selected = tag.selected
        )
    }
}
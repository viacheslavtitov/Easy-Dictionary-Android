package org.easydictionary.app.view.screens.word

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewLabel
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.word.WordTag
import org.easydictionary.app.view.dividers.Divider
import org.easydictionary.app.view.inputs.TextFieldPrimary
import org.easydictionary.app.view.widget.global.getCurrentColorScheme

@Composable
fun TagsBottomSheetModal(
    items: List<WordTag> = emptyList(),
    onAddNewClicked: () -> Unit = {},
    onNewValueChanged: (String) -> Unit = {},
    onTagClicked: (WordTag) -> Unit = {},
    newTagFieldErrorShow: Boolean = true,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextFieldPrimary(
                defaultValue = "",
                modifier = Modifier.weight(1f),
                onValueChange = { value -> onNewValueChanged(value) },
                required = true,
                errorMessage = if(newTagFieldErrorShow) stringResource(R.string.error_field_empty) else null,
                label = stringResource(R.string.add_tag)
            )
            IconButton(onClick = onAddNewClicked) {
                Icon(
                    imageVector = Icons.Default.NewLabel,
                    contentDescription = "Add tag",
                    modifier = Modifier.size(64.dp),
                    tint = getCurrentColorScheme().primary
                )
            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    TagChip(
                        tag = item,
                        onTagClicked = onTagClicked
                    )
                }
            }
        }
    }
}
package org.easydictionary.app.view.widget.phonetic

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.easydictionary.app.R
import org.easydictionary.app.view.widget.global.notoSans

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhoneticsView(
    defaultValue: String = "",
    symbols: List<String>,
    onInsert: (String) -> Unit,
    onClose: () -> Unit
) {
    var value by rememberSaveable { mutableStateOf(defaultValue) }
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.phonetic_keyboard),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onClose) { Text(stringResource(R.string.close)) }
        }
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 48.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(symbols.size) { i ->
                FilledTonalButton(
                    onClick = {
                        onInsert(symbols[i])
                        value += symbols[i]
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        symbols[i],
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = notoSans
                    )
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
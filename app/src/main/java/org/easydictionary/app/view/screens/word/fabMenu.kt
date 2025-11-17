package org.easydictionary.app.view.screens.word

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.easydictionary.app.R

@Composable
fun FabMenu(
    onAddTranslationClicked: () -> Unit,
    onAddTagClicked: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 45f else 0f, label = "fab-rotate")

    BackHandler(enabled = expanded) { expanded = false }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 68.dp),
            visible = expanded,
            enter = fadeIn() +
                    expandHorizontally(expandFrom = Alignment.End) +
                    slideInHorizontally(initialOffsetX = { it / 2 }),
            exit = fadeOut() +
                    shrinkHorizontally(shrinkTowards = Alignment.End) +
                    slideOutHorizontally(targetOffsetX = { it / 2 })
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.add_translation)) },
                    icon = {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Add Translation"
                        )
                    },
                    onClick = {
                        onAddTranslationClicked()
                        expanded = false
                    }
                )
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.add_tag)) },
                    icon = {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Add Tag"
                        )
                    },
                    onClick = {
                        onAddTagClicked()
                        expanded = false
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded }
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = if (expanded) "Close" else "Open",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

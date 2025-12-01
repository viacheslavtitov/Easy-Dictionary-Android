@file:OptIn(ExperimentalMaterial3Api::class)

package org.easydictionary.app.view.topbars

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.easydictionary.app.R
import org.easydictionary.app.view.widget.global.TextDimen

@Composable
fun TitleTopBar(
    title: String
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TopBarTitle(title = title)
            }
        },
        modifier = Modifier
    )
}

@Composable
fun TitleTopBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TopBarTitle(title = title)
            }
        },
        actions = actions,
    )
}

@Composable
fun DrawerTitleTopBar(
    title: String,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TopBarTitle(title = title, modifier = Modifier.padding(end = 48.dp))
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    if (drawerState.isClosed) {
                        drawerState.open()
                    } else {
                        drawerState.close()
                    }
                }
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
}

@Composable
private fun TopBarTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(modifier = modifier, text = title, fontSize = 18.sp)
}

@Composable
fun SearchTopBar(
    title: String,
    placeHolderText: String,
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    onSearchToggle: () -> Unit,
    onClearQuery: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(isSearching) {
        if (isSearching) {
            yield()
            focusRequester.requestFocus()
            keyboard?.show()
        } else {
            keyboard?.hide()
        }
    }
    TopAppBar(
        title = {
            if (isSearching) {
                TextField(
                    onValueChange = onQueryChange,
                    value = query,
                    placeholder = { Text(placeHolderText) },
                    textStyle = TextStyle(
                        fontSize = TextDimen.TextFieldText
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            } else {
                TopBarTitle(title)
            }
        },
        actions = {
            if (isSearching) {
                IconButton(onClick = onClearQuery) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            } else {
                IconButton(onClick = onSearchToggle) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                actions()
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun FilterableSearchTopBar(
    isSearchingEnabled: MutableState<Boolean>,
    onSearchSubmit: (String) -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    expandedContent: @Composable () -> Unit,
    onClearClicked: () -> Unit,
    onDateRangeClicked: () -> Unit,
    isDateRangeFilled: Boolean
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    BackHandler(enabled = expanded) { expanded = false }
    LaunchedEffect(expanded) {
        if (expanded) {
            yield()
            focusRequester.requestFocus()
            keyboard?.show()
        }
        isSearchingEnabled.value = expanded
    }
    val horizontalPadding by animateDpAsState(
        targetValue = if (expanded) 0.dp else 16.dp,
        label = "searchbar-padding"
    )
    val verticalPadding = if (expanded) 0.dp else 8.dp
    SearchBar(
        windowInsets = WindowInsets
            .safeDrawing
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        expanded = expanded,
        onExpandedChange = { expanded = it },
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    onSearchSubmit(query)
                    expanded = false
                    keyboard?.hide()
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = {
                    if (expanded) {
                        IconButton({ expanded = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Search Back")
                        }
                    } else {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                trailingIcon = {
                    Row {
                        if (query.isNotEmpty()) {
                            IconButton({
                                query = ""
                                onClearClicked()
                            }) {
                                Icon(Icons.Filled.Close, "Clear filter")
                            }
                        }
                        if (expanded) {
                            IconButton({
                                onDateRangeClicked()
                            }) {
                                BadgedBox(
                                    badge = {
                                        if (isDateRangeFilled) {
                                            Badge()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.DateRange, "Filter Date Range")
                                }
                            }
                        } else {
                            actions()
                        }
                    }
                }
            )
        }
    ) {
        if (expanded) {
            expandedContent()
        }
    }
}
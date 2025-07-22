package org.easydictionary.app.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.home.HomeViewModel
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.view.buttons.ButtonPrimary
import org.easydictionary.app.view.dictionary.DictionariesScreen
import org.easydictionary.app.view.dividers.Divider
import org.easydictionary.app.view.topbars.DrawerTitleTopBar
import org.easydictionary.app.view.widget.global.LightColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    defaultSelectedRoute: String = AppNavigation.DictionariesScreen.route,
    sharedMainViewModel: SharedMainViewModel
) {
    val isDark = isSystemInDarkTheme()
    val drawerContainerColor =
        if (isDark)
            LightColors.Secondary_Screen_Background
        else
            LightColors.Secondary_Screen_Background
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
    var selectedSection = rememberSaveable { mutableStateOf(defaultSelectedRoute) }
    val onSectionSelected: (String) -> Unit = { newSelectedRoute: String ->
        selectedSection.value = newSelectedRoute
        scope.launch {
            drawerState.close()
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = drawerContainerColor,
                drawerTonalElevation = 0.dp
            ) {
                Text(
                    text = stringResource(R.string.menu), fontSize = 18.sp, color = if (isDark)
                        LightColors.Text_Secondary
                    else
                        LightColors.Text_Secondary,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                )
                DrawerItem(
                    title = stringResource(R.string.my_dictionaries),
                    icon = ImageVector.vectorResource(R.drawable.ic_dictionary),
                    routeName = AppNavigation.DictionariesScreen.route,
                    selectedRoute = selectedSection,
                    onSectionSelected = onSectionSelected
                )
                DrawerItem(
                    title = stringResource(R.string.my_quizzes),
                    icon = Icons.Default.Quiz,
                    routeName = AppNavigation.UserQuizScreen.route,
                    selectedRoute = selectedSection,
                    onSectionSelected = onSectionSelected
                )
                DrawerItem(
                    title = stringResource(R.string.settings),
                    icon = Icons.Default.Settings,
                    routeName = AppNavigation.SettingsScreen.route,
                    selectedRoute = selectedSection,
                    onSectionSelected = onSectionSelected
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                ButtonPrimary(
                    title = stringResource(R.string.log_out),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = true
                ) {
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                when (selectedSection.value) {
                    AppNavigation.DictionariesScreen.route -> DrawerTitleTopBar(
                        title = stringResource(R.string.my_dictionaries),
                        scope = scope,
                        drawerState = drawerState
                    )

//                    "search" -> SearchTopBar(
//                        query = "",
//                        onQueryChange = { /* оновлення query */ },
//                        onSearch = { /* пошук */ }
//                    )

                    else -> {}
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
            ) {
                when (selectedSection.value) {
                    AppNavigation.DictionariesScreen.route -> DictionariesScreen(
                        navController = navController,
                        sharedMainViewModel = sharedMainViewModel
                    )

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    title: String,
    icon: ImageVector,
    onSectionSelected: (String) -> Unit,
    selectedRoute: State<String>,
    routeName: String
) {
    val isSelected by remember {
        derivedStateOf { selectedRoute.value == routeName }
    }

    val isDark = isSystemInDarkTheme()
    val selectedContainerColor =
        if (isDark)
            LightColors.Secondary_Dark
        else
            LightColors.Secondary_Dark
    val selectedIconColor =
        if (isDark)
            LightColors.Text_Main
        else
            LightColors.Text_Main
    val unselectedIconColor =
        if (isDark)
            LightColors.Outlined
        else
            LightColors.Outlined
    val selectedTextColor =
        if (isDark)
            LightColors.Text_Main
        else
            LightColors.Text_Main
    val unselectedTextColor =
        if (isDark)
            LightColors.Text_Secondary
        else
            LightColors.Text_Secondary
    val titleTextWeight =
        if (isSelected)
            FontWeight.Bold
        else
            FontWeight.Normal

    NavigationDrawerItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        label = {
            Text(text = title, fontWeight = titleTextWeight, fontSize = 18.sp)
        },
        selected = isSelected,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp)
            )
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = selectedContainerColor,
            unselectedContainerColor = Color.Transparent,
            selectedIconColor = selectedIconColor,
            unselectedIconColor = unselectedIconColor,
            selectedTextColor = selectedTextColor,
            unselectedTextColor = unselectedTextColor
        ),
        onClick = {
            onSectionSelected(routeName)
        }
    )
}
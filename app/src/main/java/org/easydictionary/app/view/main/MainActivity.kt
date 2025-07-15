package org.easydictionary.app.view.main

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.view.home.HomeScreen
import org.easydictionary.app.view.indicators.LoadingIndicatorCircle
import org.easydictionary.app.view.register.SignUpScreen
import org.easydictionary.app.view.signin.SignInScreen
import org.easydictionary.app.view.splash.SplashScreen
import org.easydictionary.app.view.widget.global.EasyDictionaryTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private val sharedViewModel: SharedMainViewModel by viewModels()

//    private lateinit var toolbar: MaterialToolbar
//    private lateinit var navDrawerLayout: DrawerLayout
//    private lateinit var navView: NavigationView
//    private lateinit var userLogo: AppCompatImageView
//    private lateinit var userEmail: AppCompatTextView
//    private lateinit var progressBar: LinearProgressIndicator
//    private lateinit var extendActionButton: ExtendedFloatingActionButton
//    private lateinit var tagActionButton: FloatingActionButton
//    private lateinit var translationActionButton: FloatingActionButton
//    private var isExpandedActionButtons = false

//    private val navController: NavController by lazy {
//        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
//    }
//
//    private val appBarConfiguration: AppBarConfiguration by lazy {
//        AppBarConfiguration(navController.graph, navDrawerLayout)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyDictionaryTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
//                        .padding(WindowInsets.systemBars.asPaddingValues())
                ) {
                    AppNavHost()
                    val loadingState by sharedViewModel.loadingUIState
                    if (loadingState) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .zIndex(1f), // always in the top
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicatorCircle()
                        }
                    }
                }
            }
        }
//        setContentView(R.layout.activity_main)
//        visibleSystemBars(visible = true, type = WindowInsetsCompat.Type.statusBars())
//        visibleSystemBars(visible = true, type = WindowInsetsCompat.Type.systemBars())
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
//        toolbar = findViewById(R.id.toolbar)
//        navDrawerLayout = findViewById(R.id.drawer_layout)
//        navView = findViewById(R.id.nav_view)
//        userLogo = navView.getHeaderView(0).findViewById(R.id.user_logo)
//        userEmail = navView.getHeaderView(0).findViewById(R.id.user_email)
//        progressBar = findViewById(R.id.progress_bar)
//        extendActionButton = findViewById(R.id.floating_action_extend)
//        tagActionButton = findViewById(R.id.floating_action_tag)
//        translationActionButton = findViewById(R.id.floating_action_translation)
//        findViewById<View>(R.id.nav_log_out).setOnClickListener {
//            logOut()
//        }
//        extendActionButton.setOnClickListener {
//            handleActionFloatingActionButton()
//        }
//        tagActionButton.setOnClickListener {
//            sharedViewModel.actionNavigate(AddTagNavigation())
//        }
//        translationActionButton.setOnClickListener {
//            sharedViewModel.actionNavigate(AddTranslationVariantNavigation())
//        }
//        isExpandedActionButtons = false
//        tagActionButton.hide()
//        translationActionButton.hide()
//        setSupportActionBar(toolbar)
//        supportActionBar?.let { actionBar ->
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setDisplayShowHomeEnabled(true)
//            actionBar.setHomeButtonEnabled(true)
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
//        }
//        navView.setupWithNavController(navController)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        toolbar.setupWithNavController(navController, navDrawerLayout)
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            sharedViewModel.showOrHideActionButton(false)
//            navDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
//            var newTitle = ""
//            when (destination.id) {
//                R.id.userDictionaryFragment -> {
//                    newTitle = getString(R.string.my_dictionaries)
//                }
//
//                R.id.addUserDictionaryFragment -> {
//                    newTitle = getString(R.string.add_dictionary)
//                }
//
//                R.id.languagesFragment -> {
//                    newTitle = getString(R.string.add_language)
//                }
//
//                R.id.dictionaryWordsFragment -> {
//                    newTitle = getString(R.string.words)
//                }
//
//                R.id.addDictionaryWordFragment -> {
//                    newTitle = getString(R.string.add_word)
//                    sharedViewModel.showOrHideActionButton(true)
//                }
//
//                R.id.addTranslationVariant -> {
//                    newTitle = getString(R.string.add_translation_variants)
//                }
//
//                R.id.quizDetailTabsFragment -> {
//                    newTitle = getString(R.string.quiz)
//                }
//
//                R.id.runQuizFragment -> {
//                    newTitle = getString(R.string.quiz)
//                }
//
//                R.id.addWordTagsFragment -> {
//                    newTitle = getString(R.string.add_or_choose_tags)
//                }
//
//                R.id.dictionaryWordsFilterFragment -> {
//                    newTitle = getString(R.string.filter)
//                }
//
//                R.id.userQuizzesFragment -> {
//                    newTitle = getString(R.string.my_quizzes)
//                }
//
//                R.id.simpleFragment -> {
//                    toolbar.title = "Home"
//                    toolbar.menu.clear()
//                    navDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
//                }
//            }
//            sharedViewModel.saveTitle(newTitle)
//        }
//        navDrawerLayout.addDrawerListener(drawerToggle)
//        drawerToggle.isDrawerIndicatorEnabled = false
//        drawerToggle.setToolbarNavigationClickListener {
//            if (navController.currentDestination != null && navController.currentDestination!!.id != R.id.simpleFragment) {
//                navController.popBackStack()
//            } else {
//                navDrawerLayout.openDrawer(GravityCompat.START)
//            }
//        }
//        navView.setNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_settings -> {
//
//                }
//
//                R.id.nav_user_dictionary -> {
//                    navController.navigate(R.id.userDictionaryFragment)
//                }
//
//                R.id.nav_quize -> {
//                    navController.navigate(R.id.userQuizzesFragment)
//                }
//            }
//            navDrawerLayout.closeDrawer(GravityCompat.START)
//            true
//        }
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                    sharedViewModel.loadingUIState.collect { visible ->
//                        progressBar.visible(visible, View.GONE)
//                    }
//                }
//            }
//        }
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                    sharedViewModel.navigation.drop(1).collect { navigation ->
//                        navigate(navigation)
//                    }
//                }
//            }
//        }
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                sharedViewModel.showActionButtonUIState.collect { show ->
//                    isExpandedActionButtons = false
//                    if (show) {
//                        extendActionButton.show()
//                    } else {
//                        extendActionButton.hide()
//                        tagActionButton.hide()
//                        translationActionButton.hide()
//                    }
//                }
//            }
//        }
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                sharedViewModel.titleSavedUIState.collect { value ->
//                    toolbar.setTitle(value)
//                }
//            }
//        }
    }
//
//    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        super.onPostCreate(savedInstanceState, persistentState)
//        drawerToggle.syncState()
//    }


    private fun navigate(navigation: AppNavigation) {
//        when (navigation) {
//            is SplashScreen -> {
//
//            }
//            is SignInScreen -> {
//
//            }
//            is SignUpScreen -> {
//
//            }
//            is HomeScreen -> {
//
//            }
//
//            is LanguagesScreen -> {
//                val bundle = Bundle().apply {
//                    putInt(
//                        LanguagesFragment.BUNDLE_LANGUAGE_TYPE_KEY,
//                        navigation.langType.ordinal
//                    )
//                }
//                navController.navigate(
//                    R.id.action_addUserDictionaryFragment_to_languagesFragment,
//                    bundle
//                )
//            }
//
//            is AddUserDictionaryScreen -> {
//                navController.navigate(R.id.action_userDictionaryFragment_to_addUserDictionaryFragment)
//            }
//
//            is EditDictionaryScreen -> {
//                val bundle = Bundle().apply {
//                    putParcelable(
//                        AddUserDictionaryFragment.BUNDLE_DICTIONARY,
//                        navigation.dictionary
//                    )
//                }
//                navController.navigate(
//                    R.id.action_userDictionaryFragment_to_addUserDictionaryFragment,
//                    bundle
//                )
//            }
//
//            is DictionaryWordsScreen -> {
//                val bundle = Bundle().apply {
//                    putString(
//                        DictionaryWordsFragment.BUNDLE_DICTIONARY_ID,
//                        navigation.dictionary._id
//                    )
//                }
//                navController.navigate(
//                    R.id.action_userDictionaryFragment_to_dictionaryWordsFragment,
//                    bundle
//                )
//            }
//
//            is AddDictionaryWordScreen -> {
//                val bundle = Bundle().apply {
//                    putString(
//                        AddDictionaryWordFragment.BUNDLE_DICTIONARY_ID,
//                        navigation.dictionaryId
//                    )
//                }
//                navController.navigate(
//                    R.id.action_dictionaryWordsFragment_to_addDictionaryWordFragment,
//                    bundle
//                )
//            }
//
//            is EditDictionaryWordScreen -> {
//                val bundle = Bundle().apply {
//                    putString(
//                        AddDictionaryWordFragment.BUNDLE_DICTIONARY_ID,
//                        navigation.word.dictionaryId
//                    )
//                    putParcelable(
//                        AddDictionaryWordFragment.BUNDLE_WORD,
//                        navigation.word
//                    )
//                }
//                navController.navigate(
//                    R.id.action_dictionaryWordsFragment_to_addDictionaryWordFragment,
//                    bundle
//                )
//            }
//
//            is AddTranslationVariantsScreen -> {
//                val bundle = Bundle().apply {
//                    putString(
//                        AddTranslationVariantFragment.BUNDLE_TRANSLATE_WORD,
//                        navigation.word
//                    )
//                }
//                navController.navigate(
//                    R.id.action_addDictionaryWordFragment_to_addTranslationVariant,
//                    bundle
//                )
//            }
//
//            is EditTranslationVariantsScreen -> {
//                val bundle = Bundle().apply {
//                    putString(
//                        AddTranslationVariantFragment.BUNDLE_TRANSLATE_WORD,
//                        navigation.word
//                    )
//                    putString(
//                        AddTranslationVariantFragment.BUNDLE_DICTIONARY_ID,
//                        navigation.dictionaryId
//                    )
//                    putParcelable(
//                        AddTranslationVariantFragment.BUNDLE_TRANSLATION,
//                        navigation.translation
//                    )
//                }
//                navController.navigate(
//                    R.id.action_addDictionaryWordFragment_to_addTranslationVariant,
//                    bundle
//                )
//            }
//
//            is UserQuizScreen -> {
//                val bundle = Bundle().apply {
//                    putString(
//                        QuizDetailTabsFragment.BUNDLE_QUIZ_ID,
//                        navigation.quiz._id
//                    )
//                }
//                navController.navigate(
//                    R.id.action_userQuizzesFragment_to_quizDetailFragment,
//                    bundle
//                )
//            }
//
//            is RunQuizScreen -> {
//                val bundle = Bundle().apply {
//                    putParcelable(
//                        RunQuizFragment.BUNDLE_QUIZ,
//                        navigation.quiz
//                    )
//                }
//                navController.navigate(
//                    R.id.action_quizDetailFragment_to_runQuizFragment,
//                    bundle
//                )
//            }
//
//            is EditQuizScreenFromDetail -> {
//                val bundle = Bundle().apply {
//                    putParcelable(
//                        AddQuizFragment.BUNDLE_QUIZ,
//                        navigation.quiz
//                    )
//                }
//                navController.navigate(
//                    R.id.action_quizDetailTabsFragment_to_addQuizFragment,
//                    bundle
//                )
//            }
//
//            is EditQuizScreenFromQuizList -> {
//                val bundle = Bundle().apply {
//                    putParcelable(
//                        AddQuizFragment.BUNDLE_QUIZ,
//                        navigation.quiz
//                    )
//                }
//                navController.navigate(
//                    R.id.action_userQuizzesFragment_to_addQuizFragment,
//                    bundle
//                )
//            }
//
//            is AddUserQuizScreen -> {
//                navController.navigate(R.id.action_userQuizzesFragment_to_addQuizFragment)
//            }
//
//            is WordsMultiChooseScreen -> {
//                val bundle = Bundle().apply {
//                    putString(
//                        WordsMultiChooseFragment.BUNDLE_DICTIONARY_ID,
//                        navigation.dictionaryId
//                    )
//                    putParcelableArrayList(
//                        WordsMultiChooseFragment.BUNDLE_WORDS,
//                        navigation.words
//                    )
//                }
//                navController.navigate(
//                    R.id.action_addQuizFragment_to_wordsMultiChooseFragment,
//                    bundle
//                )
//            }
//
//            is DictionaryChooseScreen -> {
//                navController.navigate(R.id.action_addQuizFragment_to_dictionaryChooseDialogFragment)
//            }
//
//            is AddWordTagsScreen -> {
//                val bundle = Bundle().apply {
//                    putParcelable(
//                        AddWordTagsFragment.BUNDLE_WORD,
//                        navigation.word
//                    )
//                    putParcelable(
//                        AddWordTagsFragment.BUNDLE_DICTIONARY,
//                        navigation.dictionary
//                    )
//                }
//                navController.navigate(
//                    R.id.action_addDictionaryWordFragment_to_addWordTagsFragment,
//                    bundle
//                )
//            }
//
//            is DictionaryFilterScreen -> {
//                val bundle = Bundle().apply {
//                    putParcelable(
//                        DictionaryWordsFilterFragment.BUNDLE_DICTIONARY,
//                        navigation.dictionary
//                    )
//                    putParcelable(
//                        DictionaryWordsFilterFragment.BUNDLE_FILTER,
//                        navigation.filterModel
//                    )
//                }
//                navController.navigate(
//                    R.id.action_dictionaryWordsFragment_to_dictionaryWordsFilterFragment,
//                    bundle
//                )
//            }
//
//            is DictionaryMultiChooseFilterScreen -> {
//                val bundle = Bundle().apply {
//                    putParcelable(
//                        DictionaryWordsFilterFragment.BUNDLE_DICTIONARY,
//                        navigation.dictionary
//                    )
//                    putParcelable(
//                        DictionaryWordsFilterFragment.BUNDLE_FILTER,
//                        navigation.filterModel
//                    )
//                }
//                navController.navigate(
//                    R.id.action_wordsMultiChooseFragment_to_dictionaryWordsFilterFragment,
//                    bundle
//                )
//            }
//        }
    }

    @Composable
    fun AppNavHost(navController: NavHostController = rememberNavController()) {
        NavHost(
            navController = navController,
            startDestination = AppNavigation.SplashScreen.route
        ) {
            composable(route = AppNavigation.SplashScreen.route) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                SplashScreen(navController)
            }
            composable(route = AppNavigation.SignInScreen.route) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                SignInScreen(navController, sharedMainViewModel = sharedViewModel)
            }
            composable(route = AppNavigation.SignUpScreen.route) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                SignUpScreen(navController, sharedMainViewModel = sharedViewModel)
            }
            composable(route = AppNavigation.HomeScreen.route) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                HomeScreen(navController, sharedMainViewModel = sharedViewModel)
            }

//            composable(
//                route = Screen.Detail.route,
//                arguments = listOf(navArgument("itemId") { type = NavType.IntType })
//            ) { backStackEntry ->
//                val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
//                DetailScreen(itemId)
//            }
        }
    }


    private fun handleActionFloatingActionButton() {
//        Log.d(TAG, "clicked on Floating Button, isExtended = $isExpandedActionButtons")
//        if (isExpandedActionButtons) {
//            isExpandedActionButtons = false
//            tagActionButton.hide()
//            translationActionButton.hide()
//            extendActionButton.shrink()
//        } else {
//            isExpandedActionButtons = true
//            tagActionButton.show()
//            translationActionButton.show()
//            extendActionButton.extend()
//        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }
}
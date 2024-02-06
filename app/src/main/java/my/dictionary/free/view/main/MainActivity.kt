package my.dictionary.free.view.main

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.navigation.AddDictionaryWordScreen
import my.dictionary.free.domain.models.navigation.AddTagNavigation
import my.dictionary.free.domain.models.navigation.AddTranslationVariantNavigation
import my.dictionary.free.domain.models.navigation.AddTranslationVariantsScreen
import my.dictionary.free.domain.models.navigation.AddUserDictionaryScreen
import my.dictionary.free.domain.models.navigation.AddUserQuizScreen
import my.dictionary.free.domain.models.navigation.AddWordTagsScreen
import my.dictionary.free.domain.models.navigation.AppNavigation
import my.dictionary.free.domain.models.navigation.DictionaryChooseScreen
import my.dictionary.free.domain.models.navigation.DictionaryFilterScreen
import my.dictionary.free.domain.models.navigation.DictionaryMultiChooseFilterScreen
import my.dictionary.free.domain.models.navigation.DictionaryWordsScreen
import my.dictionary.free.domain.models.navigation.EditDictionaryScreen
import my.dictionary.free.domain.models.navigation.EditDictionaryWordScreen
import my.dictionary.free.domain.models.navigation.EditQuizScreenFromDetail
import my.dictionary.free.domain.models.navigation.EditQuizScreenFromQuizList
import my.dictionary.free.domain.models.navigation.EditTranslationVariantsScreen
import my.dictionary.free.domain.models.navigation.HomeScreen
import my.dictionary.free.domain.models.navigation.LanguagesScreen
import my.dictionary.free.domain.models.navigation.RunQuizScreen
import my.dictionary.free.domain.models.navigation.UserQuizScreen
import my.dictionary.free.domain.models.navigation.WordsMultiChooseScreen
import my.dictionary.free.domain.viewmodels.main.SharedMainViewModel
import my.dictionary.free.view.AbstractBaseActivity
import my.dictionary.free.view.ext.visible
import my.dictionary.free.view.ext.visibleSystemBars
import my.dictionary.free.view.quiz.add.AddQuizFragment
import my.dictionary.free.view.quiz.detail.QuizDetailTabsFragment
import my.dictionary.free.view.quiz.run.RunQuizFragment
import my.dictionary.free.view.splash.SplashActivity
import my.dictionary.free.view.user.dictionary.add.AddUserDictionaryFragment
import my.dictionary.free.view.user.dictionary.add.languages.LanguagesFragment
import my.dictionary.free.view.user.dictionary.words.DictionaryWordsFragment
import my.dictionary.free.view.user.dictionary.words.add.AddDictionaryWordFragment
import my.dictionary.free.view.user.dictionary.words.choose.WordsMultiChooseFragment
import my.dictionary.free.view.user.dictionary.words.filter.DictionaryWordsFilterFragment
import my.dictionary.free.view.user.dictionary.words.tags.AddWordTagsFragment
import my.dictionary.free.view.user.dictionary.words.translations.add.AddTranslationVariantFragment

@AndroidEntryPoint
class MainActivity : AbstractBaseActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private val sharedViewModel: SharedMainViewModel by viewModels()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var navDrawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var userLogo: AppCompatImageView
    private lateinit var userEmail: AppCompatTextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var extendActionButton: ExtendedFloatingActionButton
    private lateinit var tagActionButton: FloatingActionButton
    private lateinit var translationActionButton: FloatingActionButton
    private var isExpandedActionButtons = false

    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }

    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(navController.graph, navDrawerLayout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAuth.getInstance().addAuthStateListener(signOutListener)
        setContentView(R.layout.activity_main)
        visibleSystemBars(visible = true, type = WindowInsetsCompat.Type.statusBars())
        visibleSystemBars(visible = true, type = WindowInsetsCompat.Type.systemBars())
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        toolbar = findViewById(R.id.toolbar)
        navDrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        userLogo = navView.getHeaderView(0).findViewById(R.id.user_logo)
        userEmail = navView.getHeaderView(0).findViewById(R.id.user_email)
        progressBar = findViewById(R.id.progress_bar)
        extendActionButton = findViewById(R.id.floating_action_extend)
        tagActionButton = findViewById(R.id.floating_action_tag)
        translationActionButton = findViewById(R.id.floating_action_translation)
        findViewById<View>(R.id.nav_log_out).setOnClickListener {
            logOut()
        }
        extendActionButton.setOnClickListener {
            handleActionFloatingActionButton()
        }
        tagActionButton.setOnClickListener {
            sharedViewModel.actionNavigate(AddTagNavigation())
        }
        translationActionButton.setOnClickListener {
            sharedViewModel.actionNavigate(AddTranslationVariantNavigation())
        }
        isExpandedActionButtons = false
        tagActionButton.hide()
        translationActionButton.hide()
        setSupportActionBar(toolbar)
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }
        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        toolbar.setupWithNavController(navController, navDrawerLayout)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            sharedViewModel.showOrHideActionButton(false)
            navDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            when (destination.id) {
                R.id.userDictionaryFragment -> {
                    toolbar.setTitle(R.string.my_dictionaries)
                }

                R.id.addUserDictionaryFragment -> {
                    toolbar.setTitle(R.string.add_dictionary)
                }

                R.id.languagesFragment -> {
                    toolbar.setTitle(R.string.add_language)
                }

                R.id.dictionaryWordsFragment -> {
                    toolbar.setTitle(R.string.words)
                }

                R.id.addDictionaryWordFragment -> {
                    toolbar.setTitle(R.string.add_word)
                    sharedViewModel.showOrHideActionButton(true)
                }

                R.id.addTranslationVariant -> {
                    toolbar.setTitle(R.string.add_translation_variants)
                }

                R.id.quizDetailTabsFragment -> {
                    toolbar.setTitle(R.string.quiz)
                }

                R.id.runQuizFragment -> {
                    toolbar.setTitle(R.string.quiz)
                }

                R.id.addWordTagsFragment -> {
                    toolbar.setTitle(R.string.add_or_choose_tags)
                }

                R.id.dictionaryWordsFilterFragment -> {
                    toolbar.setTitle(R.string.filter)
                }

                R.id.simpleFragment -> {
                    toolbar.title = "Home"
                    toolbar.menu.clear()
                    navDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
        }
        navDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = false
        drawerToggle.setToolbarNavigationClickListener {
            if (navController.currentDestination != null && navController.currentDestination!!.id != R.id.simpleFragment) {
                navController.popBackStack()
            } else {
                navDrawerLayout.openDrawer(GravityCompat.START)
            }
        }
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> {

                }

                R.id.nav_user_dictionary -> {
                    navController.navigate(R.id.userDictionaryFragment)
                }

                R.id.nav_quize -> {
                    navController.navigate(R.id.userQuizzesFragment)
                }
            }
            navDrawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.loadingUIState.collect { visible ->
                        progressBar.visible(visible, View.GONE)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.navigation.drop(1).collect { navigation ->
                        navigate(navigation)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.userEmailValue.drop(1).collect { email ->
                        userEmail.text = email
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.userAvatarUri.collect { uri ->
                        Glide
                            .with(applicationContext)
                            .load(uri)
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_baseline_account_circle_24)
                            .into(userLogo)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.toolbarTitleUIState.collect { title ->
                    if (!title.isNullOrEmpty()) {
                        toolbar.title = title
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.showActionButtonUIState.collect { show ->
                    isExpandedActionButtons = false
                    if (show) {
                        extendActionButton.show()
                    } else {
                        extendActionButton.hide()
                        tagActionButton.hide()
                        translationActionButton.hide()
                    }
                }
            }
        }
        navController.navigate(R.id.simpleFragment)
        sharedViewModel.loadUserData()
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        drawerToggle.syncState()
    }

    private fun logOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                sharedViewModel.clearData()
                FirebaseAuth.getInstance().signOut()
            }
        }
    }

    private val signOutListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser == null) {
            val intent = Intent(applicationContext, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (navDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            navDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (navController.currentBackStack.value.isEmpty()) {
                finish()
            } else {
                super.onBackPressed()
            }
        }
    }

    private val drawerToggle: ActionBarDrawerToggle by lazy {
        object : ActionBarDrawerToggle(
            this,
            navDrawerLayout,
            toolbar,
            R.string.menu_drawer_open,
            R.string.menu_drawer_closed
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
            }
        }
    }

    private fun navigate(navigation: AppNavigation) {
        when (navigation) {
            is HomeScreen -> {

            }

            is LanguagesScreen -> {
                val bundle = Bundle().apply {
                    putInt(
                        LanguagesFragment.BUNDLE_LANGUAGE_TYPE_KEY,
                        navigation.langType.ordinal
                    )
                }
                navController.navigate(
                    R.id.action_addUserDictionaryFragment_to_languagesFragment,
                    bundle
                )
            }

            is AddUserDictionaryScreen -> {
                navController.navigate(R.id.action_userDictionaryFragment_to_addUserDictionaryFragment)
            }

            is EditDictionaryScreen -> {
                val bundle = Bundle().apply {
                    putParcelable(
                        AddUserDictionaryFragment.BUNDLE_DICTIONARY,
                        navigation.dictionary
                    )
                }
                navController.navigate(
                    R.id.action_userDictionaryFragment_to_addUserDictionaryFragment,
                    bundle
                )
            }

            is DictionaryWordsScreen -> {
                val bundle = Bundle().apply {
                    putString(
                        DictionaryWordsFragment.BUNDLE_DICTIONARY_ID,
                        navigation.dictionary._id
                    )
                }
                navController.navigate(
                    R.id.action_userDictionaryFragment_to_dictionaryWordsFragment,
                    bundle
                )
            }

            is AddDictionaryWordScreen -> {
                val bundle = Bundle().apply {
                    putString(
                        AddDictionaryWordFragment.BUNDLE_DICTIONARY_ID,
                        navigation.dictionaryId
                    )
                }
                navController.navigate(
                    R.id.action_dictionaryWordsFragment_to_addDictionaryWordFragment,
                    bundle
                )
            }

            is EditDictionaryWordScreen -> {
                val bundle = Bundle().apply {
                    putString(
                        AddDictionaryWordFragment.BUNDLE_DICTIONARY_ID,
                        navigation.word.dictionaryId
                    )
                    putParcelable(
                        AddDictionaryWordFragment.BUNDLE_WORD,
                        navigation.word
                    )
                }
                navController.navigate(
                    R.id.action_dictionaryWordsFragment_to_addDictionaryWordFragment,
                    bundle
                )
            }

            is AddTranslationVariantsScreen -> {
                val bundle = Bundle().apply {
                    putString(
                        AddTranslationVariantFragment.BUNDLE_TRANSLATE_WORD,
                        navigation.word
                    )
                }
                navController.navigate(
                    R.id.action_addDictionaryWordFragment_to_addTranslationVariant,
                    bundle
                )
            }

            is EditTranslationVariantsScreen -> {
                val bundle = Bundle().apply {
                    putString(
                        AddTranslationVariantFragment.BUNDLE_TRANSLATE_WORD,
                        navigation.word
                    )
                    putString(
                        AddTranslationVariantFragment.BUNDLE_DICTIONARY_ID,
                        navigation.dictionaryId
                    )
                    putParcelable(
                        AddTranslationVariantFragment.BUNDLE_TRANSLATION,
                        navigation.translation
                    )
                }
                navController.navigate(
                    R.id.action_addDictionaryWordFragment_to_addTranslationVariant,
                    bundle
                )
            }

            is UserQuizScreen -> {
                val bundle = Bundle().apply {
                    putString(
                        QuizDetailTabsFragment.BUNDLE_QUIZ_ID,
                        navigation.quiz._id
                    )
                }
                navController.navigate(
                    R.id.action_userQuizzesFragment_to_quizDetailFragment,
                    bundle
                )
            }

            is RunQuizScreen -> {
                val bundle = Bundle().apply {
                    putParcelable(
                        RunQuizFragment.BUNDLE_QUIZ,
                        navigation.quiz
                    )
                }
                navController.navigate(
                    R.id.action_quizDetailFragment_to_runQuizFragment,
                    bundle
                )
            }

            is EditQuizScreenFromDetail -> {
                val bundle = Bundle().apply {
                    putParcelable(
                        AddQuizFragment.BUNDLE_QUIZ,
                        navigation.quiz
                    )
                }
                navController.navigate(
                    R.id.action_quizDetailTabsFragment_to_addQuizFragment,
                    bundle
                )
            }

            is EditQuizScreenFromQuizList -> {
                val bundle = Bundle().apply {
                    putParcelable(
                        AddQuizFragment.BUNDLE_QUIZ,
                        navigation.quiz
                    )
                }
                navController.navigate(
                    R.id.action_userQuizzesFragment_to_addQuizFragment,
                    bundle
                )
            }

            is AddUserQuizScreen -> {
                navController.navigate(R.id.action_userQuizzesFragment_to_addQuizFragment)
            }

            is WordsMultiChooseScreen -> {
                val bundle = Bundle().apply {
                    putString(
                        WordsMultiChooseFragment.BUNDLE_DICTIONARY_ID,
                        navigation.dictionaryId
                    )
                    putParcelableArrayList(
                        WordsMultiChooseFragment.BUNDLE_WORDS,
                        navigation.words
                    )
                }
                navController.navigate(
                    R.id.action_addQuizFragment_to_wordsMultiChooseFragment,
                    bundle
                )
            }

            is DictionaryChooseScreen -> {
                navController.navigate(R.id.action_addQuizFragment_to_dictionaryChooseDialogFragment)
            }

            is AddWordTagsScreen -> {
                val bundle = Bundle().apply {
                    putParcelable(
                        AddWordTagsFragment.BUNDLE_WORD,
                        navigation.word
                    )
                    putParcelable(
                        AddWordTagsFragment.BUNDLE_DICTIONARY,
                        navigation.dictionary
                    )
                }
                navController.navigate(
                    R.id.action_addDictionaryWordFragment_to_addWordTagsFragment,
                    bundle
                )
            }

            is DictionaryFilterScreen -> {
                val bundle = Bundle().apply {
                    putParcelable(
                        DictionaryWordsFilterFragment.BUNDLE_DICTIONARY,
                        navigation.dictionary
                    )
                    putParcelable(
                        DictionaryWordsFilterFragment.BUNDLE_FILTER,
                        navigation.filterModel
                    )
                }
                navController.navigate(
                    R.id.action_dictionaryWordsFragment_to_dictionaryWordsFilterFragment,
                    bundle
                )
            }

            is DictionaryMultiChooseFilterScreen -> {
                val bundle = Bundle().apply {
                    putParcelable(
                        DictionaryWordsFilterFragment.BUNDLE_DICTIONARY,
                        navigation.dictionary
                    )
                    putParcelable(
                        DictionaryWordsFilterFragment.BUNDLE_FILTER,
                        navigation.filterModel
                    )
                }
                navController.navigate(
                    R.id.action_wordsMultiChooseFragment_to_dictionaryWordsFilterFragment,
                    bundle
                )
            }
        }
    }

    private fun handleActionFloatingActionButton() {
        Log.d(TAG, "clicked on Floating Button, isExtended = $isExpandedActionButtons")
        if (isExpandedActionButtons) {
            isExpandedActionButtons = false
            tagActionButton.hide()
            translationActionButton.hide()
            extendActionButton.shrink()
        } else {
            isExpandedActionButtons = true
            tagActionButton.show()
            translationActionButton.show()
            extendActionButton.extend()
        }
    }
}
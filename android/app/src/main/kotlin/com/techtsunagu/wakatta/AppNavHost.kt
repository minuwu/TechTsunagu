package com.techtsunagu.wakatta

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.TopLevelTab
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.core.navigation.BoardKey
import com.techtsunagu.wakatta.core.navigation.ChangePinKey
import com.techtsunagu.wakatta.core.navigation.ChatsKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.HomeKey
import com.techtsunagu.wakatta.core.navigation.MachinesKey
import com.techtsunagu.wakatta.core.navigation.MeKey
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.SignInKey
import com.techtsunagu.wakatta.core.navigation.TopLevelBackStack
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    val featureEntries: Set<@JvmSuppressWildcards FeatureEntries>,
    val backStack: TopLevelBackStack,
    val navigator: Navigator,
    val sessionStore: SessionStore,
    val fakeSessionStore: FakeSessionStore,
    val appConfig: AppConfig,
    val languageManager: LanguageManager,
) : ViewModel()

private val TOP_LEVEL_KEYS = setOf(
    HomeKey::class,
    ChatsKey::class,
    BoardKey::class,
    MachinesKey::class,
    MeKey::class,
)

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = hiltViewModel(),
) {
    AppNavHostContent(
        featureEntries = viewModel.featureEntries,
        backStack = viewModel.backStack,
        navigator = viewModel.navigator,
        sessionStore = viewModel.sessionStore,
        fakeSessionStore = viewModel.fakeSessionStore,
        languageManager = viewModel.languageManager,
        modifier = modifier,
    )
}

@Composable
fun AppNavHostContent(
    featureEntries: Set<FeatureEntries>,
    backStack: TopLevelBackStack,
    navigator: Navigator,
    sessionStore: SessionStore,
    fakeSessionStore: FakeSessionStore,
    languageManager: LanguageManager,
    modifier: Modifier = Modifier,
) {
    val session by sessionStore.session.collectAsStateWithLifecycle()
    val currentTab by backStack.currentTab.collectAsStateWithLifecycle()
    val currentStack by backStack.currentStack.collectAsStateWithLifecycle()

    // Locale gate: apply default ja for Foreman, en for Worker on first run
    LaunchedEffect(session) {
        session?.let { s ->
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (currentLocales.isEmpty) {
                val defaultLang = when (s.user.role) {
                    Role.FOREMAN -> "ja"
                    Role.WORKER -> if (s.user.language.isNotBlank()) s.user.language else "en"
                    else -> "ja"
                }
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(defaultLang))
                languageManager.setLanguage(defaultLang)
            }
        }
    }

    val mustChangePin = session?.mustChangePin == true
    if (session == null || mustChangePin) {
        // Session gate: only sign-in and change-PIN render
        val authBackStack = remember {
            mutableStateListOf<NavKey>(if (mustChangePin) ChangePinKey else SignInKey)
        }
        LaunchedEffect(mustChangePin) {
            if (mustChangePin && authBackStack.lastOrNull() != ChangePinKey) {
                authBackStack.clear()
                authBackStack.add(ChangePinKey)
            }
        }

        val authNavigator = remember(navigator, authBackStack, fakeSessionStore) {
            object : Navigator {
                override fun push(key: NavKey) {
                    authBackStack.add(key)
                }

                override fun pop(): Boolean {
                    return if (authBackStack.size > 1) {
                        authBackStack.removeAt(authBackStack.size - 1)
                        true
                    } else {
                        false
                    }
                }

                override fun switchTab(tab: TopLevelTab) {
                    navigator.switchTab(tab)
                }

                override fun switchTab(key: NavKey) {
                    navigator.switchTab(key)
                }

                override fun replaceTop(key: NavKey) {
                    if (authBackStack.isNotEmpty()) {
                        authBackStack[authBackStack.size - 1] = key
                    } else {
                        authBackStack.add(key)
                    }
                }

                override fun replaceAll(key: NavKey) {
                    authBackStack.clear()
                    authBackStack.add(key)
                }
            }
        }

        val authEntryProvider = remember(featureEntries, authNavigator) {
            entryProvider {
                for (entry in featureEntries) {
                    with(entry) {
                        install(authNavigator)
                    }
                }
            }
        }

        BackHandler(enabled = authBackStack.size > 1) {
            authBackStack.removeAt(authBackStack.size - 1)
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                if (!ApiConfig.USE_LIVE_API) Surface(
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.app_demo_signin_prompt),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = {
                                    fakeSessionStore.signInAsDemoWorker()
                                    navigator.switchTab(TopLevelTab.HOME)
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.app_demo_worker))
                            }
                            Button(
                                onClick = {
                                    fakeSessionStore.signInAsDemoForeman()
                                    navigator.switchTab(TopLevelTab.HOME)
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.app_demo_foreman))
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                NavDisplay(
                    backStack = authBackStack,
                    entryProvider = authEntryProvider,
                )
            }
        }
    } else {
        // Authenticated 5-tab main UI
        val entryProvider = remember(featureEntries, navigator) {
            entryProvider {
                for (entry in featureEntries) {
                    with(entry) {
                        install(navigator)
                    }
                }
            }
        }

        val isTopLevel = currentStack.size == 1 &&
            currentStack.firstOrNull()?.let { it::class in TOP_LEVEL_KEYS } == true

        BackHandler(enabled = currentStack.size > 1 || currentTab != TopLevelTab.HOME) {
            if (!navigator.pop()) {
                navigator.switchTab(TopLevelTab.HOME)
            }
        }

        val activeTabStack = backStack.stackFor(currentTab)

        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                if (isTopLevel) {
                    AppBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { tab -> navigator.switchTab(tab) },
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                NavDisplay(
                    backStack = activeTabStack,
                    entryProvider = entryProvider,
                )
            }
        }
    }
}

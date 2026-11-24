package com.techtsunagu.wakatta

import android.util.Log
import android.widget.Toast
import com.techtsunagu.wakatta.core.common.CrashGuard
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.deeplink.parseAppLink
import com.techtsunagu.wakatta.core.deeplink.toDestination
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme
import com.techtsunagu.wakatta.core.media.PhotoCaptureBroker
import com.techtsunagu.wakatta.core.media.PhotoCaptureHost
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SettingsStore
import com.techtsunagu.wakatta.core.navigation.ChatKey
import com.techtsunagu.wakatta.core.navigation.MachineKey
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.core.sync.OutboxWorker
import dagger.hilt.android.AndroidEntryPoint
import java.net.URI
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var settingsStore: SettingsStore

    @Inject lateinit var languageManager: LanguageManager

    @Inject lateinit var photoBroker: PhotoCaptureBroker

    @Inject lateinit var sessionStore: SessionStore

    @Inject lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // A language change recreates this activity, so the API language follows what the screen shows.
        val shown = resources.configuration.locales[0].language
        lifecycleScope.launch { languageManager.setLanguage(if (shown in LANGUAGES) shown else "en") }

        // Each sign-in and app start: send what this person queued without a network.
        lifecycleScope.launch {
            sessionStore.session.map { it?.user?.id }.distinctUntilChanged().filterNotNull().collect { OutboxWorker.schedule(this@MainActivity) }
        }

        if (savedInstanceState == null) {
            openFromNotification(intent)
            openFromAppLink(intent)
        }

        // A screen hit an unexpected error (release build): say so briefly and keep the app running.
        lifecycleScope.launch {
            CrashGuard.errors.collect {
                Log.e("Wakatta", "unexpected error", it)
                Toast.makeText(this@MainActivity, R.string.app_unexpected_error, Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            val darkSetting by settingsStore.isDarkMode.collectAsStateWithLifecycle()
            val largeText by settingsStore.isLargeText.collectAsStateWithLifecycle()
            val dark = darkSetting ?: isSystemInDarkTheme()
            val density = LocalDensity.current
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).run {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            CompositionLocalProvider(
                LocalDensity provides if (largeText) Density(density.density, density.fontScale * LARGE_TEXT_SCALE) else density,
            ) {
                WakattaTheme(darkTheme = dark) {
                    AppNavHost()
                    PhotoCaptureHost(photoBroker)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openFromNotification(intent)
        openFromAppLink(intent)
    }

    /** An App Link (a Machine sticker, a shared link) opens its screen once someone is signed in; anything else is ignored. */
    private fun openFromAppLink(intent: Intent) {
        val link = intent.dataString?.let { parseAppLink(it, APP_LINK_HOST) } ?: return
        val destination = link.toDestination()
        lifecycleScope.launch {
            sessionStore.session.first { it != null && !it.mustChangePin }
            navigator.switchTab(destination.tab)
            navigator.push(destination.target)
        }
    }

    /** A push carries only ids (Android puts them in the extras of the launch intent); they open the Chat or the Machine. */
    private fun openFromNotification(intent: Intent) {
        val key = notificationKey(intent.getStringExtra("threadId"), intent.getStringExtra("machineId")) ?: return
        lifecycleScope.launch {
            sessionStore.session.first { it != null && !it.mustChangePin }
            navigator.push(key)
        }
    }

    private companion object {
        val LANGUAGES = setOf("ja", "en")
        const val LARGE_TEXT_SCALE = 1.3f

        // The manifest's App Links host; the API host serves assetlinks.json (XREQ-026)
        val APP_LINK_HOST: String = URI(ApiConfig.BASE_URL).host
    }
}

/** Ids come from outside the app: only numbers are taken. */
internal fun notificationKey(threadId: String?, machineId: String?): NavKey? = when {
    threadId != null && threadId.isServerId() -> ChatKey(threadId)
    machineId != null && machineId.isServerId() -> MachineKey(machineId)
    else -> null
}

private fun String.isServerId() = length in 1..19 && all { it in '0'..'9' }

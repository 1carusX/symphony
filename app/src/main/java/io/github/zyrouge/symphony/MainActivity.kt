package io.github.zyrouge.symphony

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.github.zyrouge.symphony.services.locales.localeTag
import io.github.zyrouge.symphony.ui.view.BaseView
import io.github.zyrouge.symphony.utils.Logger
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var gSymphony: Symphony? = null

    override fun attachBaseContext(newBase: Context) {
        val language = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("language", null)
        if (Build.VERSION.SDK_INT >= 33 || language == null) {
            super.attachBaseContext(newBase)
            return
        }
        // Below API 33 the language is not kept by the system, so apply it on every start.
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(localeTag(language)))
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ignition: ActivityIgnition by viewModels()
        if (savedInstanceState == null) {
            installSplashScreen().apply {
                setKeepOnScreenCondition { !ignition.ready.value }
            }
        }

        Thread.setDefaultUncaughtExceptionHandler { _, err ->
            Logger.error("MainActivity", "uncaught exception", err)
            ErrorActivity.start(this, err)
            finish()
        }

        val symphony: Symphony by viewModels()
        symphony.permission.handle(this)
        gSymphony = symphony
        symphony.emitActivityReady()
        attachHandlers()

        enableEdgeToEdge()
        setContent {
            LaunchedEffect(LocalContext.current) {
                ignition.emitReady()
            }
            BaseView(symphony = symphony, activity = this)
        }
    }

    override fun onPause() {
        super.onPause()
        gSymphony?.emitActivityPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        gSymphony?.emitActivityDestroy()
    }

    private fun attachHandlers() {
        gSymphony?.closeApp = {
            finish()
        }
    }
}

package com.techtsunagu.wakatta

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class TabLabelsTest {

    @Test
    fun testTabLabelsInThreeLanguages() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        fun getStringsForLocale(langTag: String): List<String> {
            val config = Configuration(context.resources.configuration)
            config.setLocale(Locale.forLanguageTag(langTag))
            val localizedContext = context.createConfigurationContext(config)
            return listOf(
                localizedContext.getString(R.string.app_tab_home),
                localizedContext.getString(R.string.app_tab_chats),
                localizedContext.getString(R.string.app_tab_board),
                localizedContext.getString(R.string.app_tab_machines),
                localizedContext.getString(R.string.app_tab_me),
            )
        }

        val jaStrings = getStringsForLocale("ja")
        assertEquals(listOf("ホーム", "チャット", "ボード", "機械", "自分"), jaStrings)

        val enStrings = getStringsForLocale("en")
        assertEquals(listOf("Home", "Chats", "Board", "Machines", "Me"), enStrings)

        // Bengali is not shipped (ADR 0016): a bn device reads English.
        assertEquals(enStrings, getStringsForLocale("bn"))
    }
}

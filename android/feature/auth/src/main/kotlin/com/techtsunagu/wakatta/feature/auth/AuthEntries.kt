package com.techtsunagu.wakatta.feature.auth

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.ChangePinKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.SignInKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        entry<SignInKey> {
            // The session gate opens by itself once the repository has saved the session.
            SignInScreen(
                onNavigateToChangePin = { navigator.push(ChangePinKey) },
            )
        }
        entry<ChangePinKey> {
            ChangePinScreen(
                onPinChanged = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @IntoSet
    abstract fun bindAuthEntries(entries: AuthEntries): FeatureEntries
}

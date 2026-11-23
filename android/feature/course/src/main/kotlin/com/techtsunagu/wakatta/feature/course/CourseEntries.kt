package com.techtsunagu.wakatta.feature.course

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.CourseKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.Navigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: course
        entry<CourseKey> {
            CourseScreen(
                onBackClick = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CourseModule {
    @Binds
    @IntoSet
    abstract fun bindCourseEntries(entries: CourseEntries): FeatureEntries
}

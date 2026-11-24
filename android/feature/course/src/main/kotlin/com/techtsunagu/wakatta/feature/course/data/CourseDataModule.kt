package com.techtsunagu.wakatta.feature.course.data

import com.techtsunagu.wakatta.feature.course.domain.CourseRepository
import com.techtsunagu.wakatta.feature.course.domain.TonePlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CourseDataModule {

    @Binds
    @Singleton
    abstract fun bindCourseRepository(impl: BundledCourseRepository): CourseRepository

    @Binds
    @Singleton
    abstract fun bindCourseProgressStore(impl: DataStoreCourseProgressStore): CourseProgressStore

    @Binds
    @Singleton
    abstract fun bindTonePlayer(impl: AudioTrackTonePlayer): TonePlayer
}

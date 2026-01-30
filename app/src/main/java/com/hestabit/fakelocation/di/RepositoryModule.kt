package com.hestabit.fakelocation.di

import com.hestabit.fakelocation.data.repository.AuthRepository
import com.hestabit.fakelocation.data.repository.AuthRepositoryImpl
import com.hestabit.fakelocation.data.repository.MockLocationRepository
import com.hestabit.fakelocation.data.repository.MockLocationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMockLocationRepository(
        mockLocationRepositoryImpl: MockLocationRepositoryImpl
    ): MockLocationRepository

    @Binds
    @Singleton
    abstract fun bindSavedLocationRepository(
        savedLocationRepositoryImpl: com.hestabit.fakelocation.data.repository.SavedLocationRepositoryImpl
    ): com.hestabit.fakelocation.data.repository.SavedLocationRepository
}

package org.intelehealth.app.ui.rosterquestionnaire.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.intelehealth.app.ui.rosterquestionnaire.repository.RosterRepository
import org.intelehealth.app.ui.rosterquestionnaire.repository.RosterRepositoryImp

@Module
@InstallIn(SingletonComponent::class)
abstract class RosterModule {

    @Binds
    abstract fun bindRosterRepository(
        rosterRepositoryImp: RosterRepositoryImp
    ): RosterRepository
}
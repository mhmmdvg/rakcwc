package com.rakcwc.di

import android.content.Context
import androidx.room.Room
import com.rakcwc.data.remote.local.dao.CatalogDao
import com.rakcwc.data.remote.local.dao.ProductsDao
import com.rakcwc.data.remote.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCatalogDao(appDatabase: AppDatabase): CatalogDao {
        return appDatabase.catalogDao()
    }

    @Provides
    @Singleton
    fun provideProductDao(appDatabase: AppDatabase): ProductsDao {
        return appDatabase.productDao()
    }
}
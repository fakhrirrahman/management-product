package com.example.managementproduct.data

import android.content.Context
import androidx.room.Room
import com.example.managementproduct.data.local.AppDatabase
import com.example.managementproduct.data.repository.KiosRepository

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val kiosRepository: KiosRepository
}

/**
 * Implementation for the Dependency Injection container at the application level.
 *
 * Variables are initialized lazily and the same instance is shared across the whole app.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "management_product.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    override val kiosRepository: KiosRepository by lazy {
        KiosRepository(database)
    }
}


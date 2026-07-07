package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ContactDao
import com.example.data.dao.LocationDao
import com.example.data.dao.NoteDao
import com.example.data.dao.SettingDao
import com.example.data.model.ContactEntity
import com.example.data.model.LocationEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SettingEntity

@Database(
    entities = [ContactEntity::class, NoteEntity::class, LocationEntity::class, SettingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun noteDao(): NoteDao
    abstract fun locationDao(): LocationDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dandali_phone_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

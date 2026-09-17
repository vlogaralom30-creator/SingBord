package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [UserWord::class, UserWordBigram::class], version = 2, exportSchema = false)
abstract class SingBordDatabase : RoomDatabase() {
    abstract fun userWordDao(): UserWordDao
    abstract fun userWordBigramDao(): UserWordBigramDao

    companion object {
        @Volatile
        private var INSTANCE: SingBordDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_bigrams` (" +
                        "`prevWord` TEXT NOT NULL, " +
                        "`nextWord` TEXT NOT NULL, " +
                        "`frequency` INTEGER NOT NULL, " +
                        "`lastUsed` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`prevWord`, `nextWord`))"
                )
            }
        }

        fun getInstance(context: Context): SingBordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SingBordDatabase::class.java,
                    "singbord_user_dict.db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

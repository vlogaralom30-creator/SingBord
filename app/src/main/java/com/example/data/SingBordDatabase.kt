package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserWord::class], version = 1, exportSchema = false)
abstract class SingBordDatabase : RoomDatabase() {
    abstract fun userWordDao(): UserWordDao

    companion object {
        @Volatile
        private var INSTANCE: SingBordDatabase? = null

        fun getInstance(context: Context): SingBordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SingBordDatabase::class.java,
                    "singbord_user_dict.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

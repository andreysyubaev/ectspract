package com.bignerdranch.android.z2.ui.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MyData::class], version = 2)
abstract class MyDataBase : RoomDatabase() {
    abstract fun getDbDao(): MyDataBaseDao

    companion object {
        @Volatile
        private var INSTANCE: MyDataBase? = null

        fun getInstance(context: Context): MyDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDataBase::class.java,
                    "my_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Пример добавления нового столбца
                db.execSQL("ALTER TABLE my_data_table ADD COLUMN new_column TEXT")
            }
        }
    }
}

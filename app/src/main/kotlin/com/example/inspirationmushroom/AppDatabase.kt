package com.example.inspirationmushroom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

// Converters for Room to handle Date objects
class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(entities = [Record::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 添加新列，并允许为空
                database.execSQL("ALTER TABLE records ADD COLUMN aiAnalysis TEXT")
                database.execSQL("ALTER TABLE records ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING_ANALYSIS'")

                // 2. (可选) 可以创建一个临时表来重命名和删除旧列，但更简单的方式是直接在代码层面放弃旧列
                // 为了简化，我们只添加新列，旧的 efficiency 和 mood 列将不再被实体类映射，相当于被废弃。
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "note30_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
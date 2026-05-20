package io.moviles.IPN_Tycoon.android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.moviles.IPN_Tycoon.data.local.daos.EscuelaDao
import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity

@Database(entities = [EscuelaEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun escuelaDao(): EscuelaDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE escuelas ADD COLUMN reputacion INTEGER NOT NULL DEFAULT 50")
            }
        }
    }
}

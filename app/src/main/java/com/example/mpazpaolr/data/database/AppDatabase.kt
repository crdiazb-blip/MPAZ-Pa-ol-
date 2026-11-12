package com.example.mpazpaolr.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mpazpaolr.data.local.dao.PanolDao
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.local.entity.SolicitudEntity
import com.example.mpazpaolr.data.local.entity.SolicitudItemEntity

@Database(
    entities = [EquipoEntity::class, SolicitudEntity::class, SolicitudItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun panolDao(): PanolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_panol.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

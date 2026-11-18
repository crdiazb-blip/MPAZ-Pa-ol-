package com.example.mpazpaolr.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mpazpaolr.data.local.dao.AuthDao
import com.example.mpazpaolr.data.local.dao.PanolDao
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.local.entity.SesionEntity
import com.example.mpazpaolr.data.local.entity.SolicitudEntity
import com.example.mpazpaolr.data.local.entity.SolicitudItemEntity
import com.example.mpazpaolr.data.local.entity.UsuarioLocalEntity

@Database(
    entities = [
        EquipoEntity::class,
        SolicitudEntity::class,
        SolicitudItemEntity::class,
        UsuarioLocalEntity::class,
        SesionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun panolDao(): PanolDao

    abstract fun authDao(): AuthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_panol.db"
                )
                    // Los datos locales son una caché del servidor: si cambia el esquema, se vuelve a sincronizar.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

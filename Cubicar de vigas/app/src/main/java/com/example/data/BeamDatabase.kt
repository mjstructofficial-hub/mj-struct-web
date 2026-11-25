package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [BeamItemEntity::class, SavedProfileEntity::class],
  version = 4,
  exportSchema = false
)
abstract class BeamDatabase : RoomDatabase() {
  abstract fun beamDao(): BeamDao

  companion object {
    private val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE beam_items ADD COLUMN topFlangeLengthMm REAL")
        db.execSQL("ALTER TABLE beam_items ADD COLUMN webLengthMm REAL")
        db.execSQL("ALTER TABLE beam_items ADD COLUMN bottomFlangeLengthMm REAL")
      }
    }

    @Volatile
    private var INSTANCE: BeamDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): BeamDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BeamDatabase::class.java,
          "beam_cubicador_db"
        )
          .addCallback(BeamDatabaseCallback(scope))
          .addMigrations(MIGRATION_3_4)
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class BeamDatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            val dao = database.beamDao()
            // Prepopulate initial default custom profile as in the web application
            dao.insertSavedProfile(
              SavedProfileEntity(
                name = "Viga Soldada 400x180",
                h = 400.0,
                b = 180.0,
                tf = 14.0,
                tw = 8.0,
                r = 0.0
              )
            )
          }
        }
      }
    }
  }
}

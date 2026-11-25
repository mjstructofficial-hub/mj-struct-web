package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BeamDao {
  @Query("SELECT * FROM beam_items ORDER BY id ASC")
  fun getAllBeamItems(): Flow<List<BeamItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBeamItem(item: BeamItemEntity): Long

  @Delete
  suspend fun deleteBeamItem(item: BeamItemEntity)

  @Query("DELETE FROM beam_items")
  suspend fun clearAllBeamItems()

  @Query("SELECT * FROM saved_profiles ORDER BY id ASC")
  fun getAllSavedProfiles(): Flow<List<SavedProfileEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSavedProfile(profile: SavedProfileEntity): Long

  @Delete
  suspend fun deleteSavedProfile(profile: SavedProfileEntity)

  @Query("SELECT COUNT(*) FROM saved_profiles")
  suspend fun getSavedProfilesCount(): Int
}

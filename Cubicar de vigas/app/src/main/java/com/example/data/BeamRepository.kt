package com.example.data

import kotlinx.coroutines.flow.Flow

class BeamRepository(private val beamDao: BeamDao) {
  val allBeamItems: Flow<List<BeamItemEntity>> = beamDao.getAllBeamItems()
  val allSavedProfiles: Flow<List<SavedProfileEntity>> = beamDao.getAllSavedProfiles()

  suspend fun insertBeamItem(item: BeamItemEntity): Long {
    return beamDao.insertBeamItem(item)
  }

  suspend fun deleteBeamItem(item: BeamItemEntity) {
    beamDao.deleteBeamItem(item)
  }

  suspend fun clearAllBeamItems() {
    beamDao.clearAllBeamItems()
  }

  suspend fun insertSavedProfile(profile: SavedProfileEntity): Long {
    return beamDao.insertSavedProfile(profile)
  }

  suspend fun deleteSavedProfile(profile: SavedProfileEntity) {
    beamDao.deleteSavedProfile(profile)
  }
}

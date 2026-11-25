package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.format

@Entity(tableName = "beam_items")
data class BeamItemEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val type: String, // "Comercial", "Armada", "Columna Cajón", "Correa G / Canal", "Placa"
  val category: String = "Viga Principal", // "Viga Principal", "Viga Secundaria", "Columna", "Correa / Canal", "Placa"
  val name: String,
  val h: Double,
  val hw: Double,
  val b: Double,
  val tf: Double,
  val tw: Double,
  val r: Double = 0.0,
  val dims: String,
  val lengthMm: Double,
  val quantity: Int,
  val unitWeightKgM: Double,
  val totalWeightKg: Double, // Net total weight
  val totalWeightTon: Double, // Net weight in tons
  val grossWeightKg: Double = totalWeightKg,
  val notchDeductionKg: Double = 0.0,
  val hasDestaje: Boolean = false,
  val destajeLongMm: Double = 0.0,
  val destajeAltMm: Double = 0.0,
  val destajeTipo: String = "",
  val destajeDetails: String = "",
  val topFlangeLengthMm: Double? = null,
  val webLengthMm: Double? = null,
  val bottomFlangeLengthMm: Double? = null,
  val timestamp: Long = System.currentTimeMillis()
) {
  val referenceLengthMm: Double
    get() = listOfNotNull(topFlangeLengthMm, webLengthMm, bottomFlangeLengthMm)
      .maxOrNull() ?: lengthMm

  val destajeInfo: String
    get() = if (hasDestaje) {
      if (destajeDetails.isNotBlank()) destajeDetails
      else {
        val tipoStr = if (destajeTipo.isNotBlank()) " ($destajeTipo)" else ""
        "${destajeLongMm.format()}x${destajeAltMm.format()} mm$tipoStr"
      }
    } else {
      "-"
    }
}

@Entity(tableName = "saved_profiles")
data class SavedProfileEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val name: String,
  val h: Double,
  val b: Double,
  val tf: Double,
  val tw: Double,
  val r: Double,
  val profileType: String = "ARMADA"
) {
  val hw: Double
    get() = kotlin.math.max(0.0, h - 2.0 * tf)
}



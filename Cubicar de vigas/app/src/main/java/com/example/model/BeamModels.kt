package com.example.model

import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow

data class CommercialProfile(
  val name: String,
  val h: Double,
  val b: Double,
  val tf: Double,
  val tw: Double,
  val weightKgPerM: Double
) {
  val hw: Double
    get() = max(0.0, h - 2.0 * tf)

  val dimsFormatted: String
    get() = "${h.format()} x ${b.format()} x ${tf.format()} x ${tw.format()}"
}

object BeamCatalog {
  val profiles: List<CommercialProfile> = listOf(
    CommercialProfile("IPE 80", 80.0, 46.0, 5.2, 3.8, 6.0),
    CommercialProfile("IPE 100", 100.0, 55.0, 5.7, 4.1, 8.1),
    CommercialProfile("IPE 120", 120.0, 64.0, 6.3, 4.4, 10.4),
    CommercialProfile("IPE 140", 140.0, 73.0, 6.9, 4.7, 12.9),
    CommercialProfile("IPE 160", 160.0, 82.0, 7.4, 5.0, 15.8),
    CommercialProfile("IPE 180", 180.0, 91.0, 8.0, 5.3, 18.8),
    CommercialProfile("IPE 200", 200.0, 100.0, 8.5, 5.6, 22.4),
    CommercialProfile("IPE 220", 220.0, 110.0, 9.2, 5.9, 26.2),
    CommercialProfile("IPE 240", 240.0, 120.0, 9.8, 6.2, 30.7),
    CommercialProfile("IPE 270", 270.0, 135.0, 10.2, 6.6, 36.1),
    CommercialProfile("IPE 300", 300.0, 150.0, 10.7, 7.1, 42.2),
    CommercialProfile("IPE 330", 330.0, 160.0, 11.5, 7.5, 49.1),
    CommercialProfile("IPE 360", 360.0, 170.0, 12.7, 8.0, 57.1),
    CommercialProfile("IPE 400", 400.0, 180.0, 13.5, 8.6, 66.3),
    CommercialProfile("IPE 450", 450.0, 190.0, 14.6, 9.4, 77.6),
    CommercialProfile("IPE 500", 500.0, 200.0, 16.0, 10.2, 90.7),
    CommercialProfile("IPE 550", 550.0, 210.0, 17.2, 11.1, 106.0),
    CommercialProfile("IPE 600", 600.0, 220.0, 19.0, 12.0, 122.0),

    CommercialProfile("HEA 100", 96.0, 100.0, 8.0, 5.0, 16.7),
    CommercialProfile("HEA 120", 114.0, 120.0, 8.0, 5.0, 19.9),
    CommercialProfile("HEA 140", 133.0, 140.0, 8.5, 5.5, 24.7),
    CommercialProfile("HEA 160", 152.0, 160.0, 9.0, 6.0, 30.4),
    CommercialProfile("HEA 180", 171.0, 180.0, 9.5, 6.0, 35.5),
    CommercialProfile("HEA 200", 190.0, 200.0, 10.0, 6.5, 42.3),
    CommercialProfile("HEA 220", 210.0, 220.0, 11.0, 7.0, 50.5),
    CommercialProfile("HEA 240", 230.0, 240.0, 12.0, 7.5, 60.3),
    CommercialProfile("HEA 260", 250.0, 260.0, 12.5, 7.5, 68.2),
    CommercialProfile("HEA 280", 270.0, 280.0, 13.0, 8.0, 76.4),
    CommercialProfile("HEA 300", 290.0, 300.0, 14.0, 8.5, 88.3),

    CommercialProfile("HEB 100", 100.0, 100.0, 10.0, 6.0, 20.4),
    CommercialProfile("HEB 120", 120.0, 120.0, 11.0, 6.5, 26.7),
    CommercialProfile("HEB 140", 140.0, 140.0, 12.0, 7.0, 33.7),
    CommercialProfile("HEB 160", 160.0, 160.0, 13.0, 8.0, 42.6),
    CommercialProfile("HEB 180", 180.0, 180.0, 14.0, 8.5, 51.2),
    CommercialProfile("HEB 200", 200.0, 200.0, 15.0, 9.0, 61.3),
    CommercialProfile("HEB 220", 220.0, 220.0, 16.0, 9.5, 71.5),
    CommercialProfile("HEB 240", 240.0, 240.0, 17.0, 10.0, 83.2),
    CommercialProfile("HEB 260", 260.0, 260.0, 17.5, 10.0, 93.0),
    CommercialProfile("HEB 280", 280.0, 280.0, 18.0, 10.5, 103.0),
    CommercialProfile("HEB 300", 300.0, 300.0, 19.0, 11.0, 117.0),

    CommercialProfile("IPN 100", 100.0, 50.0, 6.8, 4.5, 8.34),
    CommercialProfile("IPN 120", 120.0, 58.0, 7.7, 5.1, 11.1),
    CommercialProfile("IPN 140", 140.0, 66.0, 8.6, 5.7, 14.3),
    CommercialProfile("IPN 160", 160.0, 74.0, 9.5, 6.3, 17.9),
    CommercialProfile("IPN 180", 180.0, 82.0, 10.4, 6.9, 21.9),
    CommercialProfile("IPN 200", 200.0, 90.0, 11.3, 7.5, 26.2),

    CommercialProfile("W8x10", 200.4, 100.1, 5.2, 4.3, 14.9),
    CommercialProfile("W8x18", 206.8, 133.4, 8.4, 5.8, 26.8),
    CommercialProfile("W10x12", 251.5, 100.6, 5.3, 4.8, 17.9),
    CommercialProfile("W10x22", 258.3, 146.1, 9.1, 6.6, 32.7),
    CommercialProfile("W12x14", 302.5, 100.8, 5.7, 5.1, 20.8),
    CommercialProfile("W12x26", 310.4, 164.8, 9.7, 5.8, 38.7),
    CommercialProfile("W14x22", 348.7, 127.0, 8.5, 5.8, 32.7),
    CommercialProfile("W16x26", 403.6, 139.7, 8.8, 6.4, 38.7),

    // Tubulares Estructurales HSS (ASTM A500)
    CommercialProfile("HSS 100x100x3.0 mm", 100.0, 100.0, 3.0, 3.0, 8.96),
    CommercialProfile("HSS 100x100x4.0 mm", 100.0, 100.0, 4.0, 4.0, 11.7),
    CommercialProfile("HSS 150x150x4.0 mm", 150.0, 150.0, 4.0, 4.0, 18.0),
    CommercialProfile("HSS 150x150x6.0 mm", 150.0, 150.0, 6.0, 6.0, 26.4),
    CommercialProfile("HSS 200x100x5.0 mm", 200.0, 100.0, 5.0, 5.0, 22.3),
    CommercialProfile("HSS 200x200x6.0 mm", 200.0, 200.0, 6.0, 6.0, 35.8),
    CommercialProfile("HSS 200x200x8.0 mm", 200.0, 200.0, 8.0, 8.0, 46.9),
    CommercialProfile("HSS 250x250x6.0 mm", 250.0, 250.0, 6.0, 6.0, 45.2),
    CommercialProfile("HSS 250x250x8.0 mm", 250.0, 250.0, 8.0, 8.0, 59.5),
    CommercialProfile("HSS 300x300x6.0 mm", 300.0, 300.0, 6.0, 6.0, 55.39),
    CommercialProfile("HSS 300x300x8.0 mm", 300.0, 300.0, 8.0, 8.0, 73.35),
    CommercialProfile("HSS 300x300x9.5 mm", 300.0, 300.0, 9.5, 9.5, 86.66),
    CommercialProfile("HSS 300x300x10.0 mm", 300.0, 300.0, 10.0, 10.0, 91.06),
    CommercialProfile("HSS 300x300x12.0 mm", 300.0, 300.0, 12.0, 12.0, 108.52),
    CommercialProfile("HSS 300x300x12.7 mm", 300.0, 300.0, 12.7, 12.7, 114.57)
  )

  val coldFormedPresets: List<ColdFormedPreset> = listOf(
    ColdFormedPreset("Correa G 100x50x15x2.0 mm", 100.0, 50.0, 15.0, 2.0),
    ColdFormedPreset("Correa G 125x50x15x2.0 mm", 125.0, 50.0, 15.0, 2.0),
    ColdFormedPreset("Correa G 150x50x17x2.0 mm", 150.0, 50.0, 17.0, 2.0),
    ColdFormedPreset("Correa G 200x65x20x2.5 mm", 200.0, 65.0, 20.0, 2.5),
    ColdFormedPreset("Canal U 100x50x3.0 mm", 100.0, 50.0, 0.0, 3.0),
    ColdFormedPreset("Canal U 150x50x4.0 mm", 150.0, 50.0, 0.0, 4.0)
  )

  fun calculateArmadaHw(h: Double, tf: Double): Double {
    return max(0.0, h - 2.0 * tf)
  }

  fun calculateArmadaWeightPerM(
    h: Double,
    b: Double,
    tf: Double,
    tw: Double,
    r: Double
  ): Double {
    val hw = calculateArmadaHw(h, tf)
    val areaPlatesMm2 = 2.0 * (b * tf) + (hw * tw)
    val areaFilletsMm2 = if (r > 0.0) (4.0 - PI) * r.pow(2.0) else 0.0
    val totalAreaMm2 = areaPlatesMm2 + areaFilletsMm2
    // Density of structural steel: 7850 kg/m^3 -> 1 mm^2 = 1e-6 m^2
    return (totalAreaMm2 * 1e-6) * 7850.0
  }

  // Box Column (2 Canales U)
  // Channel perimeter P1 = (H + 2*B - 2*t)
  // Linear weight (kg/m) = 2 * [ P1 * t * 0.00785 kg/m ]
  fun calculateBoxColumnWeightPerM(h: Double, b: Double, t: Double): Double {
    val p1 = max(0.0, h + 2.0 * b - 2.0 * t)
    return 2.0 * (p1 * t * 0.00785)
  }

  // Cold-formed purlin / channel
  // Developed strip width (mm) = H + 2*B + 2*C
  // Linear weight (kg/m) = Developed strip width * t * 0.00785 kg/m
  fun calculateColdFormedWeightPerM(h: Double, b: Double, c: Double, t: Double): Double {
    val developedStripWidthMm = max(0.0, h + 2.0 * b + 2.0 * c)
    return developedStripWidthMm * t * 0.00785
  }

  // Flat steel plate weight (kg)
  fun calculatePlateWeight(thicknessMm: Double, widthMm: Double, lengthMm: Double): Double {
    val volumeM3 = thicknessMm * widthMm * lengthMm * 1e-9
    return volumeM3 * 7850.0
  }

  /** Weight of a built-up I-beam made from separate top flange, web and bottom flange plates. */
  fun calculateSpecialBeamWeightKg(
    h: Double,
    b: Double,
    tf: Double,
    tw: Double,
    topFlangeLengthMm: Double,
    webLengthMm: Double,
    bottomFlangeLengthMm: Double
  ): Double {
    val hw = calculateArmadaHw(h, tf)
    return calculatePlateWeight(tf, b, topFlangeLengthMm) +
      calculatePlateWeight(tw, hw, webLengthMm) +
      calculatePlateWeight(tf, b, bottomFlangeLengthMm)
  }

  // Flange Cope / Notch deduction
  // b_notch = (b - tw) / 2
  // Volume removed per notch = b_notch * tf * Ld
  // Net weight deduction (kg) = (Volume removed * 7850 kg/m³ * 1e-9) * notch_count
  fun calculateNotchDeductionKg(
    b: Double,
    tw: Double,
    tf: Double,
    ldMm: Double,
    notchType: String,
    quantity: Int
  ): Double {
    val bNotch = max(0.0, (b - tw) / 2.0)
    val volPerNotchMm3 = bNotch * tf * ldMm
    val notchCount = when (notchType) {
      "1 Extremo (Superior)" -> 2
      "2 Extremos (Superior)" -> 4
      "1 Extremo (Superior e Inferior)" -> 4
      "2 Extremos (Superior e Inferior)" -> 8
      else -> 4
    }
    val volTotalM3 = (volPerNotchMm3 * notchCount * quantity) * 1e-9
    return volTotalM3 * 7850.0
  }
}

data class ColdFormedPreset(
  val label: String,
  val h: Double,
  val b: Double,
  val c: Double,
  val t: Double
)

object DestajeCatalog {
  val types: List<String> = listOf(
    "2 Extremos (Superior)",
    "2 Extremos (Superior e Inferior)",
    "1 Extremo (Superior)",
    "1 Extremo (Superior e Inferior)"
  )
}

fun Double.format(decimals: Int = 1): String {
  return if (this % 1.0 == 0.0) {
    this.toInt().toString()
  } else {
    String.format(java.util.Locale.US, "%.${decimals}f", this)
  }
}

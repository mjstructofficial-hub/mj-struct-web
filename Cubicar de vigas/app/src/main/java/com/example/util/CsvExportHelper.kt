package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.BeamItemEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.Locale

object CsvExportHelper {

  fun cleanDestajeInstruction(item: BeamItemEntity): String {
    if (!item.hasDestaje) return "-"
    val dLong = item.destajeLongMm.toInt()
    val dAlt = item.destajeAltMm.toInt()
    if (dLong <= 0 && dAlt <= 0) return "-"

    val locationShort = when (item.destajeTipo.trim()) {
      "1 Extremo (Superior)" -> "1 Sup."
      "2 Extremos (Superior)" -> "2 Sup."
      "1 Extremo (Superior e Inferior)" -> "1 Sup/Inf."
      "2 Extremos (Superior e Inferior)" -> "2 Sup/Inf."
      else -> {
        val raw = item.destajeTipo.trim()
        if (raw.isNotBlank()) raw else "Cortes"
      }
    }

    return if (dLong > 0 && dAlt > 0) {
      "${dLong}x${dAlt} mm ($locationShort)"
    } else if (dLong > 0) {
      "Ld=${dLong} mm ($locationShort)"
    } else {
      "Hd=${dAlt} mm ($locationShort)"
    }
  }

  private fun isPlate(category: String, type: String): Boolean {
    val cat = category.lowercase()
    val typ = type.lowercase()
    return cat.contains("placa") || typ.contains("placa")
  }

  private fun isPlateOrColdFormed(category: String, type: String): Boolean {
    val cat = category.lowercase()
    val typ = type.lowercase()
    return cat.contains("placa") || cat.contains("correa") || cat.contains("canal") ||
      typ.contains("placa") || typ.contains("correa") || typ.contains("canal")
  }

  private fun escapeCsv(value: String): String {
    return if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
      "\"" + value.replace("\"", "\"\"") + "\""
    } else {
      value
    }
  }

  fun exportAndShareCsv(context: Context, items: List<BeamItemEntity>, isDetailed: Boolean): Boolean {
    if (items.isEmpty()) return false

    val fileName = if (isDetailed) "Cubicacion_Montaje_Detallado.csv" else "Cubicacion_Montaje_Compacto.csv"
    val exportDir = File(context.cacheDir, "exports")
    if (!exportDir.exists()) {
      exportDir.mkdirs()
    }
    val file = File(exportDir, fileName)

    val csvContent = if (isDetailed) {
      buildDetailedCsv(items)
    } else {
      buildCompactCsv(items)
    }

    FileOutputStream(file).use { fos ->
      // Standard UTF-8 BOM bytes (\uFEFF)
      fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
      OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
        writer.write(csvContent)
        writer.flush()
      }
      fos.flush()
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/csv"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, "Cubicación de Estructuras de Acero y Montaje")
      putExtra(
        Intent.EXTRA_TEXT,
        "Reporte de cubicación de acero para Excel (.csv) con codificación UTF-8 y delimitador punto y coma (;)."
      )
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Grant read permission to all candidate apps (WhatsApp, Gmail, Drive, etc.)
    val resInfoList = context.packageManager.queryIntentActivities(
      shareIntent,
      android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
    )
    for (resolveInfo in resInfoList) {
      val targetPackage = resolveInfo.activityInfo.packageName
      context.grantUriPermission(targetPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Compartir archivo de cubicación"))
    return true
  }

  private fun buildCompactCsv(items: List<BeamItemEntity>): String {
    val sb = StringBuilder()
    // Excel column separator directive
    sb.append("sep=;\n")

    // Compact Headers (17 columns)
    // Row 1 (Titles)
    sb.append("Item;Categoria;Tipo;Marca;Dimensiones;Destaje;Longitud;L patin superior;L alma;L patin inferior;Cantidad;Long. Total;Peso Unitario;Peso Bruto;Reduccion;Peso Neto Total;Peso Total\n")
    // Row 2 (Units)
    sb.append("-;-;-;-;-;Ld x Hd;mm;mm;mm;mm;u;m;kg/m;kg;kg;kg;t\n")

    var sumQty = 0
    var sumLongM = 0.0
    var sumGrossKg = 0.0
    var sumDeductionKg = 0.0
    var sumNetKg = 0.0

    items.forEachIndexed { idx, item ->
      val isPl = isPlate(item.category, item.type)
      val longTotalM = if (isPl) 0.0 else (item.referenceLengthMm * item.quantity) / 1000.0

      sumQty += item.quantity
      sumLongM += longTotalM
      sumGrossKg += item.grossWeightKg
      sumDeductionKg += item.notchDeductionKg
      sumNetKg += item.totalWeightKg

      val destajeText = cleanDestajeInstruction(item)
      val longTotalText = if (isPl) "-" else String.format(Locale.US, "%.2f", longTotalM)

      sb.append("${idx + 1};")
      sb.append("${escapeCsv(item.category)};")
      sb.append("${escapeCsv(item.type)};")
      sb.append("${escapeCsv(item.name)};")
      sb.append("${escapeCsv(item.dims)};")
      sb.append("${escapeCsv(destajeText)};")
      sb.append("${String.format(Locale.US, "%.2f", item.referenceLengthMm)};")
      sb.append("${item.topFlangeLengthMm?.let { String.format(Locale.US, "%.2f", it) } ?: ""};")
      sb.append("${item.webLengthMm?.let { String.format(Locale.US, "%.2f", it) } ?: ""};")
      sb.append("${item.bottomFlangeLengthMm?.let { String.format(Locale.US, "%.2f", it) } ?: ""};")
      sb.append("${item.quantity};")
      sb.append("${longTotalText};")
      sb.append("${String.format(Locale.US, "%.2f", item.unitWeightKgM)};")
      sb.append("${String.format(Locale.US, "%.2f", item.grossWeightKg)};")
      sb.append("${String.format(Locale.US, "%.2f", item.notchDeductionKg)};")
      sb.append("${String.format(Locale.US, "%.2f", item.totalWeightKg)};")
      sb.append("${String.format(Locale.US, "%.3f", item.totalWeightTon)}\n")
    }

    val totalTon = sumNetKg / 1000.0
    // Compact TOTALES row strictly aligned:
    // 1: TOTALES
    // 2: (Categoría) empty
    // 3: (Tipo) empty
    // 4: (Marca) empty
    // 5: (Dimensiones) empty
    // 6: (Destaje) empty
    // 7: (Longitud) empty
    // 8: Cantidad
    // 9: Long. Total (m)
    // 10: (Peso Unitario) empty
    // 11: Peso Bruto (kg)
    // 12: Reducción Destaje (kg)
    // 13: Peso Neto Total (kg)
    // 14: Peso Total (t)
    sb.append("TOTALES;;;;;;;;;;${sumQty};${String.format(Locale.US, "%.2f", sumLongM)};;${String.format(Locale.US, "%.2f", sumGrossKg)};${String.format(Locale.US, "%.2f", sumDeductionKg)};${String.format(Locale.US, "%.2f", sumNetKg)};${String.format(Locale.US, "%.3f", totalTon)}\n")

    return sb.toString()
  }

  private fun buildDetailedCsv(items: List<BeamItemEntity>): String {
    val sb = StringBuilder()
    // Excel column separator directive
    sb.append("sep=;\n")

    // Detailed Headers (22 columns) - Radio r is completely removed
    // Row 1 (Titles)
    sb.append("Item;Categoria;Tipo;Marca;Dimensiones;Destaje;Peralte h;Altura hw;Base b;Espesor tf;Espesor tw;Longitud;L patin superior;L alma;L patin inferior;Cantidad;Long. Total;Peso Unitario;Peso Bruto;Reduccion;Peso Neto Total;Peso Total\n")
    // Row 2 (Units)
    sb.append("-;-;-;-;-;Ld x Hd;mm;mm;mm;mm;mm;mm;mm;mm;mm;u;m;kg/m;kg;kg;kg;t\n")

    var sumQty = 0
    var sumLongM = 0.0
    var sumGrossKg = 0.0
    var sumDeductionKg = 0.0
    var sumNetKg = 0.0

    items.forEachIndexed { idx, item ->
      val isPl = isPlate(item.category, item.type)
      val longTotalM = if (isPl) 0.0 else (item.referenceLengthMm * item.quantity) / 1000.0

      sumQty += item.quantity
      sumLongM += longTotalM
      sumGrossKg += item.grossWeightKg
      sumDeductionKg += item.notchDeductionKg
      sumNetKg += item.totalWeightKg

      val destajeText = cleanDestajeInstruction(item)
      val isNonI = isPlateOrColdFormed(item.category, item.type)
      val longTotalText = if (isPl) "-" else String.format(Locale.US, "%.2f", longTotalM)

      sb.append("${idx + 1};")
      sb.append("${escapeCsv(item.category)};")
      sb.append("${escapeCsv(item.type)};")
      sb.append("${escapeCsv(item.name)};")
      sb.append("${escapeCsv(item.dims)};")
      sb.append("${escapeCsv(destajeText)};")

      // Geometric columns (5 columns: h, hw, b, tf, tw): If Plate or Cold-Formed, leave empty
      if (isNonI) {
        sb.append(";;;;;") // h, hw, b, tf, tw empty
      } else {
        sb.append("${String.format(Locale.US, "%.2f", item.h)};")
        sb.append("${String.format(Locale.US, "%.2f", item.hw)};")
        sb.append("${String.format(Locale.US, "%.2f", item.b)};")
        sb.append("${String.format(Locale.US, "%.2f", item.tf)};")
        sb.append("${String.format(Locale.US, "%.2f", item.tw)};")
      }

      sb.append("${String.format(Locale.US, "%.2f", item.referenceLengthMm)};")
      sb.append("${item.topFlangeLengthMm?.let { String.format(Locale.US, "%.2f", it) } ?: ""};")
      sb.append("${item.webLengthMm?.let { String.format(Locale.US, "%.2f", it) } ?: ""};")
      sb.append("${item.bottomFlangeLengthMm?.let { String.format(Locale.US, "%.2f", it) } ?: ""};")
      sb.append("${item.quantity};")
      sb.append("${longTotalText};")
      sb.append("${String.format(Locale.US, "%.2f", item.unitWeightKgM)};")
      sb.append("${String.format(Locale.US, "%.2f", item.grossWeightKg)};")
      sb.append("${String.format(Locale.US, "%.2f", item.notchDeductionKg)};")
      sb.append("${String.format(Locale.US, "%.2f", item.totalWeightKg)};")
      sb.append("${String.format(Locale.US, "%.3f", item.totalWeightTon)}\n")
    }

    val totalTon = sumNetKg / 1000.0
    // Detailed TOTALES row strictly aligned:
    // 1: TOTALES
    // 2-15 (14 empty columns): identity, geometry and lengths
    // 16: quantity; remaining columns contain the totals
    sb.append("TOTALES;;;;;;;;;;;;;;;${sumQty};${String.format(Locale.US, "%.2f", sumLongM)};;${String.format(Locale.US, "%.2f", sumGrossKg)};${String.format(Locale.US, "%.2f", sumDeductionKg)};${String.format(Locale.US, "%.2f", sumNetKg)};${String.format(Locale.US, "%.3f", totalTon)}\n")

    return sb.toString()
  }
}

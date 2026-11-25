package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.BeamItemEntity
import com.example.model.format
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ExcelExportHelper {

  private fun escapeXml(value: String): String {
    return value
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&apos;")
  }

  fun exportAndShareExcel(context: Context, items: List<BeamItemEntity>, isDetailed: Boolean): Boolean {
    if (items.isEmpty()) return false

    val fileName = if (isDetailed) "Cubicacion_Montaje_Detallado.xlsx" else "Cubicacion_Montaje_Compacto.xlsx"
    val file = File(context.cacheDir, fileName)

    val xmlContent = if (isDetailed) {
      buildDetailedExcelXml(items)
    } else {
      buildCompactExcelXml(items)
    }

    FileOutputStream(file).use { out ->
      out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
      out.write(xmlContent.toByteArray(Charsets.UTF_8))
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "application/vnd.ms-excel"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, "Cubicación de Estructuras de Acero y Montaje")
      putExtra(
        Intent.EXTRA_TEXT,
        "Reporte de cubicación en formato Excel (.xlsx) con fórmulas, pesos de despacho y destajes."
      )
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Compartir reporte Excel"))
    return true
  }

  private fun cleanDestajeInstruction(item: BeamItemEntity): String {
    if (!item.hasDestaje) return "Sin destaje"
    val tipo = item.destajeTipo.ifBlank { "Destaje" }
    val dLong = item.destajeLongMm.toInt()
    val dAlt = item.destajeAltMm.toInt()
    return if (dLong > 0 && dAlt > 0) {
      "$tipo ${dLong}x${dAlt} mm"
    } else if (dLong > 0) {
      "$tipo L=${dLong} mm"
    } else {
      tipo
    }
  }

  private fun isPlateOrColdFormed(category: String, type: String): Boolean {
    val cat = category.lowercase()
    val typ = type.lowercase()
    return cat.contains("placa") || cat.contains("correa") || cat.contains("canal") ||
      typ.contains("placa") || typ.contains("correa") || typ.contains("canal")
  }

  private fun buildCompactExcelXml(items: List<BeamItemEntity>): String {
    val sb = StringBuilder()
    sb.append("""<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40">
 <DocumentProperties xmlns="urn:schemas-microsoft-com:office:office">
  <Title>Cubicación de Montaje Compacto</Title>
  <Author>Control y Cubicador de Vigas</Author>
 </DocumentProperties>
 <Styles>
  <Style ss:ID="Default" ss:Name="Normal">
   <Alignment ss:Vertical="Center"/>
   <Borders/>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#000000"/>
   <Interior/>
   <NumberFormat/>
   <Protection/>
  </Style>
  <Style ss:ID="Header">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#475569"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#475569"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#475569"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#475569"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#FFFFFF" ss:Bold="1"/>
   <Interior ss:Color="#1E293B" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="TextLeft">
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="TextCenter">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="IntNum">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
   <NumberFormat ss:Format="#,##0"/>
  </Style>
  <Style ss:ID="Dec1">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
   <NumberFormat ss:Format="#,##0.0"/>
  </Style>
  <Style ss:ID="Dec2">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
   <NumberFormat ss:Format="#,##0.00"/>
  </Style>
  <Style ss:ID="Dec3">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
   <NumberFormat ss:Format="#,##0.000"/>
  </Style>
  <Style ss:ID="TotalsLabel">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#0F172A" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="TotalsEmpty">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="TotalsInt">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#0F172A" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="#,##0"/>
  </Style>
  <Style ss:ID="TotalsDec1">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#0F172A" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="#,##0.0"/>
  </Style>
  <Style ss:ID="TotalsDec2">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#0F172A" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="#,##0.00"/>
  </Style>
  <Style ss:ID="TotalsDec3">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#047857" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="#,##0.000"/>
  </Style>
 </Styles>
 <Worksheet ss:Name="Cubicacion">
  <Table ss:DefaultRowHeight="20">
   <Column ss:Width="40"/>
   <Column ss:Width="110"/>
   <Column ss:Width="90"/>
   <Column ss:Width="130"/>
   <Column ss:Width="150"/>
   <Column ss:Width="180"/>
   <Column ss:Width="90"/>
   <Column ss:Width="70"/>
   <Column ss:Width="100"/>
   <Column ss:Width="130"/>
   <Column ss:Width="100"/>
   <Column ss:Width="130"/>
   <Column ss:Width="110"/>
   <Column ss:Width="95"/>
""")

    // Compact Headers: 14 columns
    val headers = listOf(
      "Item", "Categoría", "Tipo", "Perfil / Marca", "Dimensiones (mm)",
      "Destaje", "Longitud (mm)", "Cantidad (u)", "Longitud Total (m)",
      "Peso Unitario (kg/m o kg/u)", "Peso Bruto (kg)", "Deducción Destaje (kg)",
      "Peso Neto Total (kg)", "Peso Total (t)"
    )

    sb.append("   <Row ss:Height=\"26\">\n")
    headers.forEach { h ->
      sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">${escapeXml(h)}</Data></Cell>\n")
    }
    sb.append("   </Row>\n")

    var sumQty = 0
    var sumLongM = 0.0
    var sumGrossKg = 0.0
    var sumDeductionKg = 0.0
    var sumNetKg = 0.0

    items.forEachIndexed { idx, item ->
      val longTotalM = (item.lengthMm * item.quantity) / 1000.0
      sumQty += item.quantity
      sumLongM += longTotalM
      sumGrossKg += item.grossWeightKg
      sumDeductionKg += item.notchDeductionKg
      sumNetKg += item.totalWeightKg

      val destajeText = cleanDestajeInstruction(item)

      sb.append("   <Row>\n")
      // Col 1: Item #
      sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"Number\">${idx + 1}</Data></Cell>\n")
      // Col 2: Categoría
      sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">${escapeXml(item.category)}</Data></Cell>\n")
      // Col 3: Tipo
      sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">${escapeXml(item.type)}</Data></Cell>\n")
      // Col 4: Perfil / Marca
      sb.append("    <Cell ss:StyleID=\"TextLeft\"><Data ss:Type=\"String\">${escapeXml(item.name)}</Data></Cell>\n")
      // Col 5: Dimensiones (mm)
      sb.append("    <Cell ss:StyleID=\"TextLeft\"><Data ss:Type=\"String\">${escapeXml(item.dims)}</Data></Cell>\n")
      // Col 6: Destaje (Instruction only)
      sb.append("    <Cell ss:StyleID=\"TextLeft\"><Data ss:Type=\"String\">${escapeXml(destajeText)}</Data></Cell>\n")
      // Col 7: Longitud (mm) - pure number
      sb.append("    <Cell ss:StyleID=\"IntNum\"><Data ss:Type=\"Number\">${item.lengthMm.toInt()}</Data></Cell>\n")
      // Col 8: Cantidad (u) - pure number
      sb.append("    <Cell ss:StyleID=\"IntNum\"><Data ss:Type=\"Number\">${item.quantity}</Data></Cell>\n")
      // Col 9: Longitud Total (m) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec2\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.2f", longTotalM)}</Data></Cell>\n")
      // Col 10: Peso Unitario (kg/m o kg/u) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec2\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.2f", item.unitWeightKgM)}</Data></Cell>\n")
      // Col 11: Peso Bruto (kg) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.grossWeightKg)}</Data></Cell>\n")
      // Col 12: Deducción Destaje (kg) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.notchDeductionKg)}</Data></Cell>\n")
      // Col 13: Peso Neto Total (kg) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.totalWeightKg)}</Data></Cell>\n")
      // Col 14: Peso Total (t) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec3\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.3f", item.totalWeightTon)}</Data></Cell>\n")
      sb.append("   </Row>\n")
    }

    // TOTALES Row: strictly aligned
    // 1: TOTALES
    // 2: empty
    // 3: empty
    // 4: empty
    // 5: empty
    // 6: empty
    // 7: empty
    // 8: sumQty (Cantidad u)
    // 9: sumLongM (Longitud Total m)
    // 10: empty (Peso Unitario)
    // 11: sumGrossKg (Peso Bruto kg)
    // 12: sumDeductionKg (Deducción Destaje kg)
    // 13: sumNetKg (Peso Neto Total kg)
    // 14: sumNetKg / 1000.0 (Peso Total t)
    val totalTon = sumNetKg / 1000.0

    sb.append("   <Row ss:Height=\"24\">\n")
    sb.append("    <Cell ss:StyleID=\"TotalsLabel\"><Data ss:Type=\"String\">TOTALES</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsEmpty\"/>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsEmpty\"/>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsEmpty\"/>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsEmpty\"/>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsEmpty\"/>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsEmpty\"/>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsInt\"><Data ss:Type=\"Number\">$sumQty</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec2\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.2f", sumLongM)}</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsEmpty\"/>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", sumGrossKg)}</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", sumDeductionKg)}</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", sumNetKg)}</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec3\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.3f", totalTon)}</Data></Cell>\n")
    sb.append("   </Row>\n")

    sb.append("""  </Table>
 </Worksheet>
</Workbook>""")
    return sb.toString()
  }

  private fun buildDetailedExcelXml(items: List<BeamItemEntity>): String {
    val sb = StringBuilder()
    sb.append("""<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40">
 <DocumentProperties xmlns="urn:schemas-microsoft-com:office:office">
  <Title>Cubicación de Montaje Detallado</Title>
  <Author>Control y Cubicador de Vigas</Author>
 </DocumentProperties>
 <Styles>
  <Style ss:ID="Default" ss:Name="Normal">
   <Alignment ss:Vertical="Center"/>
   <Borders/>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#000000"/>
   <Interior/>
   <NumberFormat/>
   <Protection/>
  </Style>
  <Style ss:ID="Header">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#475569"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#475569"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#475569"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#475569"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#FFFFFF" ss:Bold="1"/>
   <Interior ss:Color="#0F766E" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="TextLeft">
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="TextCenter">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="IntNum">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
   <NumberFormat ss:Format="#,##0"/>
  </Style>
  <Style ss:ID="Dec1">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
   <NumberFormat ss:Format="#,##0.0"/>
  </Style>
  <Style ss:ID="Dec2">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
   <NumberFormat ss:Format="#,##0.00"/>
  </Style>
  <Style ss:ID="Dec3">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
   <NumberFormat ss:Format="#,##0.000"/>
  </Style>
  <Style ss:ID="TotalsLabel">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#0F172A" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="TotalsEmpty">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="TotalsInt">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#0F172A" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="#,##0"/>
  </Style>
  <Style ss:ID="TotalsDec1">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#0F172A" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="#,##0.0"/>
  </Style>
  <Style ss:ID="TotalsDec2">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#0F172A" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="#,##0.00"/>
  </Style>
  <Style ss:ID="TotalsDec3">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#0F172A"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0F172A"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#047857" ss:Bold="1"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="#,##0.000"/>
  </Style>
 </Styles>
 <Worksheet ss:Name="CubicacionDetallada">
  <Table ss:DefaultRowHeight="20">
   <Column ss:Width="35"/>
   <Column ss:Width="110"/>
   <Column ss:Width="90"/>
   <Column ss:Width="130"/>
   <Column ss:Width="150"/>
   <Column ss:Width="180"/>
   <Column ss:Width="75"/>
   <Column ss:Width="75"/>
   <Column ss:Width="75"/>
   <Column ss:Width="75"/>
   <Column ss:Width="75"/>
   <Column ss:Width="75"/>
   <Column ss:Width="85"/>
   <Column ss:Width="70"/>
   <Column ss:Width="100"/>
   <Column ss:Width="130"/>
   <Column ss:Width="100"/>
   <Column ss:Width="130"/>
   <Column ss:Width="110"/>
   <Column ss:Width="95"/>
""")

    // Detailed Headers: 20 columns
    val headers = listOf(
      "Item", "Categoría", "Tipo", "Perfil / Marca", "Dimensiones (mm)",
      "Destaje", "Peralte h (mm)", "Altura alma hw (mm)", "Base b (mm)",
      "Espesor tf (mm)", "Espesor tw (mm)", "Radio r (mm)", "Longitud (mm)",
      "Cantidad (u)", "Longitud Total (m)", "Peso Unitario (kg/m o kg/u)",
      "Peso Bruto (kg)", "Deducción Destaje (kg)", "Peso Neto Total (kg)",
      "Peso Total (t)"
    )

    sb.append("   <Row ss:Height=\"26\">\n")
    headers.forEach { h ->
      sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">${escapeXml(h)}</Data></Cell>\n")
    }
    sb.append("   </Row>\n")

    var sumQty = 0
    var sumLongM = 0.0
    var sumGrossKg = 0.0
    var sumDeductionKg = 0.0
    var sumNetKg = 0.0

    items.forEachIndexed { idx, item ->
      val longTotalM = (item.lengthMm * item.quantity) / 1000.0
      sumQty += item.quantity
      sumLongM += longTotalM
      sumGrossKg += item.grossWeightKg
      sumDeductionKg += item.notchDeductionKg
      sumNetKg += item.totalWeightKg

      val destajeText = cleanDestajeInstruction(item)
      val isNonI = isPlateOrColdFormed(item.category, item.type)

      sb.append("   <Row>\n")
      // 1: Item #
      sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"Number\">${idx + 1}</Data></Cell>\n")
      // 2: Categoría
      sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">${escapeXml(item.category)}</Data></Cell>\n")
      // 3: Tipo
      sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">${escapeXml(item.type)}</Data></Cell>\n")
      // 4: Perfil / Marca
      sb.append("    <Cell ss:StyleID=\"TextLeft\"><Data ss:Type=\"String\">${escapeXml(item.name)}</Data></Cell>\n")
      // 5: Dimensiones (mm)
      sb.append("    <Cell ss:StyleID=\"TextLeft\"><Data ss:Type=\"String\">${escapeXml(item.dims)}</Data></Cell>\n")
      // 6: Destaje (Instruction only)
      sb.append("    <Cell ss:StyleID=\"TextLeft\"><Data ss:Type=\"String\">${escapeXml(destajeText)}</Data></Cell>\n")

      // Geometric columns: If Plate or Cold-formed, output "-"
      if (isNonI) {
        sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">-</Data></Cell>\n") // h
        sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">-</Data></Cell>\n") // hw
        sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">-</Data></Cell>\n") // b
        sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">-</Data></Cell>\n") // tf
        sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">-</Data></Cell>\n") // tw
        sb.append("    <Cell ss:StyleID=\"TextCenter\"><Data ss:Type=\"String\">-</Data></Cell>\n") // r
      } else {
        sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.h)}</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.hw)}</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.b)}</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.tf)}</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.tw)}</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.r)}</Data></Cell>\n")
      }

      // 13: Longitud (mm) - pure number
      sb.append("    <Cell ss:StyleID=\"IntNum\"><Data ss:Type=\"Number\">${item.lengthMm.toInt()}</Data></Cell>\n")
      // 14: Cantidad (u) - pure number
      sb.append("    <Cell ss:StyleID=\"IntNum\"><Data ss:Type=\"Number\">${item.quantity}</Data></Cell>\n")
      // 15: Longitud Total (m) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec2\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.2f", longTotalM)}</Data></Cell>\n")
      // 16: Peso Unitario (kg/m o kg/u) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec2\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.2f", item.unitWeightKgM)}</Data></Cell>\n")
      // 17: Peso Bruto (kg) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.grossWeightKg)}</Data></Cell>\n")
      // 18: Deducción Destaje (kg) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.notchDeductionKg)}</Data></Cell>\n")
      // 19: Peso Neto Total (kg) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", item.totalWeightKg)}</Data></Cell>\n")
      // 20: Peso Total (t) - pure number
      sb.append("    <Cell ss:StyleID=\"Dec3\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.3f", item.totalWeightTon)}</Data></Cell>\n")
      sb.append("   </Row>\n")
    }

    // Detailed TOTALES Row: strictly aligned
    // 1: TOTALES
    // 2-13: Empty
    // 14: sumQty (Cantidad u)
    // 15: sumLongM (Longitud Total m)
    // 16: Empty (Peso Unitario)
    // 17: sumGrossKg (Peso Bruto kg)
    // 18: sumDeductionKg (Deducción Destaje kg)
    // 19: sumNetKg (Peso Neto Total kg)
    // 20: sumNetKg / 1000.0 (Peso Total t)
    val totalTon = sumNetKg / 1000.0

    sb.append("   <Row ss:Height=\"24\">\n")
    sb.append("    <Cell ss:StyleID=\"TotalsLabel\"><Data ss:Type=\"String\">TOTALES</Data></Cell>\n")
    for (i in 2..13) {
      sb.append("    <Cell ss:StyleID=\"TotalsEmpty\"/>\n")
    }
    sb.append("    <Cell ss:StyleID=\"TotalsInt\"><Data ss:Type=\"Number\">$sumQty</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec2\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.2f", sumLongM)}</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsEmpty\"/>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", sumGrossKg)}</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", sumDeductionKg)}</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec1\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.1f", sumNetKg)}</Data></Cell>\n")
    sb.append("    <Cell ss:StyleID=\"TotalsDec3\"><Data ss:Type=\"Number\">${String.format(Locale.US, "%.3f", totalTon)}</Data></Cell>\n")
    sb.append("   </Row>\n")

    sb.append("""  </Table>
 </Worksheet>
</Workbook>""")
    return sb.toString()
  }
}

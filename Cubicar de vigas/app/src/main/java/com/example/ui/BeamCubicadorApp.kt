package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BeamItemEntity
import com.example.data.SavedProfileEntity
import com.example.model.BeamCatalog
import com.example.model.CommercialProfile
import com.example.model.DestajeCatalog
import com.example.model.format
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberYellow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GreenTon
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.SteelBlue
import com.example.viewmodel.BeamSummary
import com.example.viewmodel.BeamViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BeamCubicadorApp(
  viewModel: BeamViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }

  val beamItems by viewModel.beamItems.collectAsStateWithLifecycle()
  val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()
  val summary by viewModel.summary.collectAsStateWithLifecycle()
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

  var selectedCategoryFilter by remember { mutableStateOf<String?>("Todos") }
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  val filteredItems = remember(beamItems, selectedCategoryFilter) {
    if (selectedCategoryFilter == null || selectedCategoryFilter == "Todos") {
      beamItems
    } else {
      beamItems.filter { item ->
        when (selectedCategoryFilter) {
          "V. Principales" -> item.category == "Viga Principal"
          "V. Secundarias" -> item.category == "Viga Secundaria"
          "Columnas" -> item.category == "Columna"
          "Otros" -> item.category == "Correa / Canal" || item.category == "Otros"
          "Placas" -> item.category == "Placa" || item.category == "Placas"
          "Vigas especiales" -> item.category == "Vigas especiales"
          else -> item.category.equals(selectedCategoryFilter, ignoreCase = true)
        }
      }
    }
  }

  // Listen to one-shot messages
  LaunchedEffect(Unit) {
    viewModel.userMessage.collectLatest { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = Slate950,
    snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.imePadding()) }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // 1. Header with title, icon and export action buttons
      item {
        HeaderSection(
          onExportCompact = { viewModel.exportAndShare(context, isDetailed = false) },
          onExportDetailed = { viewModel.exportAndShare(context, isDetailed = true) },
          onPrintPdf = { viewModel.printPdfReport(context) }
        )
      }

      // 2. Statistics Summary Cards Bar
      item {
        SummaryCardsSection(summary = summary)
      }

      // 3. Tab Switcher (4 Pestañas Principales)
      item {
        Text("AÑADIR ELEMENTO", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        CustomTabRow(
          selectedTabIndex = selectedTab,
          tabTitles = viewModel.tabTitles,
          onTabSelected = { viewModel.setTab(it) }
        )
      }

      // 4. Input Panel based on active structural element tab
      item {
        StructuralElementPanel(
          viewModel = viewModel,
          savedProfiles = savedProfiles
        )
      }

      // 5. Beam Queue Header and Table
      item {
        Spacer(Modifier.height(4.dp))
        BeamTableHeader(
          itemCount = beamItems.size,
          categories = listOf("Todos") + viewModel.tabTitles,
          selectedCategory = selectedCategoryFilter ?: "Todos",
          onCategorySelected = { selectedCategoryFilter = it },
          onClearClicked = {
            if (beamItems.isNotEmpty()) {
              showClearConfirmDialog = true
            } else {
              Toast.makeText(context, "La lista ya está vacía", Toast.LENGTH_SHORT).show()
            }
          }
        )
      }

      if (filteredItems.isEmpty()) {
        item {
          EmptyQueueState(hasTotalItems = beamItems.isNotEmpty())
        }
      } else {
        itemsIndexed(filteredItems, key = { _, item -> item.id }) { _, item ->
          BeamHorizontalTableView(
            items = listOf(item),
            onDeleteItem = { viewModel.deleteBeamItem(it) },
            showTotals = false
          )
        }
        item {
          BeamQueueTotals(items = filteredItems)
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Vaciar lista", color = Color.White, fontWeight = FontWeight.Bold) },
      text = { Text("¿Deseas vaciar toda la lista acumulada de vigas para montaje?", color = Slate300) },
      containerColor = Slate800,
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllBeams()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
          Text("Sí, vaciar")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancelar", color = Slate400)
        }
      }
    )
  }
}

@Composable
private fun HeaderSection(
  onExportCompact: () -> Unit,
  onExportDetailed: () -> Unit,
  onPrintPdf: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Slate800),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // App logo badge
        Image(
          painter = painterResource(id = R.drawable.app_icon),
          contentDescription = "Logo de la Aplicación",
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, AmberWarning, RoundedCornerShape(10.dp))
        )

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Control y Cubicador de Vigas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Cubicación de despacho y peso para transporte y montaje en obra",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400
          )
        }
      }

      HorizontalDivider(color = Slate700)

     // Export Buttons Layout
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onExportCompact,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("export_compact_btn")
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("CSV compacto", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
          }

          Button(
            onClick = onExportDetailed,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("export_detailed_btn")
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("CSV detallado", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
          }
        }

        Button(
          onClick = onPrintPdf,
          colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("export_pdf_btn")
        ) {
          Icon(Icons.Default.Print, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Guardar / Imprimir PDF", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Slate950)
        }
      }
    }
  }
}

@Composable
private fun SummaryCardsSection(summary: BeamSummary) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Slate950.copy(alpha = 0.6f)),
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DataMetricTile(
          label = "TOTAL PIEZAS",
          value = "${summary.totalPieces} u",
          valueColor = SkyBlueLight,
          modifier = Modifier.weight(1f)
        )
        DataMetricTile(
          label = "LONGITUD ACUMULADA",
          value = "${String.format(Locale.US, "%.2f", summary.totalLengthM)} m",
          valueColor = SkyBlueLight,
          modifier = Modifier.weight(1f)
        )
      }

      HorizontalDivider(color = Slate800, thickness = 0.8.dp)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DataMetricTile(
          label = "PESO ACUMULADO",
          value = "${String.format(Locale.US, "%,.1f", summary.totalWeightKg)} kg",
          valueColor = SkyBlueLight,
          modifier = Modifier.weight(1f)
        )
        DataMetricTile(
          label = "CARGA MONTAJE",
          value = "${String.format(Locale.US, "%.3f", summary.totalWeightTon)} t",
          valueColor = GreenTon,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun DataMetricTile(
  label: String,
  value: String,
  valueColor: Color,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    verticalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      color = Slate400,
      letterSpacing = 0.5.sp
    )
    Text(
      text = value,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = valueColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun SummaryCard(
  label: String,
  value: String,
  valueColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = Slate800),
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Slate400
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = valueColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun CustomTabRow(
  selectedTabIndex: Int,
  tabTitles: List<String>,
  onTabSelected: (Int) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Slate950),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      tabTitles.forEachIndexed { index, title ->
        val selected = selectedTabIndex == index
        val icon = when (index) {
          0 -> Icons.Default.ViewStream
          1 -> Icons.Default.LinearScale
          2 -> Icons.Default.ViewWeek
          3 -> Icons.Default.Widgets
          4 -> Icons.Default.Layers
          else -> Icons.Default.DashboardCustomize
        }
        val activeBg = when (index) {
          0 -> Color(0xFF2563EB)
          1 -> Color(0xFF0D9488)
          2 -> Color(0xFFD97706)
          3 -> Color(0xFF7C3AED)
          4 -> Color(0xFFE11D48)
          else -> Color(0xFF0F766E)
        }

        Button(
          onClick = { onTabSelected(index) },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) activeBg else Color.Transparent,
            contentColor = if (selected) Color.White else Slate400
          ),
          elevation = if (selected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
          modifier = Modifier.testTag("tab_item_$index")
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (selected) Color.White else Slate400
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = title,
            fontSize = 12.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StructuralElementPanel(
  viewModel: BeamViewModel,
  savedProfiles: List<SavedProfileEntity>
) {
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val profileSubMode by viewModel.profileSubMode.collectAsStateWithLifecycle()

  val categoryTitle = viewModel.tabTitles.getOrElse(selectedTab) { "Vigas Principales" }
  val categorySubtitle = when (selectedTab) {
    0 -> "Perfiles principales y vigas de carga para puentes, pórticos y naves"
    1 -> "Viguetas de piso, correas y amarres de conexión con destaje de alas"
    2 -> "Pilares, soportes y columnas estructurales (comerciales o cajón 2 canales)"
    3 -> "Perfiles conformados en frío: Correas G y Canales U"
    5 -> "Elige un perfil comercial o creado y agrega las longitudes reales de sus placas"
    else -> "Placas base, cartelas de nudo, rigidizadores y planchas de acero"
  }

  val categoryColor = when (selectedTab) {
    0 -> Color(0xFF3B82F6)
    1 -> Color(0xFF14B8A6)
    2 -> Color(0xFFF59E0B)
    3 -> Color(0xFF8B5CF6)
    5 -> Color(0xFF0F766E)
    else -> Color(0xFFE11D48)
  }

  val hasSubModes = selectedTab in 0..2 || selectedTab == 5
  val subModeLabels = when (selectedTab) {
    0, 1, 5 -> listOf("Perfil Comercial", "Perfil Creado / Armado")
    2 -> listOf("Catálogo Comercial (WF/HSS)", "Columna Armada (S-BOX)")
    else -> emptyList()
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Slate800),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
  ) {
    Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(if (selectedTab == 5) 18.dp else 14.dp),
      verticalArrangement = Arrangement.spacedBy(if (selectedTab == 5) 18.dp else 14.dp)
    ) {
      // Category Header Banner
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .background(categoryColor, CircleShape)
            )
            Text(
              text = categoryTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
          Text(
            text = categorySubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
            fontSize = 11.5.sp
          )
        }
      }

      // Sub-mode Selector (Visible for tabs 0, 1, 2)
      if (hasSubModes && subModeLabels.isNotEmpty()) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = Slate900),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
          if (selectedTab == 5) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              subModeLabels.forEachIndexed { idx, label ->
                val isSelected = profileSubMode == idx
                Button(
                  onClick = { viewModel.setProfileSubMode(idx) },
                  modifier = Modifier.fillMaxWidth().height(46.dp),
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) SteelBlue else Slate800,
                    contentColor = if (isSelected) Color.White else Slate300
                  ),
                  elevation = ButtonDefaults.buttonElevation(0.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                  Text(
                    text = if (idx == 0) "Perfil comercial del catálogo" else "Perfil creado o dimensiones manuales",
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth().padding(3.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              subModeLabels.forEachIndexed { idx, label ->
                val isSelected = profileSubMode == idx
                Button(
                  onClick = { viewModel.setProfileSubMode(idx) },
                  modifier = Modifier.weight(1f).height(36.dp),
                  shape = RoundedCornerShape(6.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) SteelBlue else Color.Transparent,
                    contentColor = if (isSelected) Color.White else Slate400
                  ),
                  elevation = ButtonDefaults.buttonElevation(0.dp),
                  contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                  Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }

      // Tab Forms
      Text(
        when (selectedTab) {
          5 -> "PERFIL, LONGITUDES Y DESTAJES"
          3 -> "DATOS DEL PERFIL CONFORMADO"
          4 -> "DIMENSIONES DE LA PLACA"
          else -> "DATOS DE LA PIEZA"
        },
        color = Slate400,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.7.sp
      )
      when (selectedTab) {
        0 -> {
          // Tab 0: Vigas Principales (VP)
          if (profileSubMode == 0) {
            CommercialProfileInputSection(viewModel = viewModel, selectedTab = 0)
          } else {
            ArmadaProfileInputSection(
              viewModel = viewModel,
              savedProfiles = savedProfiles.filter { it.profileType != "BOX_COLUMN" },
              selectedTab = 0
            )
          }
        }
        1 -> {
          // Tab 1: Vigas Secundarias (VS)
          if (profileSubMode == 0) {
            CommercialProfileInputSection(viewModel = viewModel, selectedTab = 1)
          } else {
            ArmadaProfileInputSection(
              viewModel = viewModel,
              savedProfiles = savedProfiles.filter { it.profileType != "BOX_COLUMN" },
              selectedTab = 1
            )
          }
          // Destaje Configuration is EXCLUSIVELY shown and applied here
          DestajeConfigurationCard(
            viewModel = viewModel,
            isSecondaryBeam = true
          )
        }
        2 -> {
          // Tab 2: Columnas (COL)
          if (profileSubMode == 0) {
            CommercialProfileInputSection(viewModel = viewModel, selectedTab = 2)
          } else {
            BoxColumnInputSection(
              viewModel = viewModel,
              savedProfiles = savedProfiles.filter { it.profileType == "BOX_COLUMN" }
            )
          }
        }
        3 -> {
          // Tab 3: Otros (OTR - Correas G y Canales U)
          ColdFormedInputSection(viewModel = viewModel)
        }
        4 -> {
          // Tab 4: Placas (PLA)
          PlacasInputSection(viewModel = viewModel)
        }
        5 -> {
          if (profileSubMode == 0) {
            CommercialProfileInputSection(viewModel = viewModel, selectedTab = 5)
          } else {
            ArmadaProfileInputSection(
              viewModel = viewModel,
              savedProfiles = savedProfiles.filter { it.profileType != "BOX_COLUMN" },
              selectedTab = 5
            )
          }
          DestajeConfigurationCard(viewModel = viewModel, isSecondaryBeam = false)
        }
      }

      // Add Button
      val addBtnText = when (selectedTab) {
        0 -> "Añadir Viga Principal a la lista"
        1 -> "Añadir Viga Secundaria a la lista"
        2 -> "Añadir Columna a la lista"
        3 -> "Añadir Perfil a la lista"
        5 -> "Añadir Viga Especial a la lista"
        else -> "Añadir Placa a la lista"
      }

      Button(
        onClick = { viewModel.addElement() },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("add_element_btn"),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = categoryColor)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = addBtnText,
          fontWeight = FontWeight.Bold,
          fontSize = 13.5.sp,
          color = Color.White
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommercialProfileDetails(profile: CommercialProfile) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
  ) {
    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(profile.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("${profile.weightKgPerM.format(2)} kg/m", color = SkyBlueLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }
      HorizontalDivider(color = Slate700)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QueueDetail("Dimensiones h × b", "${profile.h.format()} × ${profile.b.format()} mm", Modifier.weight(1f))
        QueueDetail("Alma hw", "${profile.hw.format(1)} mm", Modifier.weight(1f), AmberYellow)
      }
      Text("Espesores: patín ${profile.tf.format()} mm · alma ${profile.tw.format()} mm", color = Slate400, fontSize = 11.sp)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommercialProfileInputSection(
  viewModel: BeamViewModel,
  selectedTab: Int
) {
  val elementName by viewModel.elementName.collectAsStateWithLifecycle()
  val selectedIndex by viewModel.selectedCommercialIndex.collectAsStateWithLifecycle()
  val lengthMm by viewModel.comLengthMm.collectAsStateWithLifecycle()
  val quantity by viewModel.comQuantity.collectAsStateWithLifecycle()
  val hasDestaje by viewModel.hasDestaje.collectAsStateWithLifecycle()
  val destajeLong by viewModel.destajeLongMm.collectAsStateWithLifecycle()
  val destajeType by viewModel.destajeTipo.collectAsStateWithLifecycle()

  val currentProfile = BeamCatalog.profiles.getOrElse(selectedIndex) { BeamCatalog.profiles[0] }
  var expandedDropdown by remember { mutableStateOf(false) }
  var profileSearchQuery by remember { mutableStateOf("") }
  var selectedProfileFamily by remember { mutableStateOf("Todos") }
  val profileFamilies = listOf("Todos", "IPE", "HEA", "HEB", "IPN", "W", "HSS")
  val visibleProfiles = remember(profileSearchQuery, selectedIndex, selectedProfileFamily) {
    val query = profileSearchQuery.trim()
    val filtered = BeamCatalog.profiles.filter { profile ->
      (selectedProfileFamily == "Todos" || profile.name.startsWith(selectedProfileFamily, ignoreCase = true)) &&
        (query.isEmpty() ||
        profile.name.contains(query, ignoreCase = true) ||
          profile.dimsFormatted.contains(query, ignoreCase = true) ||
          profile.weightKgPerM.toString().contains(query))
    }
    if (query.isNotEmpty() || selectedProfileFamily != "Todos") filtered.take(30)
    else {
      val sample = listOf("IPE", "HEA", "HEB", "IPN", "W", "HSS").flatMap { family ->
        BeamCatalog.profiles.filter { it.name.startsWith(family, ignoreCase = true) }.take(3)
      }
      (listOfNotNull(BeamCatalog.profiles.getOrNull(selectedIndex)) + sample).distinct()
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    OutlinedTextField(
      value = elementName,
      onValueChange = { viewModel.setElementName(it) },
      label = { Text(if (selectedTab == 5) "Marca / Nombre (ej: VE-1)" else "Marca / Nombre del Elemento (ej: VP-1, VS-2, COL-1)", color = Slate400, fontSize = 12.sp) },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = SteelBlue,
        unfocusedBorderColor = Slate600,
        focusedContainerColor = Slate900,
        unfocusedContainerColor = Slate900
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("element_mark_input")
    )

    ExposedDropdownMenuBox(
      expanded = expandedDropdown,
      onExpandedChange = {
        expandedDropdown = it
        if (it) profileSearchQuery = ""
      },
      modifier = Modifier.fillMaxWidth()
    ) {
      OutlinedTextField(
        value = "${currentProfile.name} (${currentProfile.weightKgPerM} kg/m)",
        onValueChange = {},
        readOnly = true,
        label = { Text("Perfil Comercial de Catálogo", color = Slate400, fontSize = 12.sp) },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .fillMaxWidth()
          .menuAnchor()
          .testTag("commercial_profile_dropdown")
      )

      ExposedDropdownMenu(
        expanded = expandedDropdown,
        onDismissRequest = { expandedDropdown = false },
        modifier = Modifier.background(Slate800)
      ) {
        OutlinedTextField(
          value = profileSearchQuery,
          onValueChange = { profileSearchQuery = it },
          label = { Text("Buscar perfil", color = Slate400, fontSize = 12.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = SteelBlue,
            unfocusedBorderColor = Slate600,
            focusedContainerColor = Slate900,
            unfocusedContainerColor = Slate900
          ),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          profileFamilies.forEach { family ->
            FilterChip(
              selected = selectedProfileFamily == family,
              onClick = { selectedProfileFamily = family },
              label = { Text(family, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = SteelBlue,
                selectedLabelColor = Color.White,
                labelColor = Slate300
              )
            )
          }
        }
        visibleProfiles.forEach { profile ->
          val idx = BeamCatalog.profiles.indexOf(profile)
          DropdownMenuItem(
            text = {
              Text(
                "${profile.name}  ·  ${profile.weightKgPerM} kg/m  (${profile.dimsFormatted})",
                color = if (idx == selectedIndex) SkyBlueLight else Slate300,
                fontSize = 13.sp,
                fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Normal
              )
            },
            onClick = {
              viewModel.setCommercialProfileIndex(idx)
              expandedDropdown = false
            }
          )
        }
      }
    }

    CommercialProfileDetails(profile = currentProfile)

    if (selectedTab == 5) {
      SpecialBeamLengthsSection(
        viewModel = viewModel,
        h = currentProfile.h,
        b = currentProfile.b,
        tf = currentProfile.tf,
        tw = currentProfile.tw,
        isCommercial = true,
        weightScale = run {
          val geometric = BeamCatalog.calculateArmadaWeightPerM(
            currentProfile.h, currentProfile.b, currentProfile.tf, currentProfile.tw, 0.0
          )
          if (geometric > 0.0) currentProfile.weightKgPerM / geometric else 1.0
        },
        quantity = quantity,
        onQuantityChange = viewModel::setComQuantity
      )
    } else {
      Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedTextField(
        value = lengthMm,
        onValueChange = { viewModel.setComLengthMm(it) },
        label = { Text("Longitud L (mm)", color = Slate400, fontSize = 12.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("com_length_input")
      )

      OutlinedTextField(
        value = quantity,
        onValueChange = { viewModel.setComQuantity(it) },
        label = { Text("Cantidad (u)", color = Slate400, fontSize = 12.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("com_quantity_input")
      )
    }
      val lengthValue = lengthMm.toDoubleOrNull() ?: 0.0
      val quantityValue = quantity.toIntOrNull() ?: 0
      val grossWeight = currentProfile.weightKgPerM * (lengthValue / 1000.0) * quantityValue
      val destajeDeduction = if (selectedTab == 1 && hasDestaje) BeamCatalog.calculateNotchDeductionKg(
        b = currentProfile.b,
        tw = currentProfile.tw,
        tf = currentProfile.tf,
        ldMm = destajeLong.toDoubleOrNull() ?: 0.0,
        notchType = destajeType,
        quantity = quantityValue
      ) else 0.0
      WeightResultCard(
        unitLabel = "Peso de catálogo",
        unitValue = "${currentProfile.weightKgPerM.format(2)} kg/m",
        totalWeightKg = (grossWeight - destajeDeduction).coerceAtLeast(0.0)
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestajeConfigurationCard(
  viewModel: BeamViewModel,
  isSecondaryBeam: Boolean
) {
  val hasDestaje by viewModel.hasDestaje.collectAsStateWithLifecycle()
  val destajeLongMm by viewModel.destajeLongMm.collectAsStateWithLifecycle()
  val destajeAltMm by viewModel.destajeAltMm.collectAsStateWithLifecycle()
  val destajeTipo by viewModel.destajeTipo.collectAsStateWithLifecycle()

  var expandedTipoDropdown by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = if (hasDestaje) Slate950 else Slate900.copy(alpha = 0.6f)
    ),
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (hasDestaje) AmberWarning else Slate700
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCut,
            contentDescription = null,
            tint = if (hasDestaje) AmberYellow else Slate400,
            modifier = Modifier.size(20.dp)
          )
          Column {
            Text(
              text = "Destaje de Conexión (Cortes en Alas y Alma)",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (hasDestaje) Color.White else Slate300
            )
            Text(
              text = if (isSecondaryBeam) "Típico en vigas secundarias para empalmar a ras de viga principal" else "Rebaje de alas o alma para pase de apoyos y cartelas",
              fontSize = 10.5.sp,
              color = if (hasDestaje) AmberYellow.copy(alpha = 0.9f) else Slate500
            )
          }
        }

        Switch(
          checked = hasDestaje,
          onCheckedChange = { viewModel.setHasDestaje(it) },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = AmberWarning,
            uncheckedThumbColor = Slate400,
            uncheckedTrackColor = Slate800
          )
        )
      }

      AnimatedVisibility(visible = hasDestaje) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          HorizontalDivider(color = Slate800)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = destajeLongMm,
              onValueChange = { viewModel.setDestajeLongMm(it) },
              label = { Text("Longitud Destaje Ld (mm)", fontSize = 11.sp, color = AmberYellow) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmberWarning,
                unfocusedBorderColor = Slate600,
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900
              ),
              modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
              value = destajeAltMm,
              onValueChange = { viewModel.setDestajeAltMm(it) },
              label = { Text("Altura Destaje Hd (mm)", fontSize = 11.sp, color = AmberYellow) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmberWarning,
                unfocusedBorderColor = Slate600,
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900
              ),
              modifier = Modifier.weight(1f)
            )
          }

          ExposedDropdownMenuBox(
            expanded = expandedTipoDropdown,
            onExpandedChange = { expandedTipoDropdown = it },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = destajeTipo,
              onValueChange = {},
              readOnly = true,
              label = { Text("Ubicación y Tipo de Destaje", color = Slate400, fontSize = 11.sp) },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoDropdown) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmberWarning,
                unfocusedBorderColor = Slate600,
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900
              ),
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
            )

            ExposedDropdownMenu(
              expanded = expandedTipoDropdown,
              onDismissRequest = { expandedTipoDropdown = false },
              modifier = Modifier.background(Slate800)
            ) {
              DestajeCatalog.types.forEach { tipo ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = tipo,
                      color = if (tipo == destajeTipo) AmberYellow else Slate300,
                      fontWeight = if (tipo == destajeTipo) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 13.sp
                    )
                  },
                  onClick = {
                    viewModel.setDestajeTipo(tipo)
                    expandedTipoDropdown = false
                  }
                )
              }
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(AmberWarning.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
              .border(1.dp, AmberWarning.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Check, contentDescription = null, tint = AmberYellow, modifier = Modifier.size(14.dp))
              Text(
                text = "Destaje activo: ${destajeLongMm} x ${destajeAltMm} mm · $destajeTipo",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = AmberYellow
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArmadaProfileInputSection(
  viewModel: BeamViewModel,
  savedProfiles: List<SavedProfileEntity>,
  selectedTab: Int
) {
  val selectedProfileId by viewModel.selectedSavedProfileId.collectAsStateWithLifecycle()
  val name by viewModel.armName.collectAsStateWithLifecycle()
  val hText by viewModel.armH.collectAsStateWithLifecycle()
  val bText by viewModel.armB.collectAsStateWithLifecycle()
  val tfText by viewModel.armTf.collectAsStateWithLifecycle()
  val twText by viewModel.armTw.collectAsStateWithLifecycle()
  val rText by viewModel.armR.collectAsStateWithLifecycle()
  val lengthMm by viewModel.armLengthMm.collectAsStateWithLifecycle()
  val quantity by viewModel.armQuantity.collectAsStateWithLifecycle()
  val isSpecialBeam = selectedTab == 5
  val hasDestaje by viewModel.hasDestaje.collectAsStateWithLifecycle()
  val destajeLong by viewModel.destajeLongMm.collectAsStateWithLifecycle()
  val destajeType by viewModel.destajeTipo.collectAsStateWithLifecycle()

  val hVal = hText.toDoubleOrNull() ?: 0.0
  val tfVal = tfText.toDoubleOrNull() ?: 0.0
  val hwVal = BeamCatalog.calculateArmadaHw(hVal, tfVal)

  var expandedSavedDropdown by remember { mutableStateOf(false) }
  var showSavedProfiles by remember(selectedTab) { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(if (isSpecialBeam) 16.dp else 10.dp)) {
    if (isSpecialBeam && savedProfiles.isNotEmpty()) {
      OutlinedButton(
        onClick = { showSavedProfiles = !showSavedProfiles },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(9.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
      ) {
        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(if (showSavedProfiles) "Ocultar perfiles guardados" else "Cargar dimensiones de un perfil guardado (opcional)", fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp))
      }
    }

    // A saved profile is only a shortcut; dimensions can still be edited below.
    if (savedProfiles.isNotEmpty() && (!isSpecialBeam || showSavedProfiles)) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = if (isSpecialBeam) "Cargar un perfil creado (opcional)" else "Mis perfiles armados guardados",
            style = MaterialTheme.typography.labelSmall,
            color = Slate400
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ExposedDropdownMenuBox(
              expanded = expandedSavedDropdown,
              onExpandedChange = { expandedSavedDropdown = it },
              modifier = Modifier.weight(1f)
            ) {
              val selectedProfile = savedProfiles.find { it.id == selectedProfileId }
              val labelText = selectedProfile?.let { "${it.name} (${it.h.format()}x${it.b.format()})" }
                ?: "Elegir perfil guardado..."

              OutlinedTextField(
                value = labelText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSavedDropdown) },
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White,
                  focusedBorderColor = SteelBlue,
                  unfocusedBorderColor = Slate700,
                  focusedContainerColor = Slate800,
                  unfocusedContainerColor = Slate800
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor()
              )

              ExposedDropdownMenu(
                expanded = expandedSavedDropdown,
                onDismissRequest = { expandedSavedDropdown = false },
                modifier = Modifier.background(Slate800)
              ) {
                savedProfiles.forEach { sp ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        "${sp.name} (h=${sp.h.format()}, hw=${sp.hw.format()}, b=${sp.b.format()})",
                        color = Color.White,
                        fontSize = 13.sp
                      )
                    },
                    onClick = {
                      viewModel.loadSavedProfile(sp)
                      expandedSavedDropdown = false
                    }
                  )
                }
              }
            }

            val currentSelectedProfile = savedProfiles.find { it.id == selectedProfileId }
            if (currentSelectedProfile != null) {
              IconButton(
                onClick = { viewModel.deleteSavedProfile(currentSelectedProfile) },
                modifier = Modifier
                  .size(42.dp)
                  .background(DangerRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
              ) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar perfil", tint = DangerRed, modifier = Modifier.size(20.dp))
              }
            }
          }
        }
      }
    }

    if (isSpecialBeam) {
      Text("DIMENSIONES DEL PERFIL", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = name,
        onValueChange = { viewModel.setArmName(it) },
        label = { Text("Marca / Nombre", fontSize = 11.sp, color = Slate400) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("arm_name_input")
      )

      OutlinedTextField(
        value = hText,
        onValueChange = { viewModel.setArmH(it) },
        label = { Text("Peralte h (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("arm_h_input")
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = bText,
        onValueChange = { viewModel.setArmB(it) },
        label = { Text("Ancho patín b (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier.weight(1f)
      )
      OutlinedTextField(
        value = tfText,
        onValueChange = { viewModel.setArmTf(it) },
        label = { Text("Espesor tf (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      MetricTile(
        label = "Altura Alma hw (mm)",
        value = "${hwVal.format(1)}",
        textColor = AmberYellow,
        modifier = Modifier.weight(1f)
      )
      OutlinedTextField(
        value = twText,
        onValueChange = { viewModel.setArmTw(it) },
        label = { Text("Espesor tw (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier.weight(1f)
      )
      if (!isSpecialBeam) {
        OutlinedTextField(
          value = rText,
          onValueChange = { viewModel.setArmR(it) },
          label = { Text("Radio r (mm)", fontSize = 11.sp, color = Slate400) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = SteelBlue,
            unfocusedBorderColor = Slate600,
            focusedContainerColor = Slate900,
            unfocusedContainerColor = Slate900
          ),
          modifier = Modifier.weight(1f)
        )
      }
    }

    if (isSpecialBeam) SpecialBeamLengthsSection(
      viewModel = viewModel,
      h = hVal,
      b = bText.toDoubleOrNull() ?: 0.0,
      tf = tfVal,
      tw = twText.toDoubleOrNull() ?: 0.0,
      isCommercial = false,
      weightScale = 1.0,
      quantity = quantity,
      onQuantityChange = viewModel::setArmQuantity
    )

    if (!isSpecialBeam) Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = lengthMm,
        onValueChange = { viewModel.setArmLengthMm(it) },
        label = { Text("Longitud L (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier.weight(1f)
      )

      OutlinedTextField(
        value = quantity,
        onValueChange = { viewModel.setArmQuantity(it) },
        label = { Text("Cantidad (u)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier.weight(1f)
      )
    }

    if (!isSpecialBeam) {
      val lengthValue = lengthMm.toDoubleOrNull() ?: 0.0
      val quantityValue = quantity.toIntOrNull() ?: 0
      val unitWeight = BeamCatalog.calculateArmadaWeightPerM(
        hVal,
        bText.toDoubleOrNull() ?: 0.0,
        tfVal,
        twText.toDoubleOrNull() ?: 0.0,
        rText.toDoubleOrNull() ?: 0.0
      )
      val grossWeight = unitWeight * (lengthValue / 1000.0) * quantityValue
      val destajeDeduction = if (selectedTab == 1 && hasDestaje) BeamCatalog.calculateNotchDeductionKg(
        b = bText.toDoubleOrNull() ?: 0.0,
        tw = twText.toDoubleOrNull() ?: 0.0,
        tf = tfVal,
        ldMm = destajeLong.toDoubleOrNull() ?: 0.0,
        notchType = destajeType,
        quantity = quantityValue
      ) else 0.0
      WeightResultCard(
        unitLabel = "Peso de la sección",
        unitValue = "${unitWeight.format(2)} kg/m",
        totalWeightKg = (grossWeight - destajeDeduction).coerceAtLeast(0.0)
      )
    }

    // Guardar plantilla de perfil armado
    OutlinedButton(
      onClick = { viewModel.saveCustomProfileTemplate() },
      modifier = Modifier
        .fillMaxWidth()
        .height(42.dp)
        .testTag("save_profile_btn"),
      shape = RoundedCornerShape(8.dp),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberYellow),
      border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning)
    ) {
      Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Guardar dimensiones como plantilla de perfil", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
private fun SpecialLengthInput(
  value: String,
  label: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, fontSize = 10.sp, color = Slate400) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    singleLine = true,
    colors = OutlinedTextFieldDefaults.colors(
      focusedTextColor = Color.White,
      unfocusedTextColor = Color.White,
      focusedBorderColor = SteelBlue,
      unfocusedBorderColor = Slate600,
      focusedContainerColor = Slate900,
      unfocusedContainerColor = Slate900
    ),
    modifier = modifier
  )
}

@Composable
private fun SpecialBeamLengthsSection(
  viewModel: BeamViewModel,
  h: Double,
  b: Double,
  tf: Double,
  tw: Double,
  isCommercial: Boolean,
  weightScale: Double,
  quantity: String,
  onQuantityChange: (String) -> Unit
) {
  val topLength by viewModel.specialTopLengthMm.collectAsStateWithLifecycle()
  val webLength by viewModel.specialWebLengthMm.collectAsStateWithLifecycle()
  val bottomLength by viewModel.specialBottomLengthMm.collectAsStateWithLifecycle()
  val hasDestaje by viewModel.hasDestaje.collectAsStateWithLifecycle()
  val destajeLength by viewModel.destajeLongMm.collectAsStateWithLifecycle()
  val destajeType by viewModel.destajeTipo.collectAsStateWithLifecycle()
  val quantityValue = quantity.toIntOrNull() ?: 0
  val grossWeight = BeamCatalog.calculateSpecialBeamWeightKg(
    h = h,
    b = b,
    tf = tf,
    tw = tw,
    topFlangeLengthMm = topLength.toDoubleOrNull() ?: 0.0,
    webLengthMm = webLength.toDoubleOrNull() ?: 0.0,
    bottomFlangeLengthMm = bottomLength.toDoubleOrNull() ?: 0.0
  ) * weightScale * quantityValue
  val notchDeduction = if (hasDestaje) BeamCatalog.calculateNotchDeductionKg(
    b = b,
    tw = tw,
    tf = tf,
    ldMm = destajeLength.toDoubleOrNull() ?: 0.0,
    notchType = destajeType,
    quantity = quantityValue
  ) else 0.0
  val totalWeight = (grossWeight - notchDeduction).coerceAtLeast(0.0)

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text("LONGITUD DE CADA PLACA", color = SkyBlueLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Text("Ingresa las medidas reales de cada elemento longitudinal.", color = Slate400, fontSize = 11.sp)
      SpecialLengthInput(topLength, "Patín superior (mm)", viewModel::setSpecialTopLengthMm, Modifier.fillMaxWidth())
      SpecialLengthInput(webLength, "Alma (mm)", viewModel::setSpecialWebLengthMm, Modifier.fillMaxWidth())
      SpecialLengthInput(bottomLength, "Patín inferior (mm)", viewModel::setSpecialBottomLengthMm, Modifier.fillMaxWidth())
      OutlinedTextField(
        value = quantity,
        onValueChange = onQuantityChange,
        label = { Text("Cantidad de vigas", fontSize = 12.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate800,
          unfocusedContainerColor = Slate800
        ),
        modifier = Modifier.fillMaxWidth()
      )
      MetricTile(
        label = if (hasDestaje) "Peso estimado después del destaje" else "Peso estimado de las placas",
        value = "${totalWeight.format(1)} kg",
        textColor = GreenTon,
        modifier = Modifier.fillMaxWidth()
      )
      Text(
        if (isCommercial) "Peso repartido según las longitudes de placa y calibrado con el kg/m del perfil del catálogo. El destaje se descuenta si lo activas." else "Se calcula por volumen de acero (densidad 7.850 kg/m³). No incluye soldadura; el destaje se descuenta si lo activas.",
        color = Slate400,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
private fun PlacasInputSection(viewModel: BeamViewModel) {
  val placaName by viewModel.placaName.collectAsStateWithLifecycle()
  val espesorMm by viewModel.placaEspesorMm.collectAsStateWithLifecycle()
  val anchoMm by viewModel.placaAnchoMm.collectAsStateWithLifecycle()
  val largoMm by viewModel.placaLargoMm.collectAsStateWithLifecycle()
  val quantity by viewModel.placaQuantity.collectAsStateWithLifecycle()

  val eVal = espesorMm.toDoubleOrNull() ?: 0.0
  val aVal = anchoMm.toDoubleOrNull() ?: 0.0
  val lVal = largoMm.toDoubleOrNull() ?: 0.0
  val qtyVal = quantity.toIntOrNull() ?: 0

  val unitWeightKg = BeamCatalog.calculatePlateWeight(eVal, aVal, lVal)
  val totalWeightKg = unitWeightKg * qtyVal

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    OutlinedTextField(
      value = placaName,
      onValueChange = { viewModel.setPlacaName(it) },
      label = { Text("Marca / Denominación de la Placa (ej: PL-1 Placa Base, CART-1)", color = Slate400, fontSize = 12.sp) },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = SteelBlue,
        unfocusedBorderColor = Slate600,
        focusedContainerColor = Slate900,
        unfocusedContainerColor = Slate900
      ),
      modifier = Modifier.fillMaxWidth()
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = espesorMm,
        onValueChange = { viewModel.setPlacaEspesorMm(it) },
        label = { Text("Espesor e (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier.weight(1f)
      )

      OutlinedTextField(
        value = anchoMm,
        onValueChange = { viewModel.setPlacaAnchoMm(it) },
        label = { Text("Ancho a (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = largoMm,
        onValueChange = { viewModel.setPlacaLargoMm(it) },
        label = { Text("Largo l (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier.weight(1f)
      )

      OutlinedTextField(
        value = quantity,
        onValueChange = { viewModel.setPlacaQuantity(it) },
        label = { Text("Cantidad (u)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier.weight(1f)
      )
    }

    WeightResultCard("Peso unitario", "${unitWeightKg.format(2)} kg/u", totalWeightKg)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxColumnInputSection(
  viewModel: BeamViewModel,
  savedProfiles: List<SavedProfileEntity>
) {
  val name by viewModel.boxColName.collectAsStateWithLifecycle()
  val hText by viewModel.boxColH.collectAsStateWithLifecycle()
  val bText by viewModel.boxColB.collectAsStateWithLifecycle()
  val tText by viewModel.boxColT.collectAsStateWithLifecycle()
  val lengthMm by viewModel.boxColLengthMm.collectAsStateWithLifecycle()
  val quantity by viewModel.boxColQuantity.collectAsStateWithLifecycle()
  val selectedProfileId by viewModel.selectedSavedProfileId.collectAsStateWithLifecycle()

  val hVal = hText.toDoubleOrNull() ?: 0.0
  val bVal = bText.toDoubleOrNull() ?: 0.0
  val tVal = tText.toDoubleOrNull() ?: 0.0
  val lVal = lengthMm.toDoubleOrNull() ?: 0.0
  val qtyVal = quantity.toIntOrNull() ?: 0

  val unitWeightKgM = BeamCatalog.calculateBoxColumnWeightPerM(hVal, bVal, tVal)
  val totalWeightKg = unitWeightKgM * (lVal / 1000.0) * qtyVal

  var expandedSavedDropdown by remember { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    // Saved Box Column Templates
    if (savedProfiles.isNotEmpty()) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Plantillas guardadas de columnas cajón",
            style = MaterialTheme.typography.labelSmall,
            color = Slate400
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ExposedDropdownMenuBox(
              expanded = expandedSavedDropdown,
              onExpandedChange = { expandedSavedDropdown = it },
              modifier = Modifier.weight(1f)
            ) {
              val selectedProfile = savedProfiles.find { it.id == selectedProfileId }
              val labelText = selectedProfile?.let { "${it.name} (H=${it.h.format()} B=${it.b.format()} t=${it.tf.format()})" }
                ?: "Elegir plantilla guardada..."

              OutlinedTextField(
                value = labelText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSavedDropdown) },
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White,
                  focusedBorderColor = SteelBlue,
                  unfocusedBorderColor = Slate700,
                  focusedContainerColor = Slate800,
                  unfocusedContainerColor = Slate800
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor()
              )

              ExposedDropdownMenu(
                expanded = expandedSavedDropdown,
                onDismissRequest = { expandedSavedDropdown = false },
                modifier = Modifier.background(Slate800)
              ) {
                savedProfiles.forEach { sp ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        "${sp.name} · 2 Canales U ${sp.h.format()}x${sp.b.format()}x${sp.tf.format()} mm",
                        color = Color.White,
                        fontSize = 13.sp
                      )
                    },
                    onClick = {
                      viewModel.loadSavedProfile(sp)
                      expandedSavedDropdown = false
                    }
                  )
                }
              }
            }

            val currentSelectedProfile = savedProfiles.find { it.id == selectedProfileId }
            if (currentSelectedProfile != null) {
              IconButton(
                onClick = { viewModel.deleteSavedProfile(currentSelectedProfile) },
                modifier = Modifier
                  .size(42.dp)
                  .background(DangerRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
              ) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar plantilla", tint = DangerRed, modifier = Modifier.size(20.dp))
              }
            }
          }
        }
      }
    }

    // Mark and Height H
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = name,
        onValueChange = { viewModel.setBoxColName(it) },
        label = { Text("Marca / Nombre", fontSize = 11.sp, color = Slate400) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("box_col_name_input")
      )

      OutlinedTextField(
        value = hText,
        onValueChange = { viewModel.setBoxColH(it) },
        label = { Text("Peralte H (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("box_col_h_input")
      )
    }

    // Width B and Thickness t
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = bText,
        onValueChange = { viewModel.setBoxColB(it) },
        label = { Text("Ancho ala B (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("box_col_b_input")
      )

      OutlinedTextField(
        value = tText,
        onValueChange = { viewModel.setBoxColT(it) },
        label = { Text("Espesor t canal (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("box_col_t_input")
      )
    }

    // Length L and Quantity
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = lengthMm,
        onValueChange = { viewModel.setBoxColLengthMm(it) },
        label = { Text("Longitud L (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("box_col_length_input")
      )

      OutlinedTextField(
        value = quantity,
        onValueChange = { viewModel.setBoxColQuantity(it) },
        label = { Text("Cantidad (u)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("box_col_quantity_input")
      )
    }

    WeightResultCard("Peso lineal", "${unitWeightKgM.format(2)} kg/m", totalWeightKg)

    // Save template button
    OutlinedButton(
      onClick = { viewModel.saveBoxColumnTemplate() },
      modifier = Modifier
        .fillMaxWidth()
        .height(42.dp)
        .testTag("save_box_col_btn"),
      shape = RoundedCornerShape(8.dp),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberYellow),
      border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning)
    ) {
      Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Guardar dimensiones como plantilla de columna cajón", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColdFormedInputSection(viewModel: BeamViewModel) {
  val presets = BeamCatalog.coldFormedPresets
  val selectedPresetIndex by viewModel.cfPresetIndex.collectAsStateWithLifecycle()
  val name by viewModel.cfName.collectAsStateWithLifecycle()
  val hText by viewModel.cfH.collectAsStateWithLifecycle()
  val bText by viewModel.cfB.collectAsStateWithLifecycle()
  val cText by viewModel.cfC.collectAsStateWithLifecycle()
  val tText by viewModel.cfT.collectAsStateWithLifecycle()
  val lengthMm by viewModel.cfLengthMm.collectAsStateWithLifecycle()
  val quantity by viewModel.cfQuantity.collectAsStateWithLifecycle()

  val hVal = hText.toDoubleOrNull() ?: 0.0
  val bVal = bText.toDoubleOrNull() ?: 0.0
  val cVal = cText.toDoubleOrNull() ?: 0.0
  val tVal = tText.toDoubleOrNull() ?: 0.0
  val lVal = lengthMm.toDoubleOrNull() ?: 0.0
  val qtyVal = quantity.toIntOrNull() ?: 0

  val unitWeightKgM = BeamCatalog.calculateColdFormedWeightPerM(hVal, bVal, cVal, tVal)
  val totalWeightKg = unitWeightKgM * (lVal / 1000.0) * qtyVal

  var expandedPresetDropdown by remember { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    // Preset Selector Dropdown
    ExposedDropdownMenuBox(
      expanded = expandedPresetDropdown,
      onExpandedChange = { expandedPresetDropdown = it },
      modifier = Modifier.fillMaxWidth()
    ) {
      val currentPreset = presets.getOrElse(selectedPresetIndex) { presets[0] }
      val currentPresetWeight = BeamCatalog.calculateColdFormedWeightPerM(
        currentPreset.h, currentPreset.b, currentPreset.c, currentPreset.t
      )
      OutlinedTextField(
        value = "${currentPreset.label}  (${currentPresetWeight.format(2)} kg/m)",
        onValueChange = {},
        readOnly = true,
        label = { Text("Preconfiguración Comercial (Correa G / Canal U)", color = Slate400, fontSize = 12.sp) },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPresetDropdown) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .fillMaxWidth()
          .menuAnchor()
          .testTag("cf_preset_dropdown")
      )

      ExposedDropdownMenu(
        expanded = expandedPresetDropdown,
        onDismissRequest = { expandedPresetDropdown = false },
        modifier = Modifier.background(Slate800)
      ) {
        presets.forEachIndexed { idx, p ->
          val pWeight = BeamCatalog.calculateColdFormedWeightPerM(p.h, p.b, p.c, p.t)
          DropdownMenuItem(
            text = {
              Text(
                "${p.label}  ·  ${pWeight.format(2)} kg/m",
                color = if (idx == selectedPresetIndex) SkyBlueLight else Slate300,
                fontSize = 13.sp,
                fontWeight = if (idx == selectedPresetIndex) FontWeight.Bold else FontWeight.Normal
              )
            },
            onClick = {
              viewModel.selectCfPreset(idx)
              expandedPresetDropdown = false
            }
          )
        }
      }
    }

    // Mark Name and Height H
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = name,
        onValueChange = { viewModel.setCfName(it) },
        label = { Text("Marca / Nombre", fontSize = 11.sp, color = Slate400) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("cf_name_input")
      )

      OutlinedTextField(
        value = hText,
        onValueChange = { viewModel.setCfH(it) },
        label = { Text("Peralte H (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("cf_h_input")
      )
    }

    // Flange B and Lip C
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = bText,
        onValueChange = { viewModel.setCfB(it) },
        label = { Text("Ancho ala B (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("cf_b_input")
      )

      OutlinedTextField(
        value = cText,
        onValueChange = { viewModel.setCfC(it) },
        label = { Text("Pestaña C (mm) (0 para U)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("cf_c_input")
      )
    }

    // Thickness t and Length L
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = tText,
        onValueChange = { viewModel.setCfT(it) },
        label = { Text("Espesor t (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("cf_t_input")
      )

      OutlinedTextField(
        value = lengthMm,
        onValueChange = { viewModel.setCfLengthMm(it) },
        label = { Text("Longitud L (mm)", fontSize = 11.sp, color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = SteelBlue,
          unfocusedBorderColor = Slate600,
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("cf_length_input")
      )
    }

    // Quantity
    OutlinedTextField(
      value = quantity,
      onValueChange = { viewModel.setCfQuantity(it) },
      label = { Text("Cantidad (u)", fontSize = 11.sp, color = Slate400) },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = SteelBlue,
        unfocusedBorderColor = Slate600,
        focusedContainerColor = Slate900,
        unfocusedContainerColor = Slate900
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("cf_quantity_input")
    )

    WeightResultCard("Peso lineal", "${unitWeightKgM.format(2)} kg/m", totalWeightKg)
  }
}

@Composable
private fun MetricTile(
  label: String,
  value: String,
  textColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.height(60.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    shape = RoundedCornerShape(6.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Slate400,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = value,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun WeightResultCard(unitLabel: String, unitValue: String, totalWeightKg: Double) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SteelBlue.copy(alpha = 0.55f))
  ) {
    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text("PESO ESTIMADO", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        QueueDetail(unitLabel, unitValue, Modifier.weight(1f), SkyBlueLight)
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
          Text("TOTAL", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Medium)
          Text("${String.format(Locale.US, "%,.1f", totalWeightKg)} kg", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
      }
      HorizontalDivider(color = Slate700)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Carga para montaje", color = Slate400, fontSize = 11.sp)
        Text("${(totalWeightKg / 1000.0).format(3)} t", color = GreenTon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun BeamTableHeader(
  itemCount: Int,
  categories: List<String>,
  selectedCategory: String,
  onCategorySelected: (String) -> Unit,
  onClearClicked: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Lista de Montaje y Despacho",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Box(
          modifier = Modifier
            .background(SteelBlue.copy(alpha = 0.3f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "$itemCount",
            color = SkyBlueLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      TextButton(
        onClick = onClearClicked,
        colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
      ) {
        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Vaciar lista", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    // Category Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      categories.forEach { cat ->
        val isSelected = cat == selectedCategory
        FilterChip(
          selected = isSelected,
          onClick = { onCategorySelected(cat) },
          label = {
            Text(
              text = cat,
              fontSize = 11.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SteelBlue,
            selectedLabelColor = Color.White,
            containerColor = Slate900,
            labelColor = Slate400
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) SteelBlue else Slate700,
            selectedBorderColor = SteelBlue
          )
        )
      }
    }
  }
}

@Composable
private fun EmptyQueueState(hasTotalItems: Boolean = false) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Slate800),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Construction,
        contentDescription = null,
        tint = Slate600,
        modifier = Modifier.size(48.dp)
      )
      Text(
        text = if (hasTotalItems) "No hay elementos en esta categoría." else "No hay elementos en la lista de despacho.",
        color = Slate300,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
      )
      Text(
        text = if (hasTotalItems) "Cambia el filtro de categoría o añade elementos en esta pestaña." else "Selecciona un perfil o ingresa dimensiones en las pestañas superiores para comenzar.",
        color = Slate500,
        fontSize = 12.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun BeamHorizontalTableView(
  items: List<BeamItemEntity>,
  onDeleteItem: (BeamItemEntity) -> Unit,
  showTotals: Boolean = true
) {
  val totalQuantity = items.sumOf { it.quantity }
  val totalLengthM = items.filterNot {
    it.category.contains("placa", ignoreCase = true) || it.type.contains("placa", ignoreCase = true)
  }.sumOf { (it.referenceLengthMm * it.quantity) / 1000.0 }
  val totalKg = items.sumOf { it.totalWeightKg }

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    items.forEachIndexed { index, item ->
      val isPlate = item.category.contains("placa", ignoreCase = true) || item.type.contains("placa", ignoreCase = true)
      val categoryColor = when (item.category) {
        "Viga Principal" -> Color(0xFF3B82F6)
        "Viga Secundaria" -> Color(0xFF14B8A6)
        "Columna" -> Color(0xFFF59E0B)
        "Vigas especiales" -> Color(0xFF0F766E)
        else -> Color(0xFFA855F7)
      }
      val specialLengths = listOfNotNull(
        item.topFlangeLengthMm?.let { "Sup. ${it.format()} mm" },
        item.webLengthMm?.let { "Alma ${it.format()} mm" },
        item.bottomFlangeLengthMm?.let { "Inf. ${it.format()} mm" }
      ).joinToString(" · ")

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
              Box(
                modifier = Modifier
                  .background(categoryColor.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                  .border(1.dp, categoryColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                  .padding(horizontal = 7.dp, vertical = 2.dp)
              ) {
                Text(item.category.ifEmpty { "General" }, color = categoryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
              }
              Text(item.name, modifier = Modifier.weight(1f), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
              }
              Text("${item.type} · ${item.dims}", color = Slate400, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { onDeleteItem(item) }, modifier = Modifier.size(34.dp)) {
              Icon(Icons.Default.Delete, contentDescription = "Eliminar ${item.name}", tint = DangerRed, modifier = Modifier.size(18.dp))
            }
          }

          HorizontalDivider(color = Slate700, thickness = 0.7.dp)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${item.quantity} u", color = Slate300, fontSize = 10.sp)
            Text("Alma ${if (item.hw > 0.0) "${item.hw.format(1)} mm" else "—"}", color = AmberYellow, fontSize = 10.sp)
            Text(if (isPlate) "Placa" else "${((item.referenceLengthMm * item.quantity) / 1000.0).format(2)} m total", color = SkyBlueLight, fontSize = 10.sp)
          }
          if (specialLengths.isNotBlank()) {
            Text("Sup. · Alma · Inf.: $specialLengths", color = Slate300, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
          } else if (!isPlate) {
            Text("${item.lengthMm.format()} mm por pieza", color = Slate400, fontSize = 10.sp)
          }
          if (item.hasDestaje) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
              Icon(Icons.Default.ContentCut, contentDescription = null, tint = AmberYellow, modifier = Modifier.size(13.dp))
              Text(item.destajeInfo, color = AmberYellow, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${item.unitWeightKgM.format(2)} ${if (isPlate) "kg/u" else "kg/m"}", color = Slate400, fontSize = 10.sp)
            Text("${String.format(Locale.US, "%,.1f", item.totalWeightKg)} kg", color = SkyBlueLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${item.totalWeightTon.format(3)} t", color = GreenTon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    if (showTotals) Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = Slate950),
      shape = RoundedCornerShape(12.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SteelBlue.copy(alpha = 0.55f))
    ) {
      Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("TOTAL DE LA LISTA", color = AmberYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          QueueDetail("Piezas", "$totalQuantity", Modifier.weight(1f), Color.White)
          QueueDetail("Longitud", "${totalLengthM.format(2)} m", Modifier.weight(1f), SkyBlueLight)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          QueueDetail("Peso", "${String.format(Locale.US, "%,.1f", totalKg)} kg", Modifier.weight(1f), SkyBlueLight)
          QueueDetail("Carga", "${(totalKg / 1000.0).format(3)} t", Modifier.weight(1f), GreenTon)
        }
      }
    }
  }
}

@Composable
private fun BeamQueueTotals(items: List<BeamItemEntity>) {
  val totalQuantity = items.sumOf { it.quantity }
  val totalLengthM = items.filterNot {
    it.category.contains("placa", ignoreCase = true) || it.type.contains("placa", ignoreCase = true)
  }.sumOf { (it.referenceLengthMm * it.quantity) / 1000.0 }
  val totalKg = items.sumOf { it.totalWeightKg }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Slate950),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SteelBlue.copy(alpha = 0.55f))
  ) {
    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text("TOTAL DE LA LISTA", color = AmberYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QueueDetail("Piezas", "$totalQuantity", Modifier.weight(1f), Color.White)
        QueueDetail("Longitud", "${totalLengthM.format(2)} m", Modifier.weight(1f), SkyBlueLight)
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QueueDetail("Peso", "${String.format(Locale.US, "%,.1f", totalKg)} kg", Modifier.weight(1f), SkyBlueLight)
        QueueDetail("Carga", "${(totalKg / 1000.0).format(3)} t", Modifier.weight(1f), GreenTon)
      }
    }
  }
}

@Composable
private fun QueueDetail(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  valueColor: Color = Slate300
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
    Text(label, color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun TableCell(
  text: String,
  width: androidx.compose.ui.unit.Dp,
  isHeader: Boolean = false,
  fontWeight: FontWeight = FontWeight.Normal,
  textColor: Color = if (isHeader) Slate400 else Slate300
) {
  Text(
    text = text,
    modifier = Modifier.width(width),
    color = textColor,
    fontSize = if (isHeader) 11.sp else 12.sp,
    fontWeight = if (isHeader) FontWeight.Bold else fontWeight,
    textAlign = TextAlign.Center,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis
  )
}

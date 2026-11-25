package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BeamDatabase
import com.example.data.BeamItemEntity
import com.example.data.BeamRepository
import com.example.data.SavedProfileEntity
import com.example.model.BeamCatalog
import com.example.model.CommercialProfile
import com.example.model.format
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class BeamSummary(
  val totalPieces: Int = 0,
  val totalLengthM: Double = 0.0,
  val totalWeightKg: Double = 0.0,
  val totalWeightTon: Double = 0.0
)

class BeamViewModel(
  application: Application,
  private val repository: BeamRepository
) : AndroidViewModel(application) {

  val beamItems: StateFlow<List<BeamItemEntity>> = repository.allBeamItems
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val savedProfiles: StateFlow<List<SavedProfileEntity>> = repository.allSavedProfiles
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val summary: StateFlow<BeamSummary> = beamItems.combine(MutableStateFlow(Unit)) { items, _ ->
    var pieces = 0
    var lengthM = 0.0
    var kg = 0.0
    for (item in items) {
      pieces += item.quantity
      lengthM += (item.referenceLengthMm * item.quantity) / 1000.0
      kg += item.totalWeightKg
    }
    BeamSummary(
      totalPieces = pieces,
      totalLengthM = lengthM,
      totalWeightKg = kg,
      totalWeightTon = kg / 1000.0
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = BeamSummary()
  )

  // Tab State: 0 = V. Principales, 1 = V. Secundarias, 2 = Columnas, 3 = Otros, 4 = Placas, 5 = Vigas especiales
  val tabTitles = listOf("V. Principales", "V. Secundarias", "Columnas", "Otros", "Placas", "Vigas especiales")

  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  // Profile sub-mode:
  // For tab 0 (V. Principales): 0 = Perfil Comercial, 1 = Vigas Armadas (Placas)
  // For tab 1 (V. Secundarias): 0 = Perfil Comercial, 1 = Vigas Armadas (Placas)
  // For tab 2 (Columnas): 0 = Catálogo Comercial (Wide Flange & HSS), 1 = Columna Armada (2 Canales U)
  // For tab 3 (Otros): 0 = Perfiles Conformados en Frío (Correas G / Canales U)
  // For tab 4 (Placas): 0 = Placas y Cartelas de Acero
  private val _profileSubMode = MutableStateFlow(0)
  val profileSubMode: StateFlow<Int> = _profileSubMode.asStateFlow()

  fun setProfileSubMode(mode: Int) {
    _profileSubMode.value = mode
  }

  fun setTab(index: Int) {
    if (index in tabTitles.indices) {
      _selectedTab.value = index
      // Reset sub-mode if appropriate
      _profileSubMode.value = 0
      val currentName = _elementName.value
      val defaultPrefixes = listOf("VP-", "VS-", "COL-", "CORREA-", "CANAL-", "PB-", "PL-", "VE-")
      val isDefault = currentName.isEmpty() || defaultPrefixes.any { currentName.startsWith(it) }
      if (isDefault) {
        _elementName.value = when (index) {
          0 -> "VP-1"
          1 -> "VS-1"
          2 -> "COL-1"
          3 -> "CORREA-1"
          4 -> "PB-1"
          5 -> "VE-1"
          else -> "ELEM-1"
        }
      }
      if (index == 5 && _armName.value == "V-1") {
        _armName.value = "VE-1"
      }
    }
  }

  // Common Element Mark
  private val _elementName = MutableStateFlow("VP-1")
  val elementName: StateFlow<String> = _elementName.asStateFlow()
  fun setElementName(name: String) { _elementName.value = name }

  // Commercial Form State
  // Default to IPE 400 (index 13 in BeamCatalog.profiles)
  private val defaultIpe400Index = BeamCatalog.profiles.indexOfFirst { it.name == "IPE 400" }.let { if (it >= 0) it else 0 }
  private val _selectedCommercialIndex = MutableStateFlow(defaultIpe400Index)
  val selectedCommercialIndex: StateFlow<Int> = _selectedCommercialIndex.asStateFlow()

  private val _comLengthMm = MutableStateFlow("12000")
  val comLengthMm: StateFlow<String> = _comLengthMm.asStateFlow()

  private val _comQuantity = MutableStateFlow("1")
  val comQuantity: StateFlow<String> = _comQuantity.asStateFlow()

  fun setCommercialProfileIndex(index: Int) {
    if (index in BeamCatalog.profiles.indices) {
      _selectedCommercialIndex.value = index
    }
  }

  fun setComLengthMm(value: String) {
    _comLengthMm.value = value
  }

  fun setComQuantity(value: String) {
    _comQuantity.value = value
  }

  // Vigas Armadas Form State
  private val _selectedSavedProfileId = MutableStateFlow<Long?>(null)
  val selectedSavedProfileId: StateFlow<Long?> = _selectedSavedProfileId.asStateFlow()

  private val _armName = MutableStateFlow("V-1")
  val armName: StateFlow<String> = _armName.asStateFlow()

  private val _armH = MutableStateFlow("400")
  val armH: StateFlow<String> = _armH.asStateFlow()

  private val _armB = MutableStateFlow("180")
  val armB: StateFlow<String> = _armB.asStateFlow()

  private val _armTf = MutableStateFlow("14")
  val armTf: StateFlow<String> = _armTf.asStateFlow()

  private val _armTw = MutableStateFlow("8")
  val armTw: StateFlow<String> = _armTw.asStateFlow()

  private val _armR = MutableStateFlow("0")
  val armR: StateFlow<String> = _armR.asStateFlow()

  private val _armLengthMm = MutableStateFlow("12000")
  val armLengthMm: StateFlow<String> = _armLengthMm.asStateFlow()

  private val _armQuantity = MutableStateFlow("1")
  val armQuantity: StateFlow<String> = _armQuantity.asStateFlow()

  fun setArmName(v: String) { _armName.value = v }
  fun setArmH(v: String) { _armH.value = v }
  fun setArmB(v: String) { _armB.value = v }
  fun setArmTf(v: String) { _armTf.value = v }
  fun setArmTw(v: String) { _armTw.value = v }
  fun setArmR(v: String) { _armR.value = v }
  fun setArmLengthMm(v: String) { _armLengthMm.value = v }
  fun setArmQuantity(v: String) { _armQuantity.value = v }

  // Special beam lengths are measured independently for each steel plate.
  private val _specialTopLengthMm = MutableStateFlow("4000")
  val specialTopLengthMm: StateFlow<String> = _specialTopLengthMm.asStateFlow()
  private val _specialWebLengthMm = MutableStateFlow("4500")
  val specialWebLengthMm: StateFlow<String> = _specialWebLengthMm.asStateFlow()
  private val _specialBottomLengthMm = MutableStateFlow("5000")
  val specialBottomLengthMm: StateFlow<String> = _specialBottomLengthMm.asStateFlow()
  fun setSpecialTopLengthMm(v: String) { _specialTopLengthMm.value = v }
  fun setSpecialWebLengthMm(v: String) { _specialWebLengthMm.value = v }
  fun setSpecialBottomLengthMm(v: String) { _specialBottomLengthMm.value = v }

  // DESTAJE FIELDS (Cortes de destaje en alas y alma)
  private val _hasDestaje = MutableStateFlow(false)
  val hasDestaje: StateFlow<Boolean> = _hasDestaje.asStateFlow()

  private val _destajeLongMm = MutableStateFlow("80")
  val destajeLongMm: StateFlow<String> = _destajeLongMm.asStateFlow()

  private val _destajeAltMm = MutableStateFlow("35")
  val destajeAltMm: StateFlow<String> = _destajeAltMm.asStateFlow()

  private val _destajeTipo = MutableStateFlow("2 Extremos (Superior)")
  val destajeTipo: StateFlow<String> = _destajeTipo.asStateFlow()

  fun setHasDestaje(v: Boolean) { _hasDestaje.value = v }
  fun setDestajeLongMm(v: String) { _destajeLongMm.value = v }
  fun setDestajeAltMm(v: String) { _destajeAltMm.value = v }
  fun setDestajeTipo(v: String) { _destajeTipo.value = v }

  // PLACAS FIELDS (Para Tab 4: Placas)
  private val _placaEspesorMm = MutableStateFlow("12")
  val placaEspesorMm: StateFlow<String> = _placaEspesorMm.asStateFlow()

  private val _placaAnchoMm = MutableStateFlow("250")
  val placaAnchoMm: StateFlow<String> = _placaAnchoMm.asStateFlow()

  private val _placaLargoMm = MutableStateFlow("400")
  val placaLargoMm: StateFlow<String> = _placaLargoMm.asStateFlow()

  private val _placaQuantity = MutableStateFlow("4")
  val placaQuantity: StateFlow<String> = _placaQuantity.asStateFlow()

  private val _placaName = MutableStateFlow("PB-1")
  val placaName: StateFlow<String> = _placaName.asStateFlow()

  fun setPlacaEspesorMm(v: String) { _placaEspesorMm.value = v }
  fun setPlacaAnchoMm(v: String) { _placaAnchoMm.value = v }
  fun setPlacaLargoMm(v: String) { _placaLargoMm.value = v }
  fun setPlacaQuantity(v: String) { _placaQuantity.value = v }
  fun setPlacaName(v: String) { _placaName.value = v }

  // COLUMNA ARMADA (2 Canales U en cajón) - Sub-tab B in Tab 2: Columnas
  private val _boxColName = MutableStateFlow("COL-CAJON 1")
  val boxColName: StateFlow<String> = _boxColName.asStateFlow()
  fun setBoxColName(v: String) { _boxColName.value = v }

  private val _boxColH = MutableStateFlow("200")
  val boxColH: StateFlow<String> = _boxColH.asStateFlow()
  fun setBoxColH(v: String) { _boxColH.value = v }

  private val _boxColB = MutableStateFlow("75")
  val boxColB: StateFlow<String> = _boxColB.asStateFlow()
  fun setBoxColB(v: String) { _boxColB.value = v }

  private val _boxColT = MutableStateFlow("4.0")
  val boxColT: StateFlow<String> = _boxColT.asStateFlow()
  fun setBoxColT(v: String) { _boxColT.value = v }

  private val _boxColLengthMm = MutableStateFlow("6000")
  val boxColLengthMm: StateFlow<String> = _boxColLengthMm.asStateFlow()
  fun setBoxColLengthMm(v: String) { _boxColLengthMm.value = v }

  private val _boxColQuantity = MutableStateFlow("1")
  val boxColQuantity: StateFlow<String> = _boxColQuantity.asStateFlow()
  fun setBoxColQuantity(v: String) { _boxColQuantity.value = v }

  // OTROS (Correas G y Canales U conformados en frío) - Tab 3: Otros
  private val _cfPresetIndex = MutableStateFlow(0)
  val cfPresetIndex: StateFlow<Int> = _cfPresetIndex.asStateFlow()

  private val _cfName = MutableStateFlow("CORREA-1")
  val cfName: StateFlow<String> = _cfName.asStateFlow()
  fun setCfName(v: String) { _cfName.value = v }

  private val _cfH = MutableStateFlow("100")
  val cfH: StateFlow<String> = _cfH.asStateFlow()
  fun setCfH(v: String) { _cfH.value = v }

  private val _cfB = MutableStateFlow("50")
  val cfB: StateFlow<String> = _cfB.asStateFlow()
  fun setCfB(v: String) { _cfB.value = v }

  private val _cfC = MutableStateFlow("15")
  val cfC: StateFlow<String> = _cfC.asStateFlow()
  fun setCfC(v: String) { _cfC.value = v }

  private val _cfT = MutableStateFlow("2.0")
  val cfT: StateFlow<String> = _cfT.asStateFlow()
  fun setCfT(v: String) { _cfT.value = v }

  private val _cfLengthMm = MutableStateFlow("6000")
  val cfLengthMm: StateFlow<String> = _cfLengthMm.asStateFlow()
  fun setCfLengthMm(v: String) { _cfLengthMm.value = v }

  private val _cfQuantity = MutableStateFlow("1")
  val cfQuantity: StateFlow<String> = _cfQuantity.asStateFlow()
  fun setCfQuantity(v: String) { _cfQuantity.value = v }

  fun selectCfPreset(index: Int) {
    if (index in BeamCatalog.coldFormedPresets.indices) {
      _cfPresetIndex.value = index
      val p = BeamCatalog.coldFormedPresets[index]
      _cfH.value = p.h.format()
      _cfB.value = p.b.format()
      _cfC.value = p.c.format()
      _cfT.value = p.t.format()
    }
  }

  fun loadSavedProfile(profile: SavedProfileEntity) {
    _selectedSavedProfileId.value = profile.id
    if (profile.profileType == "BOX_COLUMN") {
      _boxColName.value = profile.name
      _boxColH.value = profile.h.format()
      _boxColB.value = profile.b.format()
      _boxColT.value = profile.tf.format()
    } else {
      _armName.value = profile.name
      _armH.value = profile.h.format()
      _armB.value = profile.b.format()
      _armTf.value = profile.tf.format()
      _armTw.value = profile.tw.format()
      _armR.value = profile.r.format()
    }
  }

  fun clearSelectedSavedProfile() {
    _selectedSavedProfileId.value = null
  }

  // Toast / User Notification Event
  private val _userMessage = MutableSharedFlow<String>()
  val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

  private fun notify(message: String) {
    viewModelScope.launch {
      _userMessage.emit(message)
    }
  }

  // Universal Add Element for the active tab (5 Tabs)
  fun addElement() {
    when (_selectedTab.value) {
      0 -> addVigaPrincipal()
      1 -> addVigaSecundaria()
      2 -> addColumna()
      3 -> addOtros()
      4 -> addPlaca()
      5 -> addSpecialBeam()
    }
  }

  private fun addSpecialBeam() {
    val isCommercial = _profileSubMode.value == 0
    val commercialProfile = if (isCommercial) BeamCatalog.profiles.getOrNull(_selectedCommercialIndex.value) else null
    val name = if (isCommercial) _elementName.value.trim().ifEmpty { "VE-1" } else _armName.value.trim().ifEmpty { "VE-1" }
    val h = commercialProfile?.h ?: _armH.value.toDoubleOrNull()
    val b = commercialProfile?.b ?: _armB.value.toDoubleOrNull()
    val tf = commercialProfile?.tf ?: _armTf.value.toDoubleOrNull()
    val tw = commercialProfile?.tw ?: _armTw.value.toDoubleOrNull()
    val topLength = _specialTopLengthMm.value.toDoubleOrNull()
    val webLength = _specialWebLengthMm.value.toDoubleOrNull()
    val bottomLength = _specialBottomLengthMm.value.toDoubleOrNull()
    val quantity = (if (isCommercial) _comQuantity.value else _armQuantity.value).toIntOrNull()

    if (h == null || h <= 0.0 || b == null || b <= 0.0 || tf == null || tf <= 0.0 ||
      tw == null || tw <= 0.0 || h <= 2.0 * tf ||
      topLength == null || topLength <= 0.0 || webLength == null || webLength <= 0.0 ||
      bottomLength == null || bottomLength <= 0.0 || quantity == null || quantity <= 0
    ) {
      notify("Verifica las dimensiones, las tres longitudes y la cantidad de la viga especial")
      return
    }

    val hasDestaje = _hasDestaje.value
    val destajeLong = if (hasDestaje) _destajeLongMm.value.toDoubleOrNull() else 0.0
    val destajeAlt = if (hasDestaje) _destajeAltMm.value.toDoubleOrNull() else 0.0
    if (hasDestaje && (destajeLong == null || destajeLong <= 0.0 || destajeAlt == null || destajeAlt <= 0.0)) {
      notify("Ingresa una longitud y altura válidas para el destaje")
      return
    }

    val referenceLength = maxOf(topLength, webLength, bottomLength)
    val plateGeometryWeightKg = BeamCatalog.calculateSpecialBeamWeightKg(
      h = h,
      b = b,
      tf = tf,
      tw = tw,
      topFlangeLengthMm = topLength,
      webLengthMm = webLength,
      bottomFlangeLengthMm = bottomLength
    )
    // For catalog sections, calibrate the plate estimate to the published kg/m value.
    // Equal plate lengths therefore reproduce the commercial profile's catalog weight.
    val catalogScale = commercialProfile?.let { profile ->
      val geometricKgPerM = BeamCatalog.calculateArmadaWeightPerM(profile.h, profile.b, profile.tf, profile.tw, 0.0)
      if (geometricKgPerM > 0.0) profile.weightKgPerM / geometricKgPerM else 1.0
    } ?: 1.0
    val beamWeightKg = plateGeometryWeightKg * catalogScale
    val grossWeight = beamWeightKg * quantity
    val destajeDeduction = if (hasDestaje) BeamCatalog.calculateNotchDeductionKg(
      b = b,
      tw = tw,
      tf = tf,
      ldMm = destajeLong ?: 0.0,
      notchType = _destajeTipo.value,
      quantity = quantity
    ) else 0.0
    val netWeight = (grossWeight - destajeDeduction).coerceAtLeast(0.0)
    val hw = BeamCatalog.calculateArmadaHw(h, tf)
    val dims = commercialProfile?.let { "${it.name} (${h.format()}x${b.format()}x${tf.format()}x${tw.format()})" }
      ?: "${h.format()}x${b.format()}x${tf.format()}x${tw.format()} mm"
    val item = BeamItemEntity(
      type = if (isCommercial) "Viga especial · Comercial" else "Viga especial · Armada",
      category = "Vigas especiales",
      name = name,
      h = h,
      hw = hw,
      b = b,
      tf = tf,
      tw = tw,
      r = 0.0,
      dims = dims,
      lengthMm = referenceLength,
      quantity = quantity,
      unitWeightKgM = netWeight / (referenceLength / 1000.0),
      totalWeightKg = netWeight,
      totalWeightTon = netWeight / 1000.0,
      grossWeightKg = grossWeight,
      hasDestaje = hasDestaje,
      destajeLongMm = destajeLong ?: 0.0,
      destajeAltMm = destajeAlt ?: 0.0,
      destajeTipo = if (hasDestaje) _destajeTipo.value else "",
      destajeDetails = if (hasDestaje) "${_destajeTipo.value} ${destajeLong?.format()}x${destajeAlt?.format()} mm" else "",
      notchDeductionKg = destajeDeduction,
      topFlangeLengthMm = topLength,
      webLengthMm = webLength,
      bottomFlangeLengthMm = bottomLength
    )

    viewModelScope.launch {
      repository.insertBeamItem(item)
      val destajeMessage = if (destajeDeduction > 0.0) " · destaje −${String.format(Locale.US, "%.1f", destajeDeduction)} kg" else ""
      notify("Viga especial '$name' añadida · ${String.format(Locale.US, "%.1f", netWeight)} kg$destajeMessage")
    }
  }

  private fun addVigaPrincipal() {
    val categoryName = "Viga Principal"
    val length = if (_profileSubMode.value == 0) _comLengthMm.value.toDoubleOrNull() else _armLengthMm.value.toDoubleOrNull()
    val qty = if (_profileSubMode.value == 0) _comQuantity.value.toIntOrNull() else _armQuantity.value.toIntOrNull()

    if (length == null || length <= 0.0 || qty == null || qty <= 0) {
      notify("Ingresa una longitud y cantidad válidas (mayores a 0)")
      return
    }

    if (_profileSubMode.value == 0) {
      val p = BeamCatalog.profiles.getOrNull(_selectedCommercialIndex.value) ?: return
      val mark = _elementName.value.trim().ifEmpty { "VP-${p.name}" }
      val unitWeight = p.weightKgPerM
      val totalWeightKg = unitWeight * (length / 1000.0) * qty
      val totalWeightTon = totalWeightKg / 1000.0

      val item = BeamItemEntity(
        type = "Comercial",
        category = categoryName,
        name = mark,
        h = p.h,
        hw = p.hw,
        b = p.b,
        tf = p.tf,
        tw = p.tw,
        r = 0.0,
        dims = "${p.name} (${p.h.format()}x${p.b.format()}x${p.tf.format()}x${p.tw.format()})",
        lengthMm = length,
        quantity = qty,
        unitWeightKgM = unitWeight,
        totalWeightKg = totalWeightKg,
        totalWeightTon = totalWeightTon,
        grossWeightKg = totalWeightKg,
        notchDeductionKg = 0.0,
        hasDestaje = false
      )
      viewModelScope.launch {
        repository.insertBeamItem(item)
        notify("Viga Principal '$mark' añadida")
      }
    } else {
      val mark = _armName.value.trim().ifEmpty { "VP-ARM" }
      val h = _armH.value.toDoubleOrNull()
      val b = _armB.value.toDoubleOrNull()
      val tf = _armTf.value.toDoubleOrNull()
      val tw = _armTw.value.toDoubleOrNull()
      val r = _armR.value.toDoubleOrNull() ?: 0.0

      if (h == null || h <= 0.0 || b == null || b <= 0.0 || tf == null || tf <= 0.0 || tw == null || tw <= 0.0) {
        notify("Verifica que las dimensiones del perfil armado sean mayores a 0")
        return
      }

      val hw = BeamCatalog.calculateArmadaHw(h, tf)
      val unitWeight = BeamCatalog.calculateArmadaWeightPerM(h, b, tf, tw, r)
      val totalWeightKg = unitWeight * (length / 1000.0) * qty
      val totalWeightTon = totalWeightKg / 1000.0
      val rPart = if (r > 0.0) " r=${r.format()}" else ""
      val dimsStr = "${h.format()}x${b.format()}x${tf.format()}x${tw.format()}$rPart"

      val item = BeamItemEntity(
        type = "Armada",
        category = categoryName,
        name = mark,
        h = h,
        hw = hw,
        b = b,
        tf = tf,
        tw = tw,
        r = r,
        dims = dimsStr,
        lengthMm = length,
        quantity = qty,
        unitWeightKgM = unitWeight,
        totalWeightKg = totalWeightKg,
        totalWeightTon = totalWeightTon,
        grossWeightKg = totalWeightKg,
        notchDeductionKg = 0.0,
        hasDestaje = false
      )
      viewModelScope.launch {
        repository.insertBeamItem(item)
        notify("Viga Principal Armada '$mark' añadida")
      }
    }
  }

  private fun addVigaSecundaria() {
    val categoryName = "Viga Secundaria"
    val length = if (_profileSubMode.value == 0) _comLengthMm.value.toDoubleOrNull() else _armLengthMm.value.toDoubleOrNull()
    val qty = if (_profileSubMode.value == 0) _comQuantity.value.toIntOrNull() else _armQuantity.value.toIntOrNull()

    if (length == null || length <= 0.0 || qty == null || qty <= 0) {
      notify("Ingresa una longitud y cantidad válidas (mayores a 0)")
      return
    }

    val withDestaje = _hasDestaje.value
    val dLong = if (withDestaje) (_destajeLongMm.value.toDoubleOrNull() ?: 0.0) else 0.0
    val dAlt = if (withDestaje) (_destajeAltMm.value.toDoubleOrNull() ?: 0.0) else 0.0
    val dTipo = if (withDestaje) _destajeTipo.value else ""

    if (_profileSubMode.value == 0) {
      val p = BeamCatalog.profiles.getOrNull(_selectedCommercialIndex.value) ?: return
      val mark = _elementName.value.trim().ifEmpty { "VS-${p.name}" }
      val unitWeight = p.weightKgPerM
      val grossWeight = unitWeight * (length / 1000.0) * qty

      val notchDeduction = if (withDestaje && dLong > 0.0) {
        BeamCatalog.calculateNotchDeductionKg(
          b = p.b,
          tw = p.tw,
          tf = p.tf,
          ldMm = dLong,
          notchType = dTipo,
          quantity = qty
        )
      } else 0.0

      val netWeight = kotlin.math.max(0.0, grossWeight - notchDeduction)
      val destDetails = if (withDestaje) {
        "$dTipo ${dLong.toInt()}x${dAlt.toInt()} mm"
      } else ""

      val item = BeamItemEntity(
        type = "Comercial",
        category = categoryName,
        name = mark,
        h = p.h,
        hw = p.hw,
        b = p.b,
        tf = p.tf,
        tw = p.tw,
        r = 0.0,
        dims = "${p.name} (${p.h.format()}x${p.b.format()}x${p.tf.format()}x${p.tw.format()})",
        lengthMm = length,
        quantity = qty,
        unitWeightKgM = unitWeight,
        totalWeightKg = netWeight,
        totalWeightTon = netWeight / 1000.0,
        grossWeightKg = grossWeight,
        notchDeductionKg = notchDeduction,
        hasDestaje = withDestaje,
        destajeLongMm = dLong,
        destajeAltMm = dAlt,
        destajeTipo = dTipo,
        destajeDetails = destDetails
      )
      viewModelScope.launch {
        repository.insertBeamItem(item)
        val note = if (withDestaje) " (destaje: -${String.format(Locale.US, "%.1f", notchDeduction)} kg)" else ""
        notify("Viga Secundaria '$mark' añadida$note")
      }
    } else {
      val mark = _armName.value.trim().ifEmpty { "VS-ARM" }
      val h = _armH.value.toDoubleOrNull()
      val b = _armB.value.toDoubleOrNull()
      val tf = _armTf.value.toDoubleOrNull()
      val tw = _armTw.value.toDoubleOrNull()
      val r = _armR.value.toDoubleOrNull() ?: 0.0

      if (h == null || h <= 0.0 || b == null || b <= 0.0 || tf == null || tf <= 0.0 || tw == null || tw <= 0.0) {
        notify("Verifica que las dimensiones del perfil armado sean mayores a 0")
        return
      }

      val hw = BeamCatalog.calculateArmadaHw(h, tf)
      val unitWeight = BeamCatalog.calculateArmadaWeightPerM(h, b, tf, tw, r)
      val grossWeight = unitWeight * (length / 1000.0) * qty

      val notchDeduction = if (withDestaje && dLong > 0.0) {
        BeamCatalog.calculateNotchDeductionKg(
          b = b,
          tw = tw,
          tf = tf,
          ldMm = dLong,
          notchType = dTipo,
          quantity = qty
        )
      } else 0.0

      val netWeight = kotlin.math.max(0.0, grossWeight - notchDeduction)
      val destDetails = if (withDestaje) {
        "$dTipo ${dLong.toInt()}x${dAlt.toInt()} mm"
      } else ""

      val rPart = if (r > 0.0) " r=${r.format()}" else ""
      val dimsStr = "${h.format()}x${b.format()}x${tf.format()}x${tw.format()}$rPart"

      val item = BeamItemEntity(
        type = "Armada",
        category = categoryName,
        name = mark,
        h = h,
        hw = hw,
        b = b,
        tf = tf,
        tw = tw,
        r = r,
        dims = dimsStr,
        lengthMm = length,
        quantity = qty,
        unitWeightKgM = unitWeight,
        totalWeightKg = netWeight,
        totalWeightTon = netWeight / 1000.0,
        grossWeightKg = grossWeight,
        notchDeductionKg = notchDeduction,
        hasDestaje = withDestaje,
        destajeLongMm = dLong,
        destajeAltMm = dAlt,
        destajeTipo = dTipo,
        destajeDetails = destDetails
      )
      viewModelScope.launch {
        repository.insertBeamItem(item)
        val note = if (withDestaje) " (destaje: -${String.format(Locale.US, "%.1f", notchDeduction)} kg)" else ""
        notify("Viga Secundaria Armada '$mark' añadida$note")
      }
    }
  }

  private fun addColumna() {
    val categoryName = "Columna"
    if (_profileSubMode.value == 0) {
      // Sub-tab A: Catálogo Comercial (Wide Flange & HSS)
      val length = _comLengthMm.value.toDoubleOrNull()
      val qty = _comQuantity.value.toIntOrNull()
      if (length == null || length <= 0.0 || qty == null || qty <= 0) {
        notify("Ingresa una longitud y cantidad válidas")
        return
      }

      val p = BeamCatalog.profiles.getOrNull(_selectedCommercialIndex.value) ?: return
      val mark = _elementName.value.trim().ifEmpty { "COL-${p.name}" }
      val unitWeight = p.weightKgPerM
      val totalWeightKg = unitWeight * (length / 1000.0) * qty

      val item = BeamItemEntity(
        type = "Comercial",
        category = categoryName,
        name = mark,
        h = p.h,
        hw = p.hw,
        b = p.b,
        tf = p.tf,
        tw = p.tw,
        r = 0.0,
        dims = "${p.name} (${p.h.format()}x${p.b.format()}x${p.tf.format()}x${p.tw.format()})",
        lengthMm = length,
        quantity = qty,
        unitWeightKgM = unitWeight,
        totalWeightKg = totalWeightKg,
        totalWeightTon = totalWeightKg / 1000.0,
        grossWeightKg = totalWeightKg,
        notchDeductionKg = 0.0,
        hasDestaje = false
      )
      viewModelScope.launch {
        repository.insertBeamItem(item)
        notify("Columna '$mark' añadida")
      }
    } else {
      // Sub-tab B: Columna Armada (2 Canales U)
      val name = _boxColName.value.trim().ifEmpty { "COL-CAJON 1" }
      val h = _boxColH.value.toDoubleOrNull()
      val b = _boxColB.value.toDoubleOrNull()
      val t = _boxColT.value.toDoubleOrNull()
      val length = _boxColLengthMm.value.toDoubleOrNull()
      val qty = _boxColQuantity.value.toIntOrNull()

      if (h == null || h <= 0.0 || b == null || b <= 0.0 || t == null || t <= 0.0 || length == null || length <= 0.0 || qty == null || qty <= 0) {
        notify("Verifica que las dimensiones H, B, t, longitud y cantidad de la columna cajón sean mayores a 0")
        return
      }

      val unitWeight = BeamCatalog.calculateBoxColumnWeightPerM(h, b, t)
      val totalWeightKg = unitWeight * (length / 1000.0) * qty

      val item = BeamItemEntity(
        type = "Columna Cajón",
        category = categoryName,
        name = name,
        h = h,
        hw = kotlin.math.max(0.0, h - 2.0 * t),
        b = b,
        tf = t,
        tw = t,
        r = 0.0,
        dims = "2 Canales U ${h.toInt()}x${b.toInt()}x${t.format()} mm (Cajón)",
        lengthMm = length,
        quantity = qty,
        unitWeightKgM = unitWeight,
        totalWeightKg = totalWeightKg,
        totalWeightTon = totalWeightKg / 1000.0,
        grossWeightKg = totalWeightKg,
        notchDeductionKg = 0.0,
        hasDestaje = false
      )
      viewModelScope.launch {
        repository.insertBeamItem(item)
        notify("Columna Cajón '$name' añadida")
      }
    }
  }

  private fun addOtros() {
    val categoryName = "Correa / Canal"
    val name = _cfName.value.trim().ifEmpty { "CORREA-1" }
    val h = _cfH.value.toDoubleOrNull()
    val b = _cfB.value.toDoubleOrNull()
    val c = _cfC.value.toDoubleOrNull() ?: 0.0
    val t = _cfT.value.toDoubleOrNull()
    val length = _cfLengthMm.value.toDoubleOrNull()
    val qty = _cfQuantity.value.toIntOrNull()

    if (h == null || h <= 0.0 || b == null || b <= 0.0 || t == null || t <= 0.0 || length == null || length <= 0.0 || qty == null || qty <= 0) {
      notify("Verifica que las dimensiones H, B, t, longitud y cantidad sean mayores a 0")
      return
    }

    val unitWeight = BeamCatalog.calculateColdFormedWeightPerM(h, b, c, t)
    val totalWeightKg = unitWeight * (length / 1000.0) * qty
    val cText = if (c > 0.0) "x${c.format()}" else ""
    val dimsStr = "G/U ${h.toInt()}x${b.toInt()}${cText}x${t.format()} mm"

    val item = BeamItemEntity(
      type = "Correa G / Canal",
      category = categoryName,
      name = name,
      h = h,
      hw = h,
      b = b,
      tf = t,
      tw = t,
      r = 0.0,
      dims = dimsStr,
      lengthMm = length,
      quantity = qty,
      unitWeightKgM = unitWeight,
      totalWeightKg = totalWeightKg,
      totalWeightTon = totalWeightKg / 1000.0,
      grossWeightKg = totalWeightKg,
      notchDeductionKg = 0.0,
      hasDestaje = false
    )
    viewModelScope.launch {
      repository.insertBeamItem(item)
      notify("Perfil conformado '$name' añadido")
    }
  }

  private fun addPlaca() {
    val categoryName = "Placa"
    val name = _placaName.value.trim().ifEmpty { "PB-1" }
    val e = _placaEspesorMm.value.toDoubleOrNull()
    val a = _placaAnchoMm.value.toDoubleOrNull()
    val l = _placaLargoMm.value.toDoubleOrNull()
    val qty = _placaQuantity.value.toIntOrNull()

    if (e == null || e <= 0.0 || a == null || a <= 0.0 || l == null || l <= 0.0 || qty == null || qty <= 0) {
      notify("Verifica que el espesor, ancho, largo y cantidad de la placa sean mayores a 0")
      return
    }

    val unitWeight = BeamCatalog.calculatePlateWeight(e, a, l)
    val totalWeightKg = unitWeight * qty

    val item = BeamItemEntity(
      type = "Placa",
      category = categoryName,
      name = name,
      h = a,
      hw = 0.0,
      b = l,
      tf = e,
      tw = e,
      r = 0.0,
      dims = "e=${e.format()} x ${a.format()} x ${l.format()} mm",
      lengthMm = l,
      quantity = qty,
      unitWeightKgM = unitWeight,
      totalWeightKg = totalWeightKg,
      totalWeightTon = totalWeightKg / 1000.0,
      grossWeightKg = totalWeightKg,
      notchDeductionKg = 0.0,
      hasDestaje = false
    )

    viewModelScope.launch {
      repository.insertBeamItem(item)
      notify("Placa '$name' añadida a la lista")
    }
  }

  // Backwards compatibility actions
  fun addCommercialBeam() {
    addElement()
  }

  fun addCustomBeam() {
    addElement()
  }

  fun saveCustomProfileTemplate() {
    val name = _armName.value.trim().ifEmpty { "Perfil Personalizado" }
    val h = _armH.value.toDoubleOrNull()
    val b = _armB.value.toDoubleOrNull()
    val tf = _armTf.value.toDoubleOrNull()
    val tw = _armTw.value.toDoubleOrNull()
    val r = _armR.value.toDoubleOrNull() ?: 0.0

    if (h == null || h <= 0.0 || b == null || b <= 0.0 || tf == null || tf <= 0.0 || tw == null || tw <= 0.0) {
      notify("Ingresa dimensiones válidas antes de guardar")
      return
    }

    val profile = SavedProfileEntity(
      name = name,
      h = h,
      b = b,
      tf = tf,
      tw = tw,
      r = r,
      profileType = "ARMADA"
    )

    viewModelScope.launch {
      val newId = repository.insertSavedProfile(profile)
      _selectedSavedProfileId.value = newId
      notify("Perfil '$name' guardado en tus plantillas")
    }
  }

  fun saveBoxColumnTemplate() {
    val name = _boxColName.value.trim().ifEmpty { "Columna Cajón" }
    val h = _boxColH.value.toDoubleOrNull()
    val b = _boxColB.value.toDoubleOrNull()
    val t = _boxColT.value.toDoubleOrNull()

    if (h == null || h <= 0.0 || b == null || b <= 0.0 || t == null || t <= 0.0) {
      notify("Ingresa dimensiones válidas para la columna cajón antes de guardar")
      return
    }

    val profile = SavedProfileEntity(
      name = name,
      h = h,
      b = b,
      tf = t,
      tw = t,
      r = 0.0,
      profileType = "BOX_COLUMN"
    )

    viewModelScope.launch {
      val newId = repository.insertSavedProfile(profile)
      _selectedSavedProfileId.value = newId
      notify("Plantilla de columna cajón '$name' guardada")
    }
  }

  fun deleteSavedProfile(profile: SavedProfileEntity) {
    viewModelScope.launch {
      repository.deleteSavedProfile(profile)
      if (_selectedSavedProfileId.value == profile.id) {
        _selectedSavedProfileId.value = null
      }
      notify("Perfil '${profile.name}' eliminado")
    }
  }

  fun deleteBeamItem(item: BeamItemEntity) {
    viewModelScope.launch {
      repository.deleteBeamItem(item)
    }
  }

  fun clearAllBeams() {
    viewModelScope.launch {
      repository.clearAllBeamItems()
      notify("Lista de montaje vaciada")
    }
  }

  // Export functions (Compact / Detailed CSV formatted for Excel)
  fun exportAndShare(context: Context, isDetailed: Boolean) {
    val items = beamItems.value
    if (items.isEmpty()) {
      notify("La lista está vacía. Añade elementos primero.")
      return
    }

    try {
      val success = com.example.util.CsvExportHelper.exportAndShareCsv(context, items, isDetailed)
      if (!success) {
        notify("No se pudo generar el archivo de exportación.")
      }
    } catch (e: Exception) {
      notify("Error al generar el CSV: ${e.localizedMessage}")
    }
  }

  fun printPdfReport(context: Context) {
    val items = beamItems.value
    if (items.isEmpty()) {
      notify("La lista está vacía.")
      return
    }

    var totalPzas = 0
    var totalM = 0.0
    var totalGrossKg = 0.0
    var totalDeductionKg = 0.0
    var totalNetKg = 0.0
    val rowsHtml = StringBuilder()

    items.forEachIndexed { index, v ->
      val isPl = v.category.contains("placa", ignoreCase = true) || v.type.contains("placa", ignoreCase = true)
      val longTotalM = if (isPl) 0.0 else (v.lengthMm * v.quantity) / 1000.0
      totalPzas += v.quantity
      totalM += longTotalM
      totalGrossKg += v.grossWeightKg
      totalDeductionKg += v.notchDeductionKg
      totalNetKg += v.totalWeightKg

      val destajeBadge = if (v.hasDestaje) {
        "<span style='color:#b45309;font-weight:bold;'>${v.destajeInfo}</span>"
      } else {
        "<span style='color:#94a3b8;'>-</span>"
      }

      val unitWeightLabel = if (v.type == "Placa") "${String.format(Locale.US, "%.2f", v.unitWeightKgM)} kg/u" else "${String.format(Locale.US, "%.2f", v.unitWeightKgM)} kg/m"
      val longTotalText = if (isPl) "-" else String.format(Locale.US, "%.2f", longTotalM)

      rowsHtml.append("""
        <tr>
          <td>${index + 1}</td>
          <td><span style="font-size:8pt;background:#e2e8f0;padding:2px 4px;border-radius:3px;font-weight:bold;">${v.category}</span></td>
          <td>${v.type}</td>
          <td><strong>${v.name}</strong></td>
          <td>${v.dims}</td>
          <td>$destajeBadge</td>
          <td>${v.hw.format()}</td>
          <td>${v.lengthMm.toInt()}</td>
          <td>${v.quantity}</td>
          <td style="font-weight:bold;">$longTotalText</td>
          <td>$unitWeightLabel</td>
          <td>${String.format(Locale.US, "%.2f", v.grossWeightKg)}</td>
          <td style="color:${if (v.notchDeductionKg > 0.0) "#b91c1c" else "#64748b"};">${if (v.notchDeductionKg > 0.0) "-${String.format(Locale.US, "%.2f", v.notchDeductionKg)}" else "0.00"}</td>
          <td style="font-weight:bold;">${String.format(Locale.US, "%.2f", v.totalWeightKg)}</td>
          <td style="font-weight:bold;color:#047857;">${String.format(Locale.US, "%.3f", v.totalWeightTon)}</td>
        </tr>
      """.trimIndent())
    }

    // Category tonnage breakdown
    val categoryBreakdownHtml = StringBuilder()
    val groupedByCategory = items.groupBy { it.category }
    groupedByCategory.forEach { (category, catItems) ->
      val catPzas = catItems.sumOf { it.quantity }
      val catKg = catItems.sumOf { it.totalWeightKg }
      val catTon = catKg / 1000.0
      categoryBreakdownHtml.append("""
        <tr>
          <td style="text-align:left;font-weight:bold;padding-left:8px;">$category</td>
          <td>$catPzas u</td>
          <td style="font-weight:bold;">${String.format(Locale.US, "%,.1f", catKg)} kg</td>
          <td style="font-weight:bold;color:#047857;">${String.format(Locale.US, "%.3f", catTon)} t</td>
        </tr>
      """.trimIndent())
    }

    val htmlDoc = """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>Cubicación de Estructuras y Vigas para Montaje</title>
        <style>
          @page { size: landscape; margin: 8mm; }
          body { font-family: Arial, sans-serif; font-size: 9pt; color: #000; margin: 0; padding: 6px; }
          h2 { text-align: center; font-size: 13pt; margin-bottom: 2px; text-transform: uppercase; }
          p.subtitle { text-align: center; font-size: 8pt; color: #555; margin-top: 0; margin-bottom: 10px; }
          table { width: 100%; border-collapse: collapse; text-align: center; margin-bottom: 12px; }
          th, td { border: 1px solid #333; padding: 4px 2px; font-size: 8pt; }
          th { background-color: #f1f5f9; font-weight: bold; }
          .units th { font-size: 7pt; color: #64748b; font-weight: normal; background-color: #f8fafc; }
          .totales { font-weight: bold; background-color: #e2e8f0; }
          .category-box { margin-top: 10px; margin-bottom: 10px; width: 450px; }
        </style>
      </head>
      <body>
        <h2>Control y Cubicador de Estructuras de Acero para Montaje</h2>
        <p class="subtitle">V. Principales, V. Secundarias (Destajes), Columnas, Correas y Placas de Montaje</p>
        <table>
          <thead>
            <tr>
              <th>Item</th>
              <th>Categoria</th>
              <th>Tipo</th>
              <th>Perfil / Marca</th>
              <th>Dimensiones</th>
              <th>Destaje</th>
              <th>Alma hw</th>
              <th>Longitud</th>
              <th>Cant</th>
              <th>Long. Total</th>
              <th>Peso Unit.</th>
              <th>Peso Bruto</th>
              <th>Reduccion</th>
              <th>Peso Neto</th>
              <th>Total Ton</th>
            </tr>
            <tr class="units">
              <th>-</th>
              <th>-</th>
              <th>-</th>
              <th>-</th>
              <th>(mm)</th>
              <th>(mm)</th>
              <th>(mm)</th>
              <th>(mm)</th>
              <th>(u)</th>
              <th>(m)</th>
              <th>(kg/m-u)</th>
              <th>(kg)</th>
              <th>(kg)</th>
              <th>(kg)</th>
              <th>(t)</th>
            </tr>
          </thead>
          <tbody>
            $rowsHtml
            <tr class="totales">
              <td colspan="8" style="text-align:center;">TOTALES ACUMULADOS</td>
              <td>$totalPzas</td>
              <td>${String.format(Locale.US, "%.2f", totalM)}</td>
              <td>-</td>
              <td>${String.format(Locale.US, "%.2f", totalGrossKg)}</td>
              <td>${String.format(Locale.US, "%.2f", totalDeductionKg)}</td>
              <td>${String.format(Locale.US, "%.2f", totalNetKg)}</td>
              <td style="color:#047857;">${String.format(Locale.US, "%.3f", totalNetKg / 1000.0)}</td>
            </tr>
          </tbody>
        </table>

        <!-- Resumen de Toneladas por Categoría -->
        <div style="display:flex; justify-content: flex-start;">
          <table class="category-box">
            <thead>
              <tr>
                <th colspan="4" style="background-color:#cbd5e1;font-size:8.5pt;">RESUMEN DE TONELAJE POR CATEGORÍA</th>
              </tr>
              <tr class="units">
                <th>Categoría</th>
                <th>Piezas</th>
                <th>Peso Neto (kg)</th>
                <th>Total (t)</th>
              </tr>
            </thead>
            <tbody>
              $categoryBreakdownHtml
              <tr class="totales">
                <td style="text-align:left;padding-left:8px;">TOTAL GENERAL</td>
                <td>$totalPzas u</td>
                <td>${String.format(Locale.US, "%,.1f", totalNetKg)} kg</td>
                <td style="color:#047857;">${String.format(Locale.US, "%.3f", totalNetKg / 1000.0)} t</td>
              </tr>
            </tbody>
          </table>
        </div>
      </body>
      </html>
    """.trimIndent()

    try {
      val webView = WebView(context)
      webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
          val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
          val printAdapter = webView.createPrintDocumentAdapter("Cubicacion_Vigas_Montaje")
          printManager?.print("Cubicador Vigas", printAdapter, PrintAttributes.Builder().build())
        }
      }
      webView.loadDataWithBaseURL(null, htmlDoc, "text/html", "utf-8", null)
    } catch (e: Exception) {
      notify("Error al imprimir: ${e.localizedMessage}")
    }
  }

  companion object {
    fun provideFactory(application: Application): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          val database = BeamDatabase.getDatabase(application, kotlinx.coroutines.GlobalScope)
          val repository = BeamRepository(database.beamDao())
          return BeamViewModel(application, repository) as T
        }
      }
  }
}

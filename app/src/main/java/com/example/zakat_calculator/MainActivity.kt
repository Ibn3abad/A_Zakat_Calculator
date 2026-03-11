package com.example.zakat_calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.zakat_calculator.ui.theme.Zakat_CalculatorTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Zakat_CalculatorTheme {
                Zakat_CalculatorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Zakat_CalculatorApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.LIQUIDITAET) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val primaryPurple = Color(0xFFBB86FC)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color(0xFF121212)
            ) {
                Spacer(Modifier.height(48.dp))
                Text("Language / Sprache / اللغة", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray, fontSize = 12.sp)
                Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LanguageSelectButton("DE", "de")
                    LanguageSelectButton("EN", "en")
                    LanguageSelectButton("FR", "fr")
                    LanguageSelectButton("AR", "ar")
                }
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                Text(stringResource(R.string.drawer_title), modifier = Modifier.padding(16.dp), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = Color(0xFF3700B3), modifier = Modifier.padding(horizontal = 16.dp))
                AppDestinations.values().forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes), fontSize = 16.sp, color = Color.White) },
                        selected = item == currentDestination,
                        onClick = { currentDestination = item; scope.launch { drawerState.close() } },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = Color(0xFF311B92), selectedIconColor = primaryPurple, unselectedIconColor = Color.Gray),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Image(painter = painterResource(id = R.drawable.zakat_background_pattern), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.25f)
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.app_name), color = Color.White, fontWeight = FontWeight.Bold) },
                        navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Menü", tint = primaryPurple) } },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black.copy(alpha = 0.7f))
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) { ZakatPage(currentDestination, primaryPurple) }
            }
        }
    }
}

@Composable
fun LanguageSelectButton(label: String, langCode: String) {
    TextButton(onClick = {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }) { Text(label, color = Color.White, fontSize = 14.sp) }
}

@Composable
fun ZakatPage(destination: AppDestinations, accentColor: Color) {
    var inputValue by rememberSaveable { mutableStateOf("") }
    var inputLiabilities by rememberSaveable { mutableStateOf("") }
    var irrigationType by rememberSaveable { mutableStateOf(1.0) }
    var animalType by rememberSaveable { mutableStateOf("Schafe") }
    var nisabTypeForLiquid by rememberSaveable { mutableStateOf(0) }

    val liveGoldPrice = 143.22
    val liveSilverPrice = 0.82
    val nisabGoldEuro = 85 * liveGoldPrice
    val nisabSilverEuro = 595 * liveSilverPrice

    val inputAssets = inputValue.toDoubleOrNull() ?: 0.0
    val liabilities = inputLiabilities.toDoubleOrNull() ?: 0.0
    val zakatBase = if (destination == AppDestinations.FIRMA) (inputAssets - liabilities) else inputAssets

    val sheepLabel = stringResource(R.string.animal_sheep)
    val cowLabel = stringResource(R.string.animal_cows)
    val camelLabel = stringResource(R.string.animal_camels)

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), border = BorderStroke(1.dp, accentColor)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.market_nisab_title), color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${stringResource(R.string.gold)}: ${String.format("%.0f", nisabGoldEuro)} €", color = Color.White)
                    Text("${stringResource(R.string.silver)}: ${String.format("%.0f", nisabSilverEuro)} €", color = Color.White)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C).copy(alpha = 0.9f))) {
            val (nisabLabel, nisabVal) = when(destination) {
                AppDestinations.LIQUIDITAET, AppDestinations.FIRMA, AppDestinations.AKTIEN -> stringResource(R.string.nisab_label) to if(nisabTypeForLiquid == 0) "${String.format("%.0f", nisabGoldEuro)} €" else "${String.format("%.0f", nisabSilverEuro)} €"
                AppDestinations.GOLD -> stringResource(R.string.nisab_label_short) to "85 Gramm"
                AppDestinations.SILBER -> stringResource(R.string.nisab_label_short) to "595 Gramm"
                AppDestinations.ERNTE -> stringResource(R.string.nisab_label_short) to "650 kg"
                AppDestinations.TIERE -> {
                    val currentAnimalLabel = when(animalType) { "Schafe" -> sheepLabel; "Kühe" -> cowLabel; else -> camelLabel }
                    "${stringResource(R.string.nisab_label_short)} ($currentAnimalLabel):" to when(animalType){ "Schafe" -> "40"; "Kühe" -> "30"; else -> "5" }
                }
            }
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(destination.icon, null, tint = accentColor, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(nisabLabel, color = Color.LightGray)
                Spacer(Modifier.width(8.dp))
                Text(nisabVal, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        when (destination) {
            AppDestinations.LIQUIDITAET, AppDestinations.FIRMA, AppDestinations.AKTIEN -> {
                Text(stringResource(R.string.nisab_standard), color = Color.White)
                Row {
                    listOf(stringResource(R.string.gold) to 0, stringResource(R.string.silver) to 1).forEach { (l, v) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.selectable(nisabTypeForLiquid == v, onClick = { nisabTypeForLiquid = v }).padding(end = 16.dp)) {
                            RadioButton(nisabTypeForLiquid == v, { nisabTypeForLiquid = v }, colors = RadioButtonDefaults.colors(selectedColor = accentColor))
                            Text(l, color = Color.White)
                        }
                    }
                }
            }
            AppDestinations.ERNTE -> {
                Text(stringResource(R.string.irrigation), color = Color.White)
                Column {
                    listOf(stringResource(R.string.irr_rain) to 1.0, stringResource(R.string.irr_artificial) to 0.5, stringResource(R.string.irr_mixed) to 0.75).forEach { (l, v) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.selectable(irrigationType == v, onClick = { irrigationType = v })) {
                            RadioButton(irrigationType == v, { irrigationType = v }, colors = RadioButtonDefaults.colors(selectedColor = accentColor))
                            Text(l, color = Color.White)
                        }
                    }
                }
            }
            AppDestinations.TIERE -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(sheepLabel to "Schafe", cowLabel to "Kühe", camelLabel to "Kamele").forEach { (label, techName) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.selectable(animalType == techName, onClick = { animalType = techName })) {
                            RadioButton(animalType == techName, { animalType = techName }, colors = RadioButtonDefaults.colors(selectedColor = accentColor))
                            Text(label, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
            else -> {}
        }

        val suffix = when(destination) {
            AppDestinations.ERNTE -> "kg"
            AppDestinations.TIERE -> stringResource(R.string.unit_pieces)
            AppDestinations.GOLD, AppDestinations.SILBER -> "Gramm"
            else -> "€"
        }

        val mainLabel = if (destination == AppDestinations.FIRMA) stringResource(R.string.assets_label) else stringResource(R.string.input_label)
        OutlinedTextField(
            value = inputValue,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) inputValue = it },
            label = { Text("$mainLabel ($suffix)", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = accentColor, unfocusedBorderColor = Color.Gray, cursorColor = accentColor)
        )

        if (destination == AppDestinations.FIRMA) {
            OutlinedTextField(
                value = inputLiabilities,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) inputLiabilities = it },
                label = { Text("${stringResource(R.string.liabilities_label)} (€)", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = accentColor, unfocusedBorderColor = Color.Gray, cursorColor = accentColor)
            )
        }

        if (inputAssets > 0 || (destination == AppDestinations.FIRMA && zakatBase != 0.0)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF311B92).copy(alpha = 0.9f)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val res = when(destination) {
                        AppDestinations.ERNTE -> if (zakatBase >= 650) "${stringResource(R.string.res_give)}: ${String.format("%.2f", zakatBase * 0.1 * irrigationType)} kg" else stringResource(R.string.under_nisab)
                        AppDestinations.TIERE -> {
                            val n = zakatBase.toInt()
                            when(animalType) {
                                "Schafe" -> when {
                                    n < 40 -> stringResource(R.string.under_nisab) + " (40)"
                                    n in 40..120 -> "1 ${stringResource(R.string.res_sheep)}"
                                    n in 121..200 -> "2 ${stringResource(R.string.res_sheep)}"
                                    n in 201..399 -> "3 ${stringResource(R.string.res_sheep)}"
                                    else -> "${n / 100} ${stringResource(R.string.res_sheep)}"
                                }
                                "Kühe" -> when {
                                    n < 30 -> stringResource(R.string.under_nisab) + " (30)"
                                    n in 30..39 -> "1 " + stringResource(R.string.res_tabi)
                                    n in 40..59 -> "1 " + stringResource(R.string.res_musinnah)
                                    n in 60..69 -> "2 " + stringResource(R.string.res_tabi)
                                    n in 70..79 -> "1 ${stringResource(R.string.res_tabi)} & 1 ${stringResource(R.string.res_musinnah)}"
                                    n in 80..89 -> "2 " + stringResource(R.string.res_musinnah)
                                    n in 90..99 -> "3 " + stringResource(R.string.res_tabi)
                                    else -> "${n / 30} ${stringResource(R.string.res_tabi)} / ${stringResource(R.string.res_musinnah)}"
                                }
                                "Kamele" -> when {
                                    n < 5 -> stringResource(R.string.under_nisab) + " (5)"
                                    n in 5..9 -> "1 ${stringResource(R.string.res_sheep)}"
                                    n in 10..14 -> "2 ${stringResource(R.string.res_sheep)}"
                                    n in 15..19 -> "3 ${stringResource(R.string.res_sheep)}"
                                    n in 20..24 -> "4 ${stringResource(R.string.res_sheep)}"
                                    n in 25..35 -> "1 " + stringResource(R.string.res_bint_makhad)
                                    n in 36..45 -> "1 " + stringResource(R.string.res_bint_labun)
                                    n in 46..60 -> "1 " + stringResource(R.string.res_hiqqah)
                                    n in 61..75 -> "1 " + stringResource(R.string.res_jadhah)
                                    n in 76..90 -> "2 " + stringResource(R.string.res_bint_labun)
                                    n in 91..120 -> "2 " + stringResource(R.string.res_hiqqah)
                                    else -> stringResource(R.string.res_camel_rule)
                                }
                                else -> ""
                            }
                        }
                        AppDestinations.GOLD -> if (zakatBase >= 85) "${String.format("%.2f", (zakatBase * liveGoldPrice) * 0.025)} €" else "${stringResource(R.string.under_nisab)} (85g)"
                        AppDestinations.SILBER -> if (zakatBase >= 595) "${String.format("%.2f", (zakatBase * liveSilverPrice) * 0.025)} €" else "${stringResource(R.string.under_nisab)} (595g)"
                        else -> {
                            val nisab = if (nisabTypeForLiquid == 0) nisabGoldEuro else nisabSilverEuro
                            if (zakatBase >= nisab) {
                                val result = zakatBase * 0.025
                                if (destination == AppDestinations.FIRMA) {
                                    "${stringResource(R.string.res_base)} ${String.format("%.2f", zakatBase)} €\n" +
                                            "${stringResource(R.string.res_zakat_amount)} ${String.format("%.2f", result)} €"
                                } else "${String.format("%.2f", result)} €"
                            } else stringResource(R.string.under_nisab)
                        }
                    }
                    Text(stringResource(R.string.result_label), color = Color.White.copy(0.7f), fontSize = 12.sp)
                    Text(res, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

enum class AppDestinations(val labelRes: Int, val icon: ImageVector) {
    LIQUIDITAET(R.string.cat_liquid, Icons.Default.AccountBalanceWallet),
    FIRMA(R.string.cat_business, Icons.Default.Business),
    AKTIEN(R.string.cat_stocks, Icons.Default.ShowChart),
    GOLD(R.string.cat_gold, Icons.Default.Savings),
    SILBER(R.string.cat_silver, Icons.Default.MonetizationOn),
    ERNTE(R.string.cat_harvest, Icons.Default.Agriculture),
    TIERE(R.string.cat_animals, Icons.Default.Pets),
}
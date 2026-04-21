package com.ibn3abad.zakat_calculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.ibn3abad.zakat_calculator.ui.theme.Zakat_CalculatorTheme
import kotlinx.coroutines.launch

import com.google.android.gms.ads.MobileAds
//import kotlin.concurrent.thread

//import android.view.ViewGroup
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : AppCompatActivity() {

    private val zakatViewModel: ZakatViewModel by viewModels()
    private lateinit var consentInformation: ConsentInformation

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        requestConsent()

        enableEdgeToEdge()
        setContent {
            Zakat_CalculatorTheme {
                ZakatCalculatorApp(zakatViewModel)
            }
        }
    }

    private fun requestConsent() {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        android.util.Log.w("Ads", "${loadAndShowError.errorCode}: ${loadAndShowError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk()
                    }
                }
            },
            { requestConsentError ->
                android.util.Log.w("Ads", "${requestConsentError.errorCode}: ${requestConsentError.message}")
            }
        )

        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk()
        }
    }

    private var isMobileAdsSdkInitialized = false
    private fun initializeMobileAdsSdk() {
        if (isMobileAdsSdkInitialized) return
        isMobileAdsSdkInitialized = true

        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("402D6A4F87A518FEB1CCCBBD510C8BE3"))
            .build()
        MobileAds.setRequestConfiguration(configuration)
        MobileAds.initialize(this) {}
    }
}

enum class AnimalType {
    SHEEP, COWS, CAMELS
}

enum class AppDestinations(val labelRes: Int, val iconRes: Int) {
    LIQUIDITAET(R.string.cat_liquid, R.drawable.bares_geld_64x64),
    FIRMA(R.string.cat_business, R.drawable.firmen_64x64),
    AKTIEN(R.string.cat_stocks, R.drawable.aktien_64x64),
    GOLD(R.string.cat_gold, R.drawable.gold_64x64),
    SILBER(R.string.cat_silver, R.drawable.silver_64x64),
    ERNTE(R.string.cat_harvest, R.drawable.ernte_64x64),
    TIERE(R.string.cat_animals, R.drawable.tiere_64x64),
    ABOUT(R.string.cat_about, R.drawable.app_logo),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatCalculatorApp(viewModel: ZakatViewModel) {
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
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Language / Sprache / اللغة",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LanguageSelectButton("DE", "de")
                    LanguageSelectButton("EN", "en")
                    LanguageSelectButton("FR", "fr")
                    LanguageSelectButton("AR", "ar")
                }

                HorizontalDivider(
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = stringResource(R.string.drawer_title),
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(
                    color = Color(0xFF3700B3),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                //AppDestinations.values().forEach { item ->
                AppDestinations.entries.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.labelRes),
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        },
                        selected = item == currentDestination,
                        onClick = {
                            currentDestination = item
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF311B92),
                            selectedIconColor = primaryPurple,
                            unselectedIconColor = Color.Gray
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Image(
                painter = painterResource(id = R.drawable.zakat_background_pattern),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.25f
            )

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.app_name),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = null,
                                    tint = primaryPurple
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        )
                    )
                },
                bottomBar = {
                    AdMobBanner(
                        // Google Test-ID für Banner (Immer zum Testen verwenden!)
                        //adUnitId = "ca-app-pub-3940256099942544/6300978111"
                        adUnitId = if (BuildConfig.DEBUG) {
                                        "ca-app-pub-3940256099942544/6300978111" // Test ID
                                   } else {
                                        "ca-app-pub-5740096341351637/3347360479" // Production ID
                                   }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding) // WICHTIG: Verhindert Überlappung
                ) {
                    when (currentDestination) {
                        AppDestinations.ABOUT -> AboutPage(primaryPurple)
                        else -> {
                            ZakatPage(
                                destination = currentDestination,
                                accentColor = primaryPurple,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutPage(accentColor: Color) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val websiteUrl = stringResource(R.string.website_url)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.app_name),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.about_description),
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        androidx.compose.material3.Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                context.startActivity(intent)
            },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text(text = stringResource(R.string.about_website_button), color = Color.Black)
        }
    }
}

@Composable
fun LanguageSelectButton(label: String, langCode: String) {
    TextButton(
        onClick = {
            val appLocale = LocaleListCompat.forLanguageTags(langCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ZakatPage(
    destination: AppDestinations,
    accentColor: Color,
    viewModel: ZakatViewModel
) {
    val sheepLabel = stringResource(R.string.animal_sheep)
    val cowLabel = stringResource(R.string.animal_cows)
    val camelLabel = stringResource(R.string.animal_camels)

    val underNisabText = stringResource(R.string.under_nisab)
    val resGiveText = stringResource(R.string.res_give)
    val resBaseText = stringResource(R.string.res_base)
    val resZakatAmountText = stringResource(R.string.res_zakat_amount)
    val resSheepText = stringResource(R.string.res_sheep)
    val resTabiText = stringResource(R.string.res_tabi)
    val resMusinnahText = stringResource(R.string.res_musinnah)
    val resBintMakhadText = stringResource(R.string.res_bint_makhad)
    val resBintLabunText = stringResource(R.string.res_bint_labun)
    val resHiqqahText = stringResource(R.string.res_hiqqah)
    val resJadhahText = stringResource(R.string.res_jadhah)
    val resCamelRuleText = stringResource(R.string.res_camel_rule)

    val zakatBase = viewModel.getZakatBase(destination)
    val shouldShowResult = viewModel.shouldShowResult(destination)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MarketNisabCard(
            nisabGoldEuro = viewModel.nisabGoldEuro,
            nisabSilverEuro = viewModel.nisabSilverEuro,
            accentColor = accentColor
        )

        NisabInfoCard(
            destination = destination,
            animalType = viewModel.animalType,
            nisabTypeForLiquid = viewModel.nisabTypeForLiquid,
            nisabGoldEuro = viewModel.nisabGoldEuro,
            nisabSilverEuro = viewModel.nisabSilverEuro,
            sheepLabel = sheepLabel,
            cowLabel = cowLabel,
            camelLabel = camelLabel
        )

        when (destination) {
            AppDestinations.LIQUIDITAET,
            AppDestinations.FIRMA,
            AppDestinations.AKTIEN -> {
                Text(stringResource(R.string.nisab_standard), color = Color.White)

                Row {
                    listOf(
                        stringResource(R.string.gold) to 0,
                        stringResource(R.string.silver) to 1
                    ).forEach { (label, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .selectable(
                                    selected = viewModel.nisabTypeForLiquid == value,
                                    onClick = { viewModel.onNisabTypeChange(value) }
                                )
                                .padding(end = 16.dp)
                        ) {
                            RadioButton(
                                selected = viewModel.nisabTypeForLiquid == value,
                                onClick = { viewModel.onNisabTypeChange(value) },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Text(label, color = Color.White)
                        }
                    }
                }
            }

            AppDestinations.ERNTE -> {
                Text(stringResource(R.string.irrigation), color = Color.White)

                Column {
                    listOf(
                        stringResource(R.string.irr_rain) to 0.10,
                        stringResource(R.string.irr_artificial) to 0.05,
                        stringResource(R.string.irr_mixed) to 0.075
                    ).forEach { (label, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.selectable(
                                selected = viewModel.irrigationRate == value,
                                onClick = { viewModel.onIrrigationRateChange(value) }
                            )
                        ) {
                            RadioButton(
                                selected = viewModel.irrigationRate == value,
                                onClick = { viewModel.onIrrigationRateChange(value) },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Text(label, color = Color.White)
                        }
                    }
                }
            }

            AppDestinations.TIERE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        sheepLabel to AnimalType.SHEEP,
                        cowLabel to AnimalType.COWS,
                        camelLabel to AnimalType.CAMELS
                    ).forEach { (label, type) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.selectable(
                                selected = viewModel.animalType == type,
                                onClick = { viewModel.onAnimalTypeChange(type) }
                            )
                        ) {
                            RadioButton(
                                selected = viewModel.animalType == type,
                                onClick = { viewModel.onAnimalTypeChange(type) },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            else -> Unit
        }

        val suffix = when (destination) {
            AppDestinations.ERNTE -> "kg"
            AppDestinations.TIERE -> stringResource(R.string.unit_pieces)
            AppDestinations.GOLD, AppDestinations.SILBER -> "g"
            else -> "€"
        }

        val mainLabel = if (destination == AppDestinations.FIRMA) {
            stringResource(R.string.assets_label)
        } else {
            stringResource(R.string.input_label)
        }

        DecimalInputField(
            value = viewModel.inputValue,
            onValueChange = viewModel::onInputValueChange,
            label = "$mainLabel ($suffix)",
            accentColor = accentColor
        )

        if (destination == AppDestinations.FIRMA) {
            DecimalInputField(
                value = viewModel.inputLiabilities,
                onValueChange = viewModel::onLiabilitiesChange,
                label = "${stringResource(R.string.liabilities_label)} (€)",
                accentColor = accentColor
            )
        }

        if (shouldShowResult) {
            val resultText = ZakatCalculator.calculateZakatResult(
                destination = destination,
                zakatBase = zakatBase,
                nisabTypeForLiquid = viewModel.nisabTypeForLiquid,
                nisabGoldEuro = viewModel.nisabGoldEuro,
                nisabSilverEuro = viewModel.nisabSilverEuro,
                irrigationRate = viewModel.irrigationRate,
                animalType = viewModel.animalType,
                goldPricePerGram = viewModel.goldPricePerGram,
                silverPricePerGram = viewModel.silverPricePerGram,
                underNisabText = underNisabText,
                resGiveText = resGiveText,
                resBaseText = resBaseText,
                resZakatAmountText = resZakatAmountText,
                resSheepText = resSheepText,
                resTabiText = resTabiText,
                resMusinnahText = resMusinnahText,
                resBintMakhadText = resBintMakhadText,
                resBintLabunText = resBintLabunText,
                resHiqqahText = resHiqqahText,
                resJadhahText = resJadhahText,
                resCamelRuleText = resCamelRuleText
            )

            ResultCard(result = resultText)
        }
    }
}

@Composable
fun MarketNisabCard(
    nisabGoldEuro: Double,
    nisabSilverEuro: Double,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, accentColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.market_nisab_title),
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.gold)}: ${formatNumber(nisabGoldEuro, 0)} €",
                    color = Color.White
                )
                Text(
                    text = "${stringResource(R.string.silver)}: ${formatNumber(nisabSilverEuro, 0)} €",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun NisabInfoCard(
    destination: AppDestinations,
    animalType: AnimalType,
    nisabTypeForLiquid: Int,
    nisabGoldEuro: Double,
    nisabSilverEuro: Double,
    sheepLabel: String,
    cowLabel: String,
    camelLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C).copy(alpha = 0.9f)
        )
    ) {
        val (nisabLabel, nisabValue) = when (destination) {
            AppDestinations.LIQUIDITAET,
            AppDestinations.FIRMA,
            AppDestinations.AKTIEN -> {
                stringResource(R.string.nisab_label) to if (nisabTypeForLiquid == 0) {
                    "${formatNumber(nisabGoldEuro, 0)} €"
                } else {
                    "${formatNumber(nisabSilverEuro, 0)} €"
                }
            }

            AppDestinations.GOLD -> stringResource(R.string.nisab_label_short) to "85 g"
            AppDestinations.SILBER -> stringResource(R.string.nisab_label_short) to "595 g"
            AppDestinations.ERNTE -> stringResource(R.string.nisab_label_short) to "650 kg"

            AppDestinations.TIERE -> {
                val currentAnimalLabel = when (animalType) {
                    AnimalType.SHEEP -> sheepLabel
                    AnimalType.COWS -> cowLabel
                    AnimalType.CAMELS -> camelLabel
                }

                "${stringResource(R.string.nisab_label_short)} ($currentAnimalLabel):" to when (animalType) {
                    AnimalType.SHEEP -> "40"
                    AnimalType.COWS -> "30"
                    AnimalType.CAMELS -> "5"
                }
            }

            else -> "" to ""
        }

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = destination.iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = nisabLabel, color = Color.LightGray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = nisabValue,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DecimalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    accentColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = accentColor,
            unfocusedBorderColor = Color.Gray,
            cursorColor = accentColor
        )
    )
}

@Composable
fun ResultCard(result: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF311B92).copy(alpha = 0.9f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.result_label),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdLoaded() {
                        android.util.Log.d("AdMob", "Werbung erfolgreich geladen!")
                    }
                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        android.util.Log.e("AdMob", "Fehler beim Laden: ${error.message} (Code: ${error.code})")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Hier könnten Updates durchgeführt werden, falls sich die adUnitId ändert
        },
        onRelease = { adView ->
            // WICHTIG: Ressourcen freigeben, um Memory Leaks zu vermeiden
            adView.destroy()
        }
    )
}

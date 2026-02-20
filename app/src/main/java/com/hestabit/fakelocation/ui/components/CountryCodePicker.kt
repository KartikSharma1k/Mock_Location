package com.hestabit.fakelocation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────

data class CountryCode(
    val flag: String,
    val name: String,
    val dialCode: String
)

val countryCodes = listOf(
    CountryCode("🇦🇫", "Afghanistan",            "+93"),
    CountryCode("🇦🇱", "Albania",                "+355"),
    CountryCode("🇩🇿", "Algeria",                "+213"),
    CountryCode("🇦🇩", "Andorra",                "+376"),
    CountryCode("🇦🇴", "Angola",                 "+244"),
    CountryCode("🇦🇷", "Argentina",              "+54"),
    CountryCode("🇦🇲", "Armenia",                "+374"),
    CountryCode("🇦🇺", "Australia",              "+61"),
    CountryCode("🇦🇹", "Austria",                "+43"),
    CountryCode("🇦🇿", "Azerbaijan",             "+994"),
    CountryCode("🇧🇸", "Bahamas",                "+1-242"),
    CountryCode("🇧🇭", "Bahrain",                "+973"),
    CountryCode("🇧🇩", "Bangladesh",             "+880"),
    CountryCode("🇧🇾", "Belarus",                "+375"),
    CountryCode("🇧🇪", "Belgium",                "+32"),
    CountryCode("🇧🇿", "Belize",                 "+501"),
    CountryCode("🇧🇯", "Benin",                  "+229"),
    CountryCode("🇧🇹", "Bhutan",                 "+975"),
    CountryCode("🇧🇴", "Bolivia",                "+591"),
    CountryCode("🇧🇦", "Bosnia & Herzegovina",   "+387"),
    CountryCode("🇧🇼", "Botswana",               "+267"),
    CountryCode("🇧🇷", "Brazil",                 "+55"),
    CountryCode("🇧🇳", "Brunei",                 "+673"),
    CountryCode("🇧🇬", "Bulgaria",               "+359"),
    CountryCode("🇧🇫", "Burkina Faso",           "+226"),
    CountryCode("🇧🇮", "Burundi",                "+257"),
    CountryCode("🇨🇻", "Cabo Verde",             "+238"),
    CountryCode("🇰🇭", "Cambodia",               "+855"),
    CountryCode("🇨🇲", "Cameroon",               "+237"),
    CountryCode("🇨🇦", "Canada",                 "+1"),
    CountryCode("🇨🇫", "Central African Rep.",   "+236"),
    CountryCode("🇨🇱", "Chile",                  "+56"),
    CountryCode("🇨🇳", "China",                  "+86"),
    CountryCode("🇨🇴", "Colombia",               "+57"),
    CountryCode("🇰🇲", "Comoros",                "+269"),
    CountryCode("🇨🇬", "Congo",                  "+242"),
    CountryCode("🇨🇷", "Costa Rica",             "+506"),
    CountryCode("🇭🇷", "Croatia",                "+385"),
    CountryCode("🇨🇺", "Cuba",                   "+53"),
    CountryCode("🇨🇾", "Cyprus",                 "+357"),
    CountryCode("🇨🇿", "Czech Republic",         "+420"),
    CountryCode("🇩🇰", "Denmark",                "+45"),
    CountryCode("🇩🇯", "Djibouti",               "+253"),
    CountryCode("🇩🇴", "Dominican Republic",     "+1-809"),
    CountryCode("🇪🇨", "Ecuador",                "+593"),
    CountryCode("🇪🇬", "Egypt",                  "+20"),
    CountryCode("🇸🇻", "El Salvador",            "+503"),
    CountryCode("🇬🇶", "Equatorial Guinea",      "+240"),
    CountryCode("🇪🇷", "Eritrea",                "+291"),
    CountryCode("🇪🇪", "Estonia",                "+372"),
    CountryCode("🇸🇿", "Eswatini",               "+268"),
    CountryCode("🇪🇹", "Ethiopia",               "+251"),
    CountryCode("🇫🇯", "Fiji",                   "+679"),
    CountryCode("🇫🇮", "Finland",                "+358"),
    CountryCode("🇫🇷", "France",                 "+33"),
    CountryCode("🇬🇦", "Gabon",                  "+241"),
    CountryCode("🇬🇲", "Gambia",                 "+220"),
    CountryCode("🇬🇪", "Georgia",                "+995"),
    CountryCode("🇩🇪", "Germany",                "+49"),
    CountryCode("🇬🇭", "Ghana",                  "+233"),
    CountryCode("🇬🇷", "Greece",                 "+30"),
    CountryCode("🇬🇹", "Guatemala",              "+502"),
    CountryCode("🇬🇳", "Guinea",                 "+224"),
    CountryCode("🇬🇾", "Guyana",                 "+592"),
    CountryCode("🇭🇹", "Haiti",                  "+509"),
    CountryCode("🇭🇳", "Honduras",               "+504"),
    CountryCode("🇭🇺", "Hungary",                "+36"),
    CountryCode("🇮🇸", "Iceland",                "+354"),
    CountryCode("🇮🇳", "India",                  "+91"),
    CountryCode("🇮🇩", "Indonesia",              "+62"),
    CountryCode("🇮🇷", "Iran",                   "+98"),
    CountryCode("🇮🇶", "Iraq",                   "+964"),
    CountryCode("🇮🇪", "Ireland",                "+353"),
    CountryCode("🇮🇱", "Israel",                 "+972"),
    CountryCode("🇮🇹", "Italy",                  "+39"),
    CountryCode("🇯🇲", "Jamaica",                "+1-876"),
    CountryCode("🇯🇵", "Japan",                  "+81"),
    CountryCode("🇯🇴", "Jordan",                 "+962"),
    CountryCode("🇰🇿", "Kazakhstan",             "+7"),
    CountryCode("🇰🇪", "Kenya",                  "+254"),
    CountryCode("🇰🇵", "North Korea",            "+850"),
    CountryCode("🇰🇷", "South Korea",            "+82"),
    CountryCode("🇽🇰", "Kosovo",                 "+383"),
    CountryCode("🇰🇼", "Kuwait",                 "+965"),
    CountryCode("🇰🇬", "Kyrgyzstan",             "+996"),
    CountryCode("🇱🇦", "Laos",                   "+856"),
    CountryCode("🇱🇻", "Latvia",                 "+371"),
    CountryCode("🇱🇧", "Lebanon",                "+961"),
    CountryCode("🇱🇸", "Lesotho",                "+266"),
    CountryCode("🇱🇷", "Liberia",                "+231"),
    CountryCode("🇱🇾", "Libya",                  "+218"),
    CountryCode("🇱🇮", "Liechtenstein",          "+423"),
    CountryCode("🇱🇹", "Lithuania",              "+370"),
    CountryCode("🇱🇺", "Luxembourg",             "+352"),
    CountryCode("🇲🇬", "Madagascar",             "+261"),
    CountryCode("🇲🇼", "Malawi",                 "+265"),
    CountryCode("🇲🇾", "Malaysia",               "+60"),
    CountryCode("🇲🇻", "Maldives",               "+960"),
    CountryCode("🇲🇱", "Mali",                   "+223"),
    CountryCode("🇲🇹", "Malta",                  "+356"),
    CountryCode("🇲🇷", "Mauritania",             "+222"),
    CountryCode("🇲🇺", "Mauritius",              "+230"),
    CountryCode("🇲🇽", "Mexico",                 "+52"),
    CountryCode("🇲🇩", "Moldova",                "+373"),
    CountryCode("🇲🇨", "Monaco",                 "+377"),
    CountryCode("🇲🇳", "Mongolia",               "+976"),
    CountryCode("🇲🇪", "Montenegro",             "+382"),
    CountryCode("🇲🇦", "Morocco",                "+212"),
    CountryCode("🇲🇿", "Mozambique",             "+258"),
    CountryCode("🇲🇲", "Myanmar",                "+95"),
    CountryCode("🇳🇦", "Namibia",                "+264"),
    CountryCode("🇳🇵", "Nepal",                  "+977"),
    CountryCode("🇳🇱", "Netherlands",            "+31"),
    CountryCode("🇳🇿", "New Zealand",            "+64"),
    CountryCode("🇳🇮", "Nicaragua",              "+505"),
    CountryCode("🇳🇪", "Niger",                  "+227"),
    CountryCode("🇳🇬", "Nigeria",                "+234"),
    CountryCode("🇲🇰", "North Macedonia",        "+389"),
    CountryCode("🇳🇴", "Norway",                 "+47"),
    CountryCode("🇴🇲", "Oman",                   "+968"),
    CountryCode("🇵🇰", "Pakistan",               "+92"),
    CountryCode("🇵🇦", "Panama",                 "+507"),
    CountryCode("🇵🇬", "Papua New Guinea",       "+675"),
    CountryCode("🇵🇾", "Paraguay",               "+595"),
    CountryCode("🇵🇪", "Peru",                   "+51"),
    CountryCode("🇵🇭", "Philippines",            "+63"),
    CountryCode("🇵🇱", "Poland",                 "+48"),
    CountryCode("🇵🇹", "Portugal",               "+351"),
    CountryCode("🇶🇦", "Qatar",                  "+974"),
    CountryCode("🇷🇴", "Romania",                "+40"),
    CountryCode("🇷🇺", "Russia",                 "+7"),
    CountryCode("🇷🇼", "Rwanda",                 "+250"),
    CountryCode("🇸🇦", "Saudi Arabia",           "+966"),
    CountryCode("🇸🇳", "Senegal",                "+221"),
    CountryCode("🇷🇸", "Serbia",                 "+381"),
    CountryCode("🇸🇱", "Sierra Leone",           "+232"),
    CountryCode("🇸🇬", "Singapore",              "+65"),
    CountryCode("🇸🇰", "Slovakia",               "+421"),
    CountryCode("🇸🇮", "Slovenia",               "+386"),
    CountryCode("🇸🇴", "Somalia",                "+252"),
    CountryCode("🇿🇦", "South Africa",           "+27"),
    CountryCode("🇸🇸", "South Sudan",            "+211"),
    CountryCode("🇪🇸", "Spain",                  "+34"),
    CountryCode("🇱🇰", "Sri Lanka",              "+94"),
    CountryCode("🇸🇩", "Sudan",                  "+249"),
    CountryCode("🇸🇷", "Suriname",               "+597"),
    CountryCode("🇸🇪", "Sweden",                 "+46"),
    CountryCode("🇨🇭", "Switzerland",            "+41"),
    CountryCode("🇸🇾", "Syria",                  "+963"),
    CountryCode("🇹🇼", "Taiwan",                 "+886"),
    CountryCode("🇹🇯", "Tajikistan",             "+992"),
    CountryCode("🇹🇿", "Tanzania",               "+255"),
    CountryCode("🇹🇭", "Thailand",               "+66"),
    CountryCode("🇹🇱", "Timor-Leste",            "+670"),
    CountryCode("🇹🇬", "Togo",                   "+228"),
    CountryCode("🇹🇳", "Tunisia",                "+216"),
    CountryCode("🇹🇷", "Turkey",                 "+90"),
    CountryCode("🇹🇲", "Turkmenistan",           "+993"),
    CountryCode("🇺🇬", "Uganda",                 "+256"),
    CountryCode("🇺🇦", "Ukraine",                "+380"),
    CountryCode("🇦🇪", "United Arab Emirates",   "+971"),
    CountryCode("🇬🇧", "United Kingdom",         "+44"),
    CountryCode("🇺🇸", "United States",          "+1"),
    CountryCode("🇺🇾", "Uruguay",                "+598"),
    CountryCode("🇺🇿", "Uzbekistan",             "+998"),
    CountryCode("🇻🇪", "Venezuela",              "+58"),
    CountryCode("🇻🇳", "Vietnam",                "+84"),
    CountryCode("🇾🇪", "Yemen",                  "+967"),
    CountryCode("🇿🇲", "Zambia",                 "+260"),
    CountryCode("🇿🇼", "Zimbabwe",               "+263")
)

// ─────────────────────────────────────────────
// Composable
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCodePicker(
    selected: CountryCode,
    onSelected: (CountryCode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var query     by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope      = rememberCoroutineScope()

    val filtered = remember(query) {
        if (query.isBlank()) countryCodes
        else countryCodes.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.dialCode.contains(query, ignoreCase = true)
        }
    }

    // ── Chip ─────────────────────────────────────────────────────────────────
    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showSheet = true }
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEFF6FF))
            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = selected.flag, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = selected.dialCode,
            color = Color(0xFF1D4ED8),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Pick country",
            tint = Color(0xFF1D4ED8),
            modifier = Modifier.size(16.dp)
        )
    }

    // ── Bottom sheet ─────────────────────────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                query = ""
                showSheet = false
            },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp, max = 600.dp)
                    .padding(horizontal = 20.dp)
            ) {
                // Title
                Text(
                    text = "Select Country",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(0xFF111827),
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF3B82F6)),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text("Search country or dial code…", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                            }
                            inner()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Country list
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filtered, key = { "${it.name}-${it.dialCode}" }) { country ->
                        CountryListItem(
                            country = country,
                            isSelected = country.dialCode == selected.dialCode && country.name == selected.name,
                            onClick = {
                                onSelected(country)
                                query = ""
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showSheet = false
                                }
                            }
                        )
                        Divider(color = Color(0xFFF3F4F6), thickness = 0.5.dp)
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CountryListItem(
    country: CountryCode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .background(if (isSelected) Color(0xFFEFF6FF) else Color.White)
            .padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = country.flag, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = country.name,
            color = Color(0xFF111827),
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = country.dialCode,
            color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF6B7280),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

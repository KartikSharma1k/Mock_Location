package com.hestabit.fakelocation.ui.widgets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(
    modifier: Modifier = Modifier,
    hint: String = "Search location",
    placesClient: PlacesClient? = null,
    onPredictionSelected: (AutocompletePrediction) -> Unit = {},
    onSearch: (String) -> Unit = {},
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showDropdown by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val token = remember { AutocompleteSessionToken.newInstance() }


    Box(modifier = modifier, contentAlignment = Alignment.BottomStart) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .zIndex(1f)
                .clip(RoundedCornerShape(24.dp))
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                TextField(
                    value = query,
                    onValueChange = { new ->
                        query = new
                        if (new.isNotBlank() && placesClient != null) {
                            val request = FindAutocompletePredictionsRequest.builder()
                                .setSessionToken(token)
                                .setQuery(new)
                                .build()
                            try {
                                placesClient.findAutocompletePredictions(request)
                                    .addOnSuccessListener { resp ->
                                        suggestions = resp.autocompletePredictions
                                        showDropdown = suggestions.isNotEmpty()
                                    }
                                    .addOnFailureListener {
                                        suggestions = emptyList()
                                        showDropdown = false
                                    }
                            } catch (e: Exception) {
                                suggestions = emptyList()
                                showDropdown = false
                            }
                        } else {
                            suggestions = emptyList()
                            showDropdown = false
                        }
                    },
                    placeholder = { Text(hint) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (query.isNotBlank()) onSearch(query)
                            focusManager.clearFocus()
                            onClose()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        }

        if (showDropdown) {
            Column(
                modifier = Modifier
//                    .offset(y = collapsedSize + 8.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            ) {
                suggestions.forEach { prediction ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                query = prediction.getPrimaryText(null).toString()
                                showDropdown = false
                                suggestions = emptyList()
                                onPredictionSelected(prediction)
                                focusManager.clearFocus()
                            }
                            .padding(12.dp)
                    ) {
                        Text(
                            prediction.getPrimaryText(null).toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        val second = prediction.getSecondaryText(null).toString()
                        if (second.isNotBlank()) Text(
                            second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        LaunchedEffect(true) { focusRequester.requestFocus() }
    }
}
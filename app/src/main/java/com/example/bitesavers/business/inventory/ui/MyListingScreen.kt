package com.example.bitesavers.business.inventory.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bitesavers.R
import com.example.bitesavers.business.inventory.data.ListingItem
import com.example.bitesavers.business.inventory.logic.InventoryViewModel
import com.example.bitesavers.ui.theme.BiteSaversTheme
import com.example.bitesavers.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingScreen(
    viewModel: InventoryViewModel,
    onNavigateToAddFood: () -> Unit,
    onNavigateToEditFood: (String) -> Unit = {}
) {
    val listings by viewModel.listings.collectAsState()

    // Active count checks whether the listing is not expired and has stock
    val activeCount = listings.count {
        it.status.equals("Active", ignoreCase = true) && !isPickupEnd(it.pickupEnd) && it.quantity > 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.my_listings_title),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = stringResource(R.string.my_listings_subtitle, listings.size, activeCount),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.selectedItemForEdit = null
                    onNavigateToAddFood()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.btn_add_food), fontWeight = FontWeight.SemiBold)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listings) { item ->
                ListingCard(
                    item = item,
                    onEditClick = {
                        viewModel.selectedItemForEdit = item
                        onNavigateToEditFood(item.id)
                    },
                    onDelete = { viewModel.deleteListing(item.id) }
                )
            }
        }
    }
}

// Self-contained, crash-proof parser for 12h/24h time strings to minutes from midnight
private fun parseLocalMinutes(timeStr: String?): Int? {
    if (timeStr.isNullOrBlank()) return null
    val raw = timeStr.trim().uppercase()
    return try {
        val isPm = raw.contains("PM")
        val isAm = raw.contains("AM")
        val clean = raw.replace("AM", "").replace("PM", "").trim()
        val parts = clean.split(":")
        var hour = parts[0].trim().toInt()
        val minute = if (parts.size > 1) parts[1].trim().take(2).toInt() else 0

        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        (hour * 60) + minute
    } catch (_: Exception) {
        null
    }
}

/**
 * Foolproof pickup window expiration check locked to Malaysia Standard Time (GMT+8).
 */
fun isPickupEnd(pickupEndTimeStr: String?): Boolean {
    val endMinutes = parseLocalMinutes(pickupEndTimeStr) ?: return false
    val currentMinutes = TimeUtils.getCurrentMinutesOfDay()
    return currentMinutes > endMinutes
}

/**
 * Evaluates whether an item is actively inside the NGO Rescue claim window:
 * Scenario 1: Within 1 hour post-pickup (pickup_end .. pickup_end + 60m).
 * Scenario 2: Between pickup_end and store cleanupEndTime.
 */
fun isNgoRescueWindow(pickupEndTimeStr: String?, cleanupEndTimeStr: String?): Boolean {
    val endMinutes = parseLocalMinutes(pickupEndTimeStr) ?: return false
    val currentMinutes = TimeUtils.getCurrentMinutesOfDay()

    val cleanupMinutes = parseLocalMinutes(cleanupEndTimeStr) ?: (endMinutes + 60)
    val maxEligibleMinute = maxOf(endMinutes + 60, cleanupMinutes)

    return currentMinutes in endMinutes..maxEligibleMinute
}

@Composable
fun ListingCard(
    item: ListingItem,
    onEditClick: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isAfterPickup = isPickupEnd(item.pickupEnd)
    val isNgoWindow = isNgoRescueWindow(item.pickupEnd, item.cleanupEndTime)
    val isNgoRescueTier = item.isEligibleForNgoFree || item.discountPrice == 0.0 || (isAfterPickup && isNgoWindow)

    // Dynamic resolution of the merchant badge
    val effectiveStatus = when {
        item.quantity <= 0 -> "SOLD OUT"
        item.status.equals("PAUSED", ignoreCase = true) -> "PAUSED"
        !isAfterPickup -> "ACTIVE"
        isNgoWindow -> "NGO RESCUE"
        else -> "PAUSED"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick(item.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (item.imageBitmap != null) {
                        Image(
                            bitmap = item.imageBitmap.asImageBitmap(),
                            contentDescription = item.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_store),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = item.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isNgoRescueTier) {
                            Text(
                                text = "FREE (NGO)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "RM %.2f".format(item.discountPrice),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "RM %.2f".format(item.originalPrice),
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        val badgeText = if (isNgoRescueTier) "-100%" else "-${item.discountPercent}%"
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.remaining_count, item.quantity),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val pickupWindowDisplay = if (item.pickupStart.isNotBlank() && item.pickupEnd.isNotBlank()) {
                        "${item.pickupStart} - ${item.pickupEnd}"
                    } else {
                        item.pickupEnd.ifBlank { "Pickup time pending" }
                    }

                    Text(
                        text = "Pickup: $pickupWindowDisplay",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Status styling according to the resolved status
                val (statusBg, statusText) = when (effectiveStatus) {
                    "ACTIVE" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    "NGO RESCUE" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                    "SOLD OUT" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = effectiveStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEditClick(item.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.btn_edit))
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            }
        }
    }
}

@Preview(name = "My Listing Screen - Light", showBackground = true)
@Preview(name = "My Listing Screen - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MyListingScreenPreview() {
    BiteSaversTheme {
        MyListingScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            onNavigateToAddFood = {},
            onNavigateToEditFood = {}
        )
    }
}
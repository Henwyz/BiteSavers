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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingScreen(
    viewModel: InventoryViewModel,
    onNavigateToAddFood: () -> Unit,
    onNavigateToEditFood: (String) -> Unit = {}
) {
    val listings by viewModel.listings.collectAsState()

    // Compare status case-insensitively to match both "ACTIVE" and "Active"
    val activeCount = listings.count {
        it.status.equals("Active", ignoreCase = true) && !isPickupEnd(it.pickupEnd)
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
                // Uses painterResource for vector add icon
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


// Checks if current system time has passed pickup end time, supporting multi-day and 12h/24h formats
// Checks if current system time has passed pickup end time, supporting multi-day and 12h/24h formats
fun isPickupEnd(pickupEndTimeStr: String?): Boolean {
    if (pickupEndTimeStr.isNullOrBlank()) return false
    return try {
        val trimmed = pickupEndTimeStr.trim()
        val is12Hour = trimmed.endsWith("AM", ignoreCase = true) || trimmed.endsWith("PM", ignoreCase = true)
        val parser = if (is12Hour) {
            SimpleDateFormat("hh:mm a", Locale.US)
        } else {
            SimpleDateFormat("HH:mm", Locale.US)
        }

        val parsedDate = parser.parse(trimmed.take(8)) ?: return false

        val endCalendar = Calendar.getInstance().apply { time = parsedDate }
        val endHour = endCalendar.get(Calendar.HOUR_OF_DAY)
        val endMinute = endCalendar.get(Calendar.MINUTE)

        val currentCal = Calendar.getInstance()
        val currentHour = currentCal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentCal.get(Calendar.MINUTE)

        // Convert both to total minutes from midnight for foolproof comparison
        val currentTotalMinutes = currentHour * 60 + currentMinute
        val endTotalMinutes = endHour * 60 + endMinute

        // If it's early morning (e.g., 00:00 to 05:59) and the pickup window was in the evening (e.g., 19:00 onwards),
        // the pickup window definitely happened yesterday and has ended.
        if (currentHour < 6 && endHour >= 18) {
            return true
        }

        currentTotalMinutes > endTotalMinutes
    } catch (_: Exception) {
        false
    }
}

@Composable
fun ListingCard(
    item: ListingItem,
    onEditClick: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isExpired = isPickupEnd(item.pickupEnd)
    val isNgoRescueTier = item.isEligibleForNgoFree || item.discountPrice == 0.0 || isExpired

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

                    // Construct explicit pickup duration string
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

                // Determine active phase using pickup and store cleanup boundaries
                val isPausedState = item.status.equals("PAUSED", ignoreCase = true)
                val isAfterPickup = isPickupEnd(item.pickupEnd)
                val isBeforeCleanup = TimeUtils.isCurrentTimeWithin(item.pickupEnd, item.cleanupEndTime) ||
                        (TimeUtils.getCurrentMinutesOfDay() <= TimeUtils.timeStringToMinutes(item.pickupEnd))

                // Compute badge display for merchant: ACTIVE, NGO RESCUE, or PAUSED
                val effectiveStatus = when {
                    isPausedState -> "PAUSED"
                    // Active during or before pickup window
                    !isAfterPickup -> "ACTIVE"
                    // NGO Rescue active between pickup end and cleanup end time
                    isAfterPickup && TimeUtils.isCurrentTimeWithin(item.pickupEnd, item.cleanupEndTime) -> "NGO RESCUE"
                    // Past cleanup/closing time (e.g., 2:30 AM), automatically pause/close the listing for the day
                    else -> "PAUSED"
                }

                val (statusBg, statusText) = when (effectiveStatus) {
                    "ACTIVE" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    "NGO RESCUE" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
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
package com.example.bitesavers.business.inventory.ui

import android.app.TimePickerDialog
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.business.inventory.data.ListingItem
import com.example.bitesavers.business.inventory.logic.InventoryViewModel
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.ui.theme.BiteSaversTheme
import com.example.bitesavers.util.DynamicPricingEngine
import java.io.File
import java.util.Locale
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val editingItem = viewModel.selectedItemForEdit

    var foodName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Bakery") }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var originalPrice by remember { mutableStateOf("") }
    var discountPrice by remember { mutableStateOf("") }
    var discountBadge by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var weightKg by remember { mutableStateOf("0.35") }
    var pickupStartTime by remember { mutableStateOf("05:00 PM") }
    var pickupEndTime by remember { mutableStateOf("07:00 PM") }
    var selectedBoxId by remember { mutableStateOf<String?>(null) }
    var isBoxDropdownExpanded by remember { mutableStateOf(false) }

    // Toggle for dynamic pricing info dialog
    var showPricingInfoDialog by remember { mutableStateOf(false) }

    // selects photo and takes photo
    var myFoodImage by remember { mutableStateOf<Bitmap?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var showImagePick by remember { mutableStateOf(false) }
    var showFullImagePreview by remember { mutableStateOf(false) }

    LaunchedEffect(editingItem, viewModel.defaultPickupStart, viewModel.defaultPickupEnd) {
        viewModel.fetchStorageBoxes()
        foodName = editingItem?.name ?: ""
        description = editingItem?.description ?: ""
        category = editingItem?.category ?: "Bakery"
        originalPrice = editingItem?.originalPrice?.let { if (it > 0) it.toString() else "" } ?: ""
        discountPrice = editingItem?.discountPrice?.let { if (it > 0) "%.2f".format(it) else "" } ?: ""
        quantity = editingItem?.quantity?.let { if (it > 0) it.toString() else "1" } ?: "1"
        weightKg = editingItem?.weightKg?.toString() ?: "0.35"
        selectedBoxId = editingItem?.storageBoxId

        // Use editing item's time, or fall back to store's automated suggestion
        pickupStartTime = editingItem?.pickupStart ?: viewModel.defaultPickupStart
        pickupEndTime = editingItem?.pickupEnd ?: viewModel.defaultPickupEnd

        myFoodImage = editingItem?.imageBitmap
        existingImageUrl = editingItem?.imageUrl

        val orig = originalPrice.toDoubleOrNull() ?: 0.0
        val disc = discountPrice.toDoubleOrNull() ?: 0.0
        if (orig > 0.0 && disc > 0.0) {
            val pct = (((orig - disc) / orig) * 100).toInt()
            discountBadge = "-$pct%"
        }
    }

    // Validate mandatory form fields before enabling publish
    val isFormValid = foodName.isNotBlank() &&
            category.isNotBlank() &&
            !selectedBoxId.isNullOrBlank() &&
            (originalPrice.toDoubleOrNull() ?: 0.0) > 0.0 &&
            (discountPrice.toDoubleOrNull() ?: 0.0) > 0.0 &&
            ((discountPrice.toDoubleOrNull() ?: 0.0) <= (originalPrice.toDoubleOrNull() ?: 0.0)) &&
            (quantity.toIntOrNull() ?: 0) > 0 &&
            pickupStartTime.isNotBlank() &&
            pickupEndTime.isNotBlank()

    // Activity launcher for gallery media selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            myFoodImage = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Activity launcher for camera capture via temporary content URI
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            myFoodImage = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, tempImageUri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, tempImageUri!!)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }

    // Explains dynamic time-decay pricing tiers to the merchant
    if (showPricingInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPricingInfoDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.label_discount_price_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.label_discount_price_info_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showPricingInfoDialog = false }) {
                    Text(
                        text = stringResource(R.string.action_got_it),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // show select picture from gallery or take photo at bottom
    if (showImagePick) {
        ModalBottomSheet(
            onDismissRequest = { showImagePick = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_choose_photo),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showImagePick = false
                            val uri = getTmpFileUri(context)
                            tempImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.action_take_photo),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showImagePick = false
                            galleryLauncher.launch("image/*")
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_image),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.action_choose_gallery),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    // click the picture then zoom it
    if (showFullImagePreview && (myFoodImage != null || !existingImageUrl.isNullOrBlank())) {
        Dialog(onDismissRequest = { showFullImagePreview = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (myFoodImage != null) {
                        Image(
                            bitmap = myFoodImage!!.asImageBitmap(),
                            contentDescription = stringResource(R.string.content_desc_full_image_preview),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else if (!existingImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = existingImageUrl,
                            contentDescription = stringResource(R.string.content_desc_full_image_preview),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = {
                                showFullImagePreview = false
                                showImagePick = true
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.action_retake_change),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = { showFullImagePreview = false }) {
                            Text(
                                text = stringResource(R.string.action_close),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_food_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.selectedItemForEdit = null
                        onNavigateBack()
                    }) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.cd_navigate_back),
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // place of upload picture or preview
            val hasPhoto = myFoodImage != null || !existingImageUrl.isNullOrBlank()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        if (!hasPhoto) {
                            showImagePick = true
                        } else {
                            showFullImagePreview = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (myFoodImage != null) {
                    Image(
                        bitmap = myFoodImage!!.asImageBitmap(),
                        contentDescription = stringResource(R.string.cd_food_preview),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!existingImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = existingImageUrl,
                        contentDescription = stringResource(R.string.content_desc_food_preview),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                if (hasPhoto) {
                    IconButton(
                        onClick = {
                            myFoodImage = null
                            existingImageUrl = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(30.dp)
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Text(
                            text = "X",
                            color = MaterialTheme.colorScheme.surface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.upload_photo_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.upload_photo_subtitle),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            FormField(
                label = stringResource(R.string.label_food_name),
                value = foodName,
                placeholder = stringResource(R.string.hint_food_name),
                onValueChange = { foodName = it }
            )

            FormField(
                label = stringResource(R.string.label_description),
                value = description,
                placeholder = stringResource(R.string.hint_description),
                onValueChange = { description = it }
            )

            // Category selection restricted to actual physical food categories
            Column {
                Text(
                    text = stringResource(R.string.label_category),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = isCategoryExpanded,
                    onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DiscoveryCategory.entries
                            .filter { it != DiscoveryCategory.ALL && !it.name.equals("FREE", ignoreCase = true) }
                            .forEach { item ->
                                val displayName = item.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = displayName,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        category = displayName
                                        isCategoryExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                    }
                }
            }

            // Pickup Window Selection
            Column {
                Text(
                    text = stringResource(R.string.label_pickup_window),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, selectedHour, selectedMinute ->
                                        val amPm = if (selectedHour >= 12) "PM" else "AM"
                                        val hourIn12 = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                                        pickupStartTime = String.format(Locale.getDefault(), "%02d:%02d %s", hourIn12, selectedMinute, amPm)

                                        // Re-evaluate discount recommendation on window duration adjustment
                                        val orig = originalPrice.toDoubleOrNull() ?: 0.0
                                        if (orig > 0.0) {
                                            val (sugPrice, pct) = DynamicPricingEngine.calculateSuggestedPrice(
                                                originalPrice = orig,
                                                pickupEndTimeStr = pickupEndTime,
                                                pickupStartTimeStr = pickupStartTime
                                            )
                                            discountPrice = "%.2f".format(sugPrice)
                                            discountBadge = "-$pct%"
                                        }
                                    },
                                    17, 0, false
                                ).show()
                            }
                    ) {
                        OutlinedTextField(
                            value = pickupStartTime,
                            onValueChange = {},
                            placeholder = { Text(text = "05:00 PM", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(
                        text = stringResource(R.string.text_to),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, selectedHour, selectedMinute ->
                                        val amPm = if (selectedHour >= 12) "PM" else "AM"
                                        val hourIn12 = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                                        pickupEndTime = String.format(Locale.getDefault(), "%02d:%02d %s", hourIn12, selectedMinute, amPm)

                                        // Recalculates discount suggestion when pickup end time changes
                                        val orig = originalPrice.toDoubleOrNull() ?: 0.0
                                        if (orig > 0.0) {
                                            val (sugPrice, pct) = DynamicPricingEngine.calculateSuggestedPrice(
                                                originalPrice = orig,
                                                pickupEndTimeStr = pickupEndTime,
                                                pickupStartTimeStr = pickupStartTime
                                            )
                                            discountPrice = "%.2f".format(sugPrice)
                                            discountBadge = "-$pct%"
                                        }
                                    },
                                    19, 0, false
                                ).show()
                            }
                    ) {
                        OutlinedTextField(
                            value = pickupEndTime,
                            onValueChange = {},
                            placeholder = { Text(text = "07:00 PM", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Original Price and Auto-Calculated Dynamic Discounted Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FormField(
                        label = stringResource(R.string.label_original_price),
                        value = originalPrice,
                        placeholder = stringResource(R.string.hint_original_price),
                        keyboardType = KeyboardType.Decimal,
                        onValueChange = { input ->
                            originalPrice = input
                            val orig = input.toDoubleOrNull() ?: 0.0
                            if (orig > 0.0) {
                                // Automatically triggers algorithm recommendation based on the pickup window
                                val (sugPrice, pct) = DynamicPricingEngine.calculateSuggestedPrice(
                                    originalPrice = orig,
                                    pickupEndTimeStr = pickupEndTime,
                                    pickupStartTimeStr = pickupStartTime
                                )
                                discountPrice = "%.2f".format(sugPrice)
                                discountBadge = "-$pct%"
                            } else {
                                discountPrice = ""
                                discountBadge = ""
                            }
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.label_discount_price),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                // Info icon opens the dynamic pricing policy popup
                                IconButton(
                                    onClick = { showPricingInfoDialog = true },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_info),
                                        contentDescription = stringResource(R.string.cd_pricing_info),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            if (discountBadge.isNotBlank()) {
                                Text(
                                    text = discountBadge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = discountPrice,
                            onValueChange = {},
                            readOnly = true, // Read-only: price strictly follows the platform dynamic algorithm
                            placeholder = { Text(text = stringResource(R.string.hint_discount_price), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            supportingText = {
                                Text(
                                    text = stringResource(R.string.hint_auto_calculated_pricing),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Quantity and Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FormField(
                        label = stringResource(R.string.label_quantity),
                        value = quantity,
                        placeholder = stringResource(R.string.hint_quantity),
                        keyboardType = KeyboardType.Number,
                        onValueChange = { quantity = it }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FormField(
                        label = stringResource(R.string.label_est_weight),
                        value = weightKg,
                        placeholder = stringResource(R.string.hint_est_weight),
                        keyboardType = KeyboardType.Decimal,
                        onValueChange = { weightKg = it }
                    )
                }
            }

            // Storage Box Selector with current temperature display
            Column(modifier = Modifier.fillMaxWidth()){
                // Only allow normal, safe (unlocked) boxes to be assigned
                val availableBoxes = viewModel.storageBoxes.filter { !it.isLocked }
                val hasBoxes = availableBoxes.isNotEmpty()
                val selectedBox = availableBoxes.firstOrNull { it.id == selectedBoxId }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_storage_box),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (selectedBox != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.format_current_temp, selectedBox.currentTemperature),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                val displayBoxText = when {
                    !hasBoxes -> stringResource(R.string.no_storage_box_warning)
                    selectedBox != null -> "${selectedBox.boxCode} (${selectedBox.storageType ?: "General"})"
                    else -> stringResource(R.string.placeholder_select_storage_box)
                }

                ExposedDropdownMenuBox(
                    expanded = isBoxDropdownExpanded && hasBoxes,
                    onExpandedChange = {
                        if (hasBoxes) isBoxDropdownExpanded = !isBoxDropdownExpanded
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = displayBoxText,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            if (hasBoxes) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBoxDropdownExpanded)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = if (!hasBoxes) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (!hasBoxes) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            focusedTextColor = if (!hasBoxes) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = if (!hasBoxes) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        ),
                        supportingText = {
                            if (!hasBoxes) {
                                Text(
                                    text = stringResource(R.string.no_storage_box_warning),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = isBoxDropdownExpanded && hasBoxes,
                        onDismissRequest = { isBoxDropdownExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        availableBoxes.forEach { box ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = box.boxCode,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = box.storageType ?: "General",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.format_current_temp, box.currentTemperature),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedBoxId = box.id
                                    isBoxDropdownExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Safety Notice / Warning Banner (Yellow Container)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (!hasBoxes) "⚠️" else "🛡️",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (!hasBoxes) {
                                stringResource(R.string.storage_box_none_available_warning)
                            } else {
                                stringResource(R.string.storage_box_safety_warning)
                            },
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val finalDiscountPrice = discountPrice.toDoubleOrNull() ?: 0.0

                    if (editingItem != null) {
                        val updated = editingItem.copy(
                            name = foodName.trim(),
                            description = description.trim(),
                            category = category.trim(),
                            originalPrice = originalPrice.toDoubleOrNull() ?: editingItem.originalPrice,
                            discountPrice = finalDiscountPrice,
                            quantity = quantity.toIntOrNull() ?: editingItem.quantity,
                            weightKg = weightKg.toDoubleOrNull() ?: editingItem.weightKg,
                            pickupStart = pickupStartTime.trim(),
                            pickupEnd = pickupEndTime.trim(),
                            storageBoxId = selectedBoxId,
                            imageUrl = if (myFoodImage != null) null else existingImageUrl,
                            imageBitmap = myFoodImage
                        )
                        viewModel.updateListing(updated)
                    } else {
                        val newItem = ListingItem(
                            id = java.util.UUID.randomUUID().toString(),
                            name = foodName.trim(),
                            description = description.trim().ifBlank { "" },
                            category = category.trim(),
                            originalPrice = originalPrice.toDoubleOrNull() ?: 0.0,
                            discountPrice = finalDiscountPrice,
                            quantity = quantity.toIntOrNull() ?: 1,
                            weightKg = weightKg.toDoubleOrNull() ?: 0.35,
                            pickupStart = pickupStartTime.trim(),
                            pickupEnd = pickupEndTime.trim(),
                            status = "ACTIVE",
                            storageBoxId = selectedBoxId,
                            imageBitmap = myFoodImage
                        )
                        viewModel.addListing(newItem)
                    }
                    viewModel.selectedItemForEdit = null
                    onNavigateBack()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (editingItem != null) {
                        stringResource(R.string.btn_update_listing)
                    } else {
                        stringResource(R.string.btn_publish_listing)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

fun getTmpFileUri(context: android.content.Context): Uri {
    val tmpFile = File.createTempFile("tmp_food_image", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        tmpFile
    )
}

@Composable
fun FormField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Add Food Screen - Light", showBackground = true)
@Preview(name = "Add Food Screen - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AddFoodScreenPreview() {
    BiteSaversTheme {
        AddFoodScreen(
            viewModel = viewModel(),
            onNavigateBack = {}
        )
    }
}
package com.example.bitesavers.business.inventory.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.inputmethodservice.Keyboard
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.toString
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.bitesavers.business.inventory.logic.InventoryViewModel
import com.example.bitesavers.R
import com.example.bitesavers.business.inventory.data.ListingItem
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.ui.theme.BiteSaversTheme
import java.io.File
import kotlin.contracts.contract

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
    var quantity by remember { mutableStateOf("") }
    var expiryTime by remember { mutableStateOf("06:00 PM")}
    var pickupStartTime by remember { mutableStateOf("05:00 PM") }
    var pickupEndTime by remember { mutableStateOf("07:00 PM") }

    var myFoodImage by remember { mutableStateOf<Bitmap?>(null) }
    var showImagePick by remember { mutableStateOf(false) }
    var showFullImagePreview by remember { mutableStateOf(false) }

    LaunchedEffect(editingItem) {
        foodName = editingItem?.name ?: ""
        description = editingItem?.description ?: ""
        category = editingItem?.category ?: "Bakery"
        originalPrice = editingItem?.originalPrice?.toString() ?: ""
        discountPrice = editingItem?.discountPrice?.toString() ?: ""
        quantity = editingItem?.quantity?.toString() ?: ""
        expiryTime = editingItem?.expiryTime ?: "06:00 PM"
        myFoodImage = editingItem?.imageBitmap
    }

    val isFormValid = foodName.isNotBlank() &&
            category.isNotBlank() &&
            (originalPrice.toDoubleOrNull() ?: 0.0) > 0.0 &&
            (discountPrice.toDoubleOrNull() ?: 0.0) > 0.0 &&
            ((discountPrice.toDoubleOrNull() ?: 0.0) <= (originalPrice.toDoubleOrNull() ?: 0.0)) &&
            (quantity.toIntOrNull() ?: 0) > 0 &&
            expiryTime.isNotBlank()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            myFoodImage = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver,it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

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
                    text = "Choose Photo Source",
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
                    Text(text = "\uD83D\uDCF7", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Take Photo",
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
                    Text(text = "\uD83D\uDDBC\uFE0F", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Choose from gallery",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    // click the picture then zoom it
    if (showFullImagePreview && myFoodImage != null) {
        Dialog(onDismissRequest = { showFullImagePreview = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                           MaterialTheme.colorScheme.surface,
                           RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = myFoodImage!!.asImageBitmap(),
                        contentDescription = "Full Image Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        contentScale = ContentScale.Fit
                    )
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
                            Text(text = "Retake / Change",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showFullImagePreview = false }) {
                            Text(text = "Close",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f),
                                    CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "<", color = MaterialTheme.colorScheme.onSecondary, fontSize = 14.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary)
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
                    .clickable{
                        if (myFoodImage == null) {
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
                        contentDescription = "Food Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { myFoodImage = null },
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
                            Text(text = "\uD83D\uDCF7", fontSize = 20.sp)
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded)},
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
                            .filter { it != DiscoveryCategory.ALL }
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

            //original price and discount price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FormField(
                        label = "Original Price (RM)",
                        value = originalPrice,
                        placeholder = "4.50",
                        keyboardType = KeyboardType.Decimal,
                        onValueChange = { originalPrice = it }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    FormField(
                        label = "Discount Price (RM)",
                        value = discountPrice,
                        placeholder = "1.50",
                        keyboardType = KeyboardType.Decimal,
                        onValueChange = { discountPrice = it }
                    )
                }
            }

            // quantity and expiry time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FormField(
                        label = stringResource(R.string.label_quantity),
                        value = quantity,
                        placeholder = "8",
                        keyboardType = KeyboardType.Number,
                        onValueChange = { quantity = it }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FormField(
                        label = stringResource(R.string.label_expiry_time),
                        value = expiryTime,
                        placeholder = "06:00 PM",
                        onValueChange = { expiryTime = it }
                    )
                }
            }

            // Pickup Window
            Column{
                Text(
                    text = "Pickup Window",
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
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = pickupStartTime,
                            onValueChange = { pickupStartTime = it },
                            placeholder = { Text("05:00 PM",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(
                        text = "to",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = pickupEndTime,
                            onValueChange = { pickupEndTime = it },
                            placeholder = { Text("07:00 PM",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (editingItem != null) {
                        // update existing item (edit mode)
                        val updated = editingItem.copy(
                            name = foodName.trim(),
                            description = description.trim(),
                            category = category.trim(),
                            originalPrice = originalPrice.toDoubleOrNull() ?: editingItem.originalPrice,
                            discountPrice = discountPrice.toDoubleOrNull() ?: editingItem.discountPrice,
                            quantity = quantity.toIntOrNull() ?: editingItem.quantity,
                            expiryTime = expiryTime.trim(),
                            imageBitmap = myFoodImage
                        )
                        viewModel.updateListing(updated)
                    } else {
                        val newItem = ListingItem(
                            id = System.currentTimeMillis().toString(),
                            name = foodName.trim(),
                            description = description.trim().ifBlank { "" },
                            category = category.trim(),
                            originalPrice = originalPrice.toDoubleOrNull() ?: 0.0,
                            discountPrice = discountPrice.toDoubleOrNull() ?: 0.0,
                            quantity = quantity.toIntOrNull() ?: 1,
                            expiryTime = expiryTime.trim(),
                            status = "Active",
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
                    text = stringResource(R.string.btn_publish_listing),
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


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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toString
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.business.inventory.logic.InventoryViewModel
import com.example.bitesavers.R
import com.example.bitesavers.business.inventory.data.ListingItem
import com.example.bitesavers.ui.theme.BiteSaversTheme
import kotlin.contracts.contract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var foodName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Bakery") }
    var originalPrice by remember { mutableStateOf("") }
    var discountPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expiryTime by remember { mutableStateOf("06:00 PM")}

    var myFoodImage by remember { mutableStateOf<Bitmap?>(null) }
    var showImagePick by remember { mutableStateOf(false) }

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

    val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
            ) { bitmap: Bitmap? ->
        bitmap?.let { myFoodImage = it }
    }

    if (showImagePick) {
        AlertDialog(
            onDismissRequest = { showImagePick = false },
            title = { Text(text = stringResource(R.string.upload_photo_title)) },
            text = { Text(text = stringResource(R.string.upload_photo_subtitle))},
            confirmButton = {
                TextButton(
                    onClick = {
                        showImagePick = false
                        cameraLauncher.launch()
                    }
                ) {
                    Text(text = "Camera")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImagePick = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text(text = "Gallery")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_food_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                    CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "<", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor =MaterialTheme.colorScheme.onPrimary)
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
                        showImagePick = true
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

            FormField(
                label = stringResource(R.string.label_category),
                value = category,
                placeholder = "Bakery",
                onValueChange = { category = it }
            )

            Row(
                modifier = Modifier.fillMaxSize(),
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

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val orig = originalPrice.toDoubleOrNull() ?: 0.0
                    val disc = discountPrice.toDoubleOrNull() ?: 0.0
                    val percent = if (orig > 0)
                            (((orig - disc) / orig) * 100).toInt() else 0
                    val newItem = ListingItem(
                        id = System.currentTimeMillis().toString(),
                        name = foodName.ifBlank { "Untitled Food" },
                        category = category.ifBlank { "Bakery" },
                        originalPrice = orig,
                        discountPrice = disc,
                        quantity = quantity.toIntOrNull() ?:1,
                        expiryTime = expiryTime,
                        status = "Active"
                    )
                    viewModel.addListing(newItem)
                    onNavigateBack()
                },
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}


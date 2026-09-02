package com.example.bitesavers.business.restaurant.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.business.restaurant.logic.RegisterRestaurantViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterRestaurantScreen(
    viewModel: RegisterRestaurantViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onRestaurantRegistered: () -> Unit
) {
    val context = LocalContext.current

    var showImagePick by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateStoreImageUri(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            viewModel.updateStoreImageUri(tempImageUri)
        }
    }

    val ssmDocPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateSsmDocUri(it) }
    }

    // Modal Bottom Sheet for Store Photo Source Selection
    if (showImagePick) {
        ModalBottomSheet(
            onDismissRequest = { showImagePick = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.choose_store_photo_source),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showImagePick = false
                            val tmpFile = File.createTempFile("store_cam_", ".jpg", context.cacheDir).apply {
                                createNewFile()
                                deleteOnExit()
                            }
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tmpFile)
                            tempImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📸", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(R.string.take_photo), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
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
                    Text(text = "🖼️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(R.string.choose_from_gallery), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Main layout with green header matching other screens
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.cd_back_button),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Text(
                text = stringResource(R.string.register_restaurant_title),
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Box(modifier = Modifier.size(40.dp)) // Spacer for title balance
        }

        Spacer(Modifier.height(8.dp))

        // Form Body Content with Background Color Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Restaurant Name
            FormLabel(stringResource(R.string.restaurant_name_label))
            OutlinedTextField(
                value = viewModel.restaurantName,
                onValueChange = { viewModel.updateRestaurantName(it) },
                placeholder = { Text(stringResource(R.string.restaurant_name_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Contact Phone
            FormLabel(stringResource(R.string.contact_phone_label))
            OutlinedTextField(
                value = viewModel.contactPhone,
                onValueChange = { viewModel.updateContactPhone(it) },
                placeholder = { Text(stringResource(R.string.contact_phone_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Address & Geocoding Fallback Option
            FormLabel(stringResource(R.string.address_label))
            OutlinedTextField(
                value = viewModel.address,
                onValueChange = { viewModel.updateAddress(it) },
                placeholder = { Text(stringResource(R.string.address_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30),
                maxLines = 3
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = { viewModel.useDefaultPenangLocation() },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.use_default_penang_fallback), fontSize = 11.sp)
            }

            Spacer(Modifier.height(10.dp))

            // 12-Digit SSM Number
            FormLabel(stringResource(R.string.ssm_number_label))
            OutlinedTextField(
                value = viewModel.ssmNumber,
                onValueChange = { if (it.length <= 12) viewModel.updateSsmNumber(it) },
                placeholder = { Text(stringResource(R.string.ssm_number_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Store Front Image Picker Button
            FormLabel(stringResource(R.string.store_banner_label))
            Button(
                onClick = { showImagePick = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    if (viewModel.storeImageUri != null) Icons.Filled.CheckCircle else Icons.Filled.UploadFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (viewModel.storeImageUri != null) stringResource(R.string.store_photo_attached) else stringResource(R.string.take_upload_store_photo),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            // SSM Document Upload Button
            FormLabel(stringResource(R.string.ssm_doc_label))
            Button(
                onClick = { ssmDocPickerLauncher.launch("*/*") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    if (viewModel.ssmDocUri != null) Icons.Filled.CheckCircle else Icons.Filled.UploadFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (viewModel.ssmDocUri != null) stringResource(R.string.ssm_doc_uploaded) else stringResource(R.string.upload_ssm_doc),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Operating Hours
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel(stringResource(R.string.opening_time_label))
                    OutlinedTextField(
                        value = viewModel.openingTime,
                        onValueChange = { viewModel.updateOpeningTime(it) },
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel(stringResource(R.string.closing_time_label))
                    OutlinedTextField(
                        value = viewModel.closingTime,
                        onValueChange = { viewModel.updateClosingTime(it) },
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Cleanup End Time
            FormLabel(stringResource(R.string.cleanup_end_time_label))
            OutlinedTextField(
                value = viewModel.cleanupEndTime,
                onValueChange = { viewModel.updateCleanupEndTime(it) },
                placeholder = { Text(stringResource(R.string.cleanup_end_time_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.restaurant_verification_note),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Submit Button
            Button(
                onClick = {
                    viewModel.registerRestaurant {
                        onRestaurantRegistered()
                    }
                },
                enabled = viewModel.restaurantName.isNotBlank() &&
                        viewModel.address.isNotBlank() &&
                        viewModel.contactPhone.isNotBlank() &&
                        viewModel.ssmNumber.length == 12,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = stringResource(R.string.save_and_continue),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline
)
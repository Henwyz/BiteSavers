package com.example.bitesavers.business.restaurant.ui

import android.app.TimePickerDialog
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.business.restaurant.logic.RegisterRestaurantViewModel
import java.io.File
import java.util.Calendar
import java.util.Locale

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

    var showOpeningTimePicker by remember { mutableStateOf(false) }
    var showClosingTimePicker by remember { mutableStateOf(false) }
    var showCleanupTimePicker by remember { mutableStateOf(false) }

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

    // Opening Time Dialog with AM/PM
    if (showOpeningTimePicker) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                val formatted = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
                viewModel.updateOpeningTime(formatted)
                showOpeningTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).apply {
            setOnDismissListener { showOpeningTimePicker = false }
        }.show()
    }

    // Closing Time Dialog with AM/PM
    if (showClosingTimePicker) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                val formatted = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
                viewModel.updateClosingTime(formatted)
                showClosingTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).apply {
            setOnDismissListener { showClosingTimePicker = false }
        }.show()
    }

    // Cleanup Time Dialog with AM/PM
    if (showCleanupTimePicker) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                val formatted = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
                viewModel.updateCleanupEndTime(formatted)
                showCleanupTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).apply {
            setOnDismissListener { showCleanupTimePicker = false }
        }.show()
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Text(
                text = stringResource(R.string.register_restaurant_title),
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Box(modifier = Modifier.size(40.dp))
        }

        Spacer(Modifier.height(8.dp))

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
                isError = viewModel.restaurantNameError != null,
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )
            if (viewModel.restaurantNameError != null) {
                Text(text = viewModel.restaurantNameError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Contact Phone
            FormLabel(stringResource(R.string.contact_phone_label))
            OutlinedTextField(
                value = viewModel.contactPhone,
                onValueChange = { viewModel.updateContactPhone(it) },
                placeholder = { Text(stringResource(R.string.contact_phone_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = viewModel.contactPhoneError != null,
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )
            if (viewModel.contactPhoneError != null) {
                Text(text = viewModel.contactPhoneError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Address Field
            FormLabel(stringResource(R.string.address_label))
            OutlinedTextField(
                value = viewModel.address,
                onValueChange = { viewModel.updateAddress(it) },
                placeholder = { Text(stringResource(R.string.address_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                isError = viewModel.addressError != null,
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30),
                maxLines = 3
            )
            if (viewModel.addressError != null) {
                Text(text = viewModel.addressError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
            }

            Spacer(Modifier.height(16.dp))

            // 12-Digit SSM Number with Error State
            FormLabel(stringResource(R.string.ssm_number_label))
            OutlinedTextField(
                value = viewModel.ssmNumber,
                onValueChange = { viewModel.updateSsmNumber(it) },
                placeholder = { Text(stringResource(R.string.ssm_number_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = viewModel.ssmError != null,
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )
            if (viewModel.ssmError != null) {
                Text(
                    text = viewModel.ssmError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

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
            if (viewModel.storeImageError != null) {
                Text(text = viewModel.storeImageError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
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
            if (viewModel.ssmDocError != null) {
                Text(text = viewModel.ssmDocError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Operating Hours
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel(stringResource(R.string.opening_time_label))
                    Box(modifier = Modifier.clickable { showOpeningTimePicker = true }) {
                        OutlinedTextField(
                            value = viewModel.openingTime,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            colors = textFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            singleLine = true
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel(stringResource(R.string.closing_time_label))
                    Box(modifier = Modifier.clickable { showClosingTimePicker = true }) {
                        OutlinedTextField(
                            value = viewModel.closingTime,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            colors = textFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Cleanup End Time with 12-Hour Picker & Validation Error
            FormLabel(stringResource(R.string.cleanup_end_time_label))
            Box(modifier = Modifier.clickable { showCleanupTimePicker = true }) {
                OutlinedTextField(
                    value = viewModel.cleanupEndTime,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    isError = viewModel.cleanupTimeError != null,
                    colors = textFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    singleLine = true
                )
            }
            if (viewModel.cleanupTimeError != null) {
                Text(
                    text = viewModel.cleanupTimeError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

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
                enabled = !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.save_and_continue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
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
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline
)

@Preview(showBackground = true)
@Composable
fun RegisterRestaurantScreenPreview() {
    MaterialTheme {
        RegisterRestaurantScreen(onRestaurantRegistered = {})
    }
}
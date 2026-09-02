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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.business.restaurant.logic.RegisterRestaurantViewModel
import com.example.bitesavers.ui.theme.BiteSaversTheme
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
                    text = "Choose Store Photo Source",
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
                    Text(text = "Take Photo", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
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
                    Text(text = "Choose from Gallery", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
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
                text = "Register Restaurant 🏪",
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Box(modifier = Modifier.size(40.dp)) // Spacer for title balance
        }

        Spacer(Modifier.height(8.dp))

        // Form Body Content with SoftGreen / Background Color Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Restaurant Name
            FormLabel("Restaurant Name")
            OutlinedTextField(
                value = viewModel.restaurantName,
                onValueChange = { viewModel.updateRestaurantName(it) },
                placeholder = { Text("e.g. BiteSaver Cafe", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Contact Phone
            FormLabel("Contact Phone Number")
            OutlinedTextField(
                value = viewModel.contactPhone,
                onValueChange = { viewModel.updateContactPhone(it) },
                placeholder = { Text("e.g. 0123456789", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Address & Geocoding Fallback Option
            FormLabel("Address")
            OutlinedTextField(
                value = viewModel.address,
                onValueChange = { viewModel.updateAddress(it) },
                placeholder = { Text("e.g. 123, Jalan Ampang", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                Text("Use Default Penang Fallback", fontSize = 11.sp)
            }

            Spacer(Modifier.height(10.dp))

            // 12-Digit SSM Number
            FormLabel("12-Digit SSM Business Number")
            OutlinedTextField(
                value = viewModel.ssmNumber,
                onValueChange = { if (it.length <= 12) viewModel.updateSsmNumber(it) },
                placeholder = { Text("Enter 12-digit registration code", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Store Front Image Picker Button
            FormLabel("Store Front / Banner Image")
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
                    text = if (viewModel.storeImageUri != null) "Store Photo Attached ✓" else "Take or Upload Store Photo",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            // SSM Document Upload Button
            FormLabel("SSM Certificate Document")
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
                    text = if (viewModel.ssmDocUri != null) "SSM Document Uploaded ✓" else "Upload SSM Document (PDF/Image)",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Operating Hours
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Opening Time")
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
                    FormLabel("Closing Time")
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
            FormLabel("Cleanup End Time")
            OutlinedTextField(
                value = viewModel.cleanupEndTime,
                onValueChange = { viewModel.updateCleanupEndTime(it) },
                placeholder = { Text("e.g. 22:30", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Note: New registrations require review and verification (is_verified = false) before approval.",
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
                    text = "Save & Continue",
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

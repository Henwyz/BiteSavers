package com.example.bitesavers.customer.profile.logic

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.profile.data.NgoApplicationUiModel
import com.example.bitesavers.customer.profile.data.NgoCauseCategory
import com.example.bitesavers.customer.profile.data.NgoRegistrationType
import com.example.bitesavers.customer.profile.data.UserProfileUiModel
import com.example.bitesavers.data.mapper.toInsertDto
import com.example.bitesavers.data.mapper.toUiModel
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.repository.ProfileRepository
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.OrderRepository
import com.example.bitesavers.data.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NgoFormMode { REGISTER, EDIT }

private val ALL_VALIDATED_FIELDS = setOf(
    "organizationName", "registrationNumber", "contactPersonName",
    "contactEmail", "contactPhone", "causeCategory", "address", "reasonForChange"
)

private val PLACEHOLDER_PROFILE = UserProfileUiModel(
    id = "",
    name = "Loading…",
    email = "",
    avatarInitials = "?",
    memberSinceLabel = "",
    walletBalance = 0.0,
    mealsRescued = 0,
    moneySaved = 0.0,
    co2ReducedKg = 0.0
)

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()
    private val userRepository = UserRepository()
    private val orderRepository = OrderRepository()
    private val offerRepository = OfferRepository()

    private val _profile = MutableStateFlow(PLACEHOLDER_PROFILE)
    val profile: StateFlow<UserProfileUiModel> = _profile.asStateFlow()

    private val _activeNgoDetails = MutableStateFlow<NgoApplicationUiModel?>(null)
    val activeNgoDetails: StateFlow<NgoApplicationUiModel?> = _activeNgoDetails.asStateFlow()

    private val _hasPendingEdit = MutableStateFlow(false)
    val hasPendingEdit: StateFlow<Boolean> = _hasPendingEdit.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private val _ngoApplication = MutableStateFlow(NgoApplicationUiModel())
    val ngoApplication: StateFlow<NgoApplicationUiModel> = _ngoApplication.asStateFlow()

    private val _fieldErrors = MutableStateFlow(NgoFormErrors())
    val fieldErrors: StateFlow<NgoFormErrors> = _fieldErrors.asStateFlow()

    private val _showTncDialog = MutableStateFlow(false)
    val showTncDialog: StateFlow<Boolean> = _showTncDialog.asStateFlow()

    private val _showNoChangesDialog = MutableStateFlow(false)
    val showNoChangesDialog: StateFlow<Boolean> = _showNoChangesDialog.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    private val _showEditProfileDialog = MutableStateFlow(false)
    val showEditProfileDialog: StateFlow<Boolean> = _showEditProfileDialog.asStateFlow()

    private val _editProfileName = MutableStateFlow("")
    val editProfileName: StateFlow<String> = _editProfileName.asStateFlow()

    private val _editProfileError = MutableStateFlow<String?>(null)
    val editProfileError: StateFlow<String?> = _editProfileError.asStateFlow()

    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile: StateFlow<Boolean> = _isUpdatingProfile.asStateFlow()

    private val _showChangePasswordDialog = MutableStateFlow(false)
    val showChangePasswordDialog: StateFlow<Boolean> = _showChangePasswordDialog.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _isChangingPassword = MutableStateFlow(false)
    val isChangingPassword: StateFlow<Boolean> = _isChangingPassword.asStateFlow()

    private val touchedFields = mutableSetOf<String>()
    private var currentMode = NgoFormMode.REGISTER
    private var pendingSubmitMode = NgoFormMode.REGISTER

    init {
        viewModelScope.launch {
            UserSession.currentUserId.collectLatest { userId ->
                if (userId.isNotBlank()) {
                    loadProfileData(userId)
                }
            }
        }
    }

    private suspend fun loadProfileData(userId: String) {
        _isLoading.value = true
        _loadError.value = null
        try {
            val userDto = repository.getUser(userId)
            val baseProfile = userDto.toUiModel()

            val rawOrders = orderRepository.fetchCustomerOrders()
            val completedOrders = rawOrders.filter { it.status.equals("COMPLETED", ignoreCase = true) }

            var computedMoneySaved = 0.0
            var totalWeightKg = 0.0
            var completedCount = 0

            completedOrders.map { order ->
                viewModelScope.async {
                    val offerId = order.offerId.orEmpty() // 👈 Safely unwrap offerId
                    val offer = if (offerId.isNotBlank()) offerRepository.fetchOfferById(offerId) else null
                    val originalTotal = (offer?.originalPrice ?: 0.0) * order.quantity
                    val saved = (originalTotal - order.totalPrice).coerceAtLeast(0.0)
                    val weight = order.totalWeightKg ?: (0.3 * order.quantity)
                    Triple(saved, weight, order.quantity)
                }
            }.awaitAll().forEach { (saved, weight, qty) ->
                computedMoneySaved += saved
                totalWeightKg += weight
                completedCount += qty
            }

            val computedCo2 = totalWeightKg * 2.5

            _profile.value = baseProfile.copy(
                mealsRescued = if (completedCount > 0) completedCount else baseProfile.mealsRescued,
                moneySaved = computedMoneySaved,
                co2ReducedKg = computedCo2
            )

            val applications = repository.getNgoApplications(userId)
            _activeNgoDetails.value = applications.firstOrNull { it.status == "APPROVED" }?.toUiModel()
            _hasPendingEdit.value = applications.firstOrNull()?.status == "PENDING"
        } catch (e: Exception) {
            _loadError.value = "Couldn't load profile: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch { loadProfileData(UserSession.currentUserId.value) }
    }

    // ---------- Consumer Edit Profile Handlers ----------

    fun openEditProfileDialog() {
        _editProfileName.value = _profile.value.name
        _editProfileError.value = null
        _showEditProfileDialog.value = true
    }

    fun dismissEditProfileDialog() {
        _showEditProfileDialog.value = false
        _editProfileError.value = null
    }

    fun onEditProfileNameChange(newName: String) {
        _editProfileName.value = newName
        if (_editProfileError.value != null && newName.isNotBlank()) {
            _editProfileError.value = null
        }
    }

    fun saveProfileChanges() {
        val trimmedName = _editProfileName.value.trim()
        if (trimmedName.isBlank()) {
            _editProfileError.value = "Name cannot be empty"
            return
        }

        val userId = UserSession.currentUserId.value
        viewModelScope.launch {
            _isUpdatingProfile.value = true
            val success = userRepository.updateUserName(userId, trimmedName)
            if (success) {
                loadProfileData(userId)
                _showEditProfileDialog.value = false
            } else {
                _editProfileError.value = "Failed to update profile. Please try again."
            }
            _isUpdatingProfile.value = false
        }
    }

    // ---------- Change Password Handlers ----------

    fun openChangePasswordDialog() {
        _newPassword.value = ""
        _confirmPassword.value = ""
        _passwordError.value = null
        _showChangePasswordDialog.value = true
    }

    fun dismissChangePasswordDialog() {
        _showChangePasswordDialog.value = false
        _passwordError.value = null
    }

    fun onNewPasswordChange(input: String) {
        _newPassword.value = input
        if (_passwordError.value != null) _passwordError.value = null
    }

    fun onConfirmPasswordChange(input: String) {
        _confirmPassword.value = input
        if (_passwordError.value != null) _passwordError.value = null
    }

    fun savePasswordChanges(
        lengthErrorMessage: String,
        mismatchErrorMessage: String,
        onSuccess: () -> Unit
    ) {
        val pass = _newPassword.value
        val confirm = _confirmPassword.value

        if (pass.length < 6) {
            _passwordError.value = lengthErrorMessage
            return
        }
        if (pass != confirm) {
            _passwordError.value = mismatchErrorMessage
            return
        }

        viewModelScope.launch {
            _isChangingPassword.value = true
            try {
                repository.updateUserPassword(pass)
                _showChangePasswordDialog.value = false
                onSuccess()
            } catch (e: Exception) {
                _passwordError.value = e.message ?: "Failed to update password"
            } finally {
                _isChangingPassword.value = false
            }
        }
    }

    // ---------- Screen setup ----------

    fun startNgoRegistration() {
        currentMode = NgoFormMode.REGISTER
        _ngoApplication.value = NgoApplicationUiModel()
        touchedFields.clear()
        _fieldErrors.value = NgoFormErrors()
        _submissionState.value = SubmissionState.Idle
    }

    fun startNgoEdit() {
        currentMode = NgoFormMode.EDIT
        _ngoApplication.value = (_activeNgoDetails.value ?: NgoApplicationUiModel())
            .copy(agreedToTerms = false, reasonForChange = "")
        touchedFields.clear()
        _fieldErrors.value = NgoFormErrors()
        _submissionState.value = SubmissionState.Idle
    }

    // ---------- Field setters ----------

    fun updateOrganizationName(value: String) {
        _ngoApplication.update { it.copy(organizationName = value) }
        revalidate()
    }

    fun updateRegistrationType(type: NgoRegistrationType) {
        _ngoApplication.update { it.copy(registrationType = type) }
        revalidate()
    }

    fun updateRegistrationNumber(value: String) {
        _ngoApplication.update { it.copy(registrationNumber = value) }
        revalidate()
    }

    fun updateContactPersonName(value: String) {
        _ngoApplication.update { it.copy(contactPersonName = value) }
        revalidate()
    }

    fun updateContactEmail(value: String) {
        _ngoApplication.update { it.copy(contactEmail = value) }
        revalidate()
    }

    fun updateContactPhone(value: String) {
        _ngoApplication.update { it.copy(contactPhone = value) }
        revalidate()
    }

    fun updateCauseCategory(category: NgoCauseCategory) {
        _ngoApplication.update { it.copy(causeCategory = category) }
        touchedFields.add("causeCategory")
        revalidate()
    }

    fun updateAddress(value: String) {
        _ngoApplication.update { it.copy(address = value) }
        revalidate()
    }

    fun updateReasonForChange(value: String) {
        _ngoApplication.update { it.copy(reasonForChange = value) }
        revalidate()
    }

    fun updateAgreedToTerms(value: Boolean) {
        _ngoApplication.update { it.copy(agreedToTerms = value) }
    }

    fun setCertificate(uri: Uri, fileName: String?) {
        _ngoApplication.update { it.copy(certificateUri = uri, certificateFileName = fileName) }
    }

    fun onFieldBlur(field: String, currentValue: String) {
        if (currentValue.isNotBlank()) {
            touchedFields.add(field)
            revalidate()
        }
    }

    private fun revalidate() {
        val raw = NgoValidation.validate(_ngoApplication.value, currentMode)
        _fieldErrors.value = NgoFormErrors(
            organizationName = raw.organizationName.takeIf { "organizationName" in touchedFields },
            registrationNumber = raw.registrationNumber.takeIf { "registrationNumber" in touchedFields },
            contactPersonName = raw.contactPersonName.takeIf { "contactPersonName" in touchedFields },
            contactEmail = raw.contactEmail.takeIf { "contactEmail" in touchedFields },
            contactPhone = raw.contactPhone.takeIf { "contactPhone" in touchedFields },
            causeCategory = raw.causeCategory.takeIf { "causeCategory" in touchedFields },
            address = raw.address.takeIf { "address" in touchedFields },
            reasonForChange = raw.reasonForChange.takeIf { "reasonForChange" in touchedFields }
        )
    }

    // ---------- Submission ----------

    fun submitNgoApplication(mode: NgoFormMode) {
        currentMode = mode

        if (mode == NgoFormMode.EDIT && isUnchangedFromActive()) {
            _showNoChangesDialog.value = true
            return
        }

        touchedFields.addAll(ALL_VALIDATED_FIELDS)
        revalidate()

        if (_fieldErrors.value.hasErrors) {
            _submissionState.value = SubmissionState.Error("Please fix the highlighted fields")
            return
        }

        if (!_ngoApplication.value.agreedToTerms) {
            pendingSubmitMode = mode
            _showTncDialog.value = true
            return
        }
        proceedWithSubmission(mode)
    }

    private fun isUnchangedFromActive(): Boolean {
        val active = _activeNgoDetails.value ?: return false
        val draft = _ngoApplication.value.trimmed().copy(reasonForChange = "", agreedToTerms = false)
        val activeComparable = active.trimmed().copy(reasonForChange = "", agreedToTerms = false)
        return draft == activeComparable
    }

    fun dismissNoChangesDialog() {
        _showNoChangesDialog.value = false
    }

    fun agreeToTermsAndSubmit() {
        _ngoApplication.update { it.copy(agreedToTerms = true) }
        _showTncDialog.value = false
        proceedWithSubmission(pendingSubmitMode)
    }

    fun dismissTncDialog() {
        _showTncDialog.value = false
    }

    private fun proceedWithSubmission(mode: NgoFormMode) {
        val application = _ngoApplication.value.trimmed()
        val userId = UserSession.currentUserId.value

        viewModelScope.launch {
            _submissionState.value = SubmissionState.Submitting
            try {
                when (mode) {
                    NgoFormMode.REGISTER -> {
                        repository.insertNgoApplication(application.toInsertDto(userId, status = "APPROVED"))
                        repository.updateUserNgoStatus(userId, status = "APPROVED", orgName = application.organizationName)
                    }
                    NgoFormMode.EDIT -> {
                        repository.insertNgoApplication(application.toInsertDto(userId, status = "PENDING"))
                    }
                }
                loadProfileData(userId)
                _submissionState.value = SubmissionState.Success
            } catch (e: Exception) {
                _submissionState.value = SubmissionState.Error("Submission failed: ${e.message}")
            }
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = SubmissionState.Idle
    }

    // ---------- Disable NGO account ----------

    fun disableNgoAccount() {
        val userId = UserSession.currentUserId.value
        viewModelScope.launch {
            try {
                repository.updateUserNgoStatus(userId, status = "NONE", orgName = null)
                loadProfileData(userId)
            } catch (e: Exception) {
                _loadError.value = "Couldn't disable NGO account: ${e.message}"
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                onSignedOut()
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Supabase signOut error: ${e.message}")
            } finally {
                UserSession.clear()
                onSignedOut()
            }
        }
    }
}

sealed interface SubmissionState {
    data object Idle : SubmissionState
    data object Submitting : SubmissionState
    data object Success : SubmissionState
    data class Error(val message: String) : SubmissionState
}
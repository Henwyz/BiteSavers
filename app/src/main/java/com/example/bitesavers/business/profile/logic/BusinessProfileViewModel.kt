package com.example.bitesavers.business.profile.logic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.business.profile.data.*
import com.example.bitesavers.customer.profile.logic.SubmissionState
import com.example.bitesavers.data.mapper.toUiModel
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.data.remote.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BusinessProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()
    private var currentStoreId: String? = null
    private var currentStoreOwnerId: String? = null
    private var currentLatitude: Double = 3.1390
    private var currentLongitude: Double = 101.6869

    private val _profile = MutableStateFlow(BusinessProfileUiModel())
    val profile: StateFlow<BusinessProfileUiModel> = _profile.asStateFlow()

    private val _ownerAccount = MutableStateFlow(BusinessOwnerAccountUiModel())
    val ownerAccount: StateFlow<BusinessOwnerAccountUiModel> = _ownerAccount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedTab = MutableStateFlow(BusinessEditTab.ACCOUNT)
    val selectedTab: StateFlow<BusinessEditTab> = _selectedTab.asStateFlow()

    // Account tab
    private val _accountDraft = MutableStateFlow(BusinessAccountEditUiModel())
    val accountDraft: StateFlow<BusinessAccountEditUiModel> = _accountDraft.asStateFlow()

    private val _accountErrors = MutableStateFlow(BusinessAccountFormErrors())
    val accountErrors: StateFlow<BusinessAccountFormErrors> = _accountErrors.asStateFlow()

    // Business tab
    private val _businessDraft = MutableStateFlow(BusinessDetailsEditUiModel())
    val businessDraft: StateFlow<BusinessDetailsEditUiModel> = _businessDraft.asStateFlow()

    private val _businessErrors = MutableStateFlow(BusinessDetailsFormErrors())
    val businessErrors: StateFlow<BusinessDetailsFormErrors> = _businessErrors.asStateFlow()

    // True if newest row in stores table has status = "PENDING"
    private val _hasPendingBusinessEdit = MutableStateFlow(false)
    val hasPendingBusinessEdit: StateFlow<Boolean> = _hasPendingBusinessEdit.asStateFlow()

    // Dialogs
    private val _showNoChangesDialog = MutableStateFlow(false)
    val showNoChangesDialog: StateFlow<Boolean> = _showNoChangesDialog.asStateFlow()

    private val _showTncDialog = MutableStateFlow(false)
    val showTncDialog: StateFlow<Boolean> = _showTncDialog.asStateFlow()

    private val _showPendingWarningDialog = MutableStateFlow(false)
    val showPendingWarningDialog: StateFlow<Boolean> = _showPendingWarningDialog.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    private val touchedAccountFields = mutableSetOf<String>()
    private val touchedBusinessFields = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            UserSession.currentUserId.collectLatest { userId ->
                loadAllData(userId)
            }
        }
    }

    fun loadAllData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Fetch User details from users table
                if (userId.isNotBlank()) {
                    try {
                        val userDto = repository.getUser(userId)
                        val owner = BusinessOwnerAccountUiModel(name = userDto.name, email = userDto.email)
                        _ownerAccount.value = owner
                        _accountDraft.update { it.copy(name = owner.name, email = owner.email) }
                    } catch (e: Exception) {
                        Log.w("BusinessProfile", "Could not fetch user: ${e.message}")
                    }
                }

                // 2. Fetch all store rows
                val storeRows: List<StoreDto> = if (userId.isNotBlank()) repository.getStoreRowsByOwnerId(userId) else emptyList()

                // Display APPROVED row
                val activeApprovedStore: StoreDto? = storeRows.firstOrNull { it.status?.equals("APPROVED", ignoreCase = true) == true }
                    ?: storeRows.lastOrNull { it.status?.equals("PENDING", ignoreCase = true) != true }
                    ?: storeRows.lastOrNull()
                    ?: repository.getFirstStoreFallback()

                _hasPendingBusinessEdit.value = (storeRows.firstOrNull()?.status?.equals("PENDING", ignoreCase = true) == true)

                if (activeApprovedStore != null) {
                    currentStoreId = activeApprovedStore.id
                    currentStoreOwnerId = activeApprovedStore.ownerId ?: userId
                    currentLatitude = activeApprovedStore.latitude ?: 3.1390
                    currentLongitude = activeApprovedStore.longitude ?: 101.6869

                    val storeUi = activeApprovedStore.toUiModel()
                    _profile.value = storeUi

                    _businessDraft.update {
                        it.copy(
                            businessName = storeUi.businessName,
                            address = storeUi.address,
                            phone = storeUi.phone,
                            operatingHours = storeUi.operatingHours,
                            cleanupHours = storeUi.cleanupHours
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("BusinessProfile", "Error loading data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun initEditScreen() {
        _selectedTab.value = BusinessEditTab.ACCOUNT

        _accountDraft.value = BusinessAccountEditUiModel(
            name = _ownerAccount.value.name,
            email = _ownerAccount.value.email,
            password = ""
        )

        _businessDraft.value = BusinessDetailsEditUiModel(
            businessName = _profile.value.businessName,
            address = _profile.value.address,
            phone = _profile.value.phone,
            operatingHours = _profile.value.operatingHours,
            cleanupHours = _profile.value.cleanupHours,
            reasonForChange = "",
            agreedToTerms = false
        )

        touchedAccountFields.clear()
        touchedBusinessFields.clear()
        _accountErrors.value = BusinessAccountFormErrors()
        _businessErrors.value = BusinessDetailsFormErrors()
        _submissionState.value = SubmissionState.Idle
    }

    fun selectTab(tab: BusinessEditTab) {
        _selectedTab.value = tab
    }

    // ---------- Account Tab ----------
    fun updateAccountName(name: String) {
        _accountDraft.update { it.copy(name = name) }
        revalidateAccount()
    }

    fun updateAccountEmail(email: String) {
        _accountDraft.update { it.copy(email = email) }
        revalidateAccount()
    }

    fun updateAccountPassword(password: String) {
        _accountDraft.update { it.copy(password = password) }
        revalidateAccount()
    }

    fun onAccountBlur(field: String) {
        touchedAccountFields.add(field)
        revalidateAccount()
    }

    private fun revalidateAccount() {
        val raw = BusinessProfileValidation.validateAccount(_accountDraft.value)
        _accountErrors.value = BusinessAccountFormErrors(
            name = raw.name.takeIf { "name" in touchedAccountFields },
            email = raw.email.takeIf { "email" in touchedAccountFields },
            password = raw.password.takeIf { "password" in touchedAccountFields }
        )
    }

    fun saveAccountDetails() {
        touchedAccountFields.addAll(listOf("name", "email", "password"))
        revalidateAccount()

        val errors = BusinessProfileValidation.validateAccount(_accountDraft.value)
        if (errors.hasErrors) {
            _submissionState.value = SubmissionState.Error("Please fix the errors above")
            return
        }

        val draft = _accountDraft.value.trimmed()
        val userId = UserSession.currentUserId.value

        viewModelScope.launch {
            _submissionState.value = SubmissionState.Submitting
            try {
                if (userId.isNotBlank()) {
                    repository.updateUserProfile(userId, name = draft.name, email = draft.email)
                }
                if (draft.password.isNotBlank()) {
                    repository.updateUserPassword(draft.password)
                }

                _ownerAccount.value = BusinessOwnerAccountUiModel(name = draft.name, email = draft.email)
                _submissionState.value = SubmissionState.Success
            } catch (e: Exception) {
                _submissionState.value = SubmissionState.Error("Couldn't save account: ${e.message}")
            }
        }
    }

    // ---------- Business Tab ----------
    fun updateBusinessName(name: String) {
        _businessDraft.update { it.copy(businessName = name) }
        revalidateBusiness()
    }

    fun updateBusinessAddress(address: String) {
        _businessDraft.update { it.copy(address = address) }
        revalidateBusiness()
    }

    fun updateBusinessPhone(phone: String) {
        _businessDraft.update { it.copy(phone = phone) }
        revalidateBusiness()
    }

    fun updateBusinessOperatingHours(hours: String) {
        _businessDraft.update { it.copy(operatingHours = hours) }
        revalidateBusiness()
    }

    fun updateBusinessCleanupHours(hours: String) {
        _businessDraft.update { it.copy(cleanupHours = hours) }
        revalidateBusiness()
    }

    fun updateBusinessReasonForChange(reason: String) {
        _businessDraft.update { it.copy(reasonForChange = reason) }
        revalidateBusiness()
    }

    fun updateAgreedToTerms(agreed: Boolean) {
        _businessDraft.update { it.copy(agreedToTerms = agreed) }
    }

    fun onBusinessBlur(field: String, currentValue: String) {
        if (currentValue.isNotBlank()) {
            touchedBusinessFields.add(field)
            revalidateBusiness()
        }
    }

    private fun revalidateBusiness() {
        val raw = BusinessProfileValidation.validateBusiness(_businessDraft.value)
        _businessErrors.value = BusinessDetailsFormErrors(
            businessName = raw.businessName.takeIf { "businessName" in touchedBusinessFields },
            address = raw.address.takeIf { "address" in touchedBusinessFields },
            phone = raw.phone.takeIf { "phone" in touchedBusinessFields },
            operatingHours = raw.operatingHours.takeIf { "operatingHours" in touchedBusinessFields },
            cleanupHours = raw.cleanupHours.takeIf { "cleanupHours" in touchedBusinessFields },
            reasonForChange = raw.reasonForChange.takeIf { "reasonForChange" in touchedBusinessFields }
        )
    }

    private fun isUnchangedFromActive(): Boolean {
        val active = _profile.value
        val draft = _businessDraft.value.trimmed()
        return draft.businessName == active.businessName.trim() &&
                draft.address == active.address.trim() &&
                draft.phone == active.phone.trim() &&
                draft.operatingHours == active.operatingHours.trim() &&
                draft.cleanupHours == active.cleanupHours.trim()
    }

    fun submitBusinessDetails() {
        if (_hasPendingBusinessEdit.value) {
            _showPendingWarningDialog.value = true
            return
        }

        if (isUnchangedFromActive()) {
            _showNoChangesDialog.value = true
            return
        }

        touchedBusinessFields.addAll(
            listOf("businessName", "address", "phone", "operatingHours", "cleanupHours", "reasonForChange")
        )
        revalidateBusiness()

        val errors = BusinessProfileValidation.validateBusiness(_businessDraft.value)
        if (errors.hasErrors) {
            _submissionState.value = SubmissionState.Error("Please fix the highlighted fields")
            return
        }

        if (!_businessDraft.value.agreedToTerms) {
            _showTncDialog.value = true
            return
        }

        proceedWithBusinessSubmission()
    }

    fun agreeToTermsAndSubmit() {
        _businessDraft.update { it.copy(agreedToTerms = true) }
        _showTncDialog.value = false
        proceedWithBusinessSubmission()
    }

    private fun proceedWithBusinessSubmission() {
        val draft = _businessDraft.value.trimmed()
        val userId = UserSession.currentUserId.value
        val ownerIdToUse = if (userId.isNotBlank()) userId else (currentStoreOwnerId ?: "")

        viewModelScope.launch {
            _submissionState.value = SubmissionState.Submitting
            try {
                repository.insertStoreEditRequest(
                    ownerId = ownerIdToUse,
                    name = draft.businessName,
                    address = draft.address,
                    phone = draft.phone,
                    operatingHours = draft.operatingHours,
                    cleanupHours = draft.cleanupHours,
                    latitude = currentLatitude,
                    longitude = currentLongitude,
                    reasonForChange = draft.reasonForChange // 👈 Passes the draft's reason for change
                )

                _hasPendingBusinessEdit.value = true
                loadAllData(ownerIdToUse)
                _submissionState.value = SubmissionState.Success
            } catch (e: Exception) {
                Log.e("BusinessProfile", "Error submitting business edit", e)
                _submissionState.value = SubmissionState.Error("Submission failed: ${e.message}")
            }
        }
    }

    fun dismissNoChangesDialog() { _showNoChangesDialog.value = false }
    fun dismissTncDialog() { _showTncDialog.value = false }
    fun dismissPendingWarningDialog() { _showPendingWarningDialog.value = false }
    fun resetSubmissionState() { _submissionState.value = SubmissionState.Idle }
}
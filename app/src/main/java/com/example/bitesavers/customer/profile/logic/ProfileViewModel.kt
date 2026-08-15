package com.example.bitesavers.customer.profile.logic

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.profile.data.NgoApplicationUiModel
import com.example.bitesavers.customer.profile.data.NgoCauseCategory
import com.example.bitesavers.customer.profile.data.NgoRegistrationType
import com.example.bitesavers.customer.profile.data.NgoStatus
import com.example.bitesavers.customer.profile.data.UserProfileUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NgoFormMode { REGISTER, EDIT }

private val ALL_VALIDATED_FIELDS = setOf(
    "organizationName", "registrationNumber", "contactPersonName",
    "contactEmail", "contactPhone", "address"
)

/**
 * MVP-phase ViewModel: dummy in-memory state, per the team's "shared
 * ViewModel, real DB later" rule.
 */
class ProfileViewModel : ViewModel() {

    private val _profile = MutableStateFlow(
        UserProfileUiModel(
            id = "u1",
            name = "Michelle Lim",
            email = "michellelim@gmail.com",
            avatarInitials = "ML",
            memberSinceLabel = "Member since 7 June 2026",
            walletBalance = 67.50,
            mealsRescued = 15
        )
    )
    val profile: StateFlow<UserProfileUiModel> = _profile.asStateFlow()

    // The currently APPROVED, displayed NGO details (shown on NgoDetailsScreen).
    // Only REGISTER submissions write here. EDIT submissions deliberately do
    // NOT touch this — see proceedWithSubmission() — to simulate "changes
    // apply only after approval, old details stay active until then."
    private val _activeNgoDetails = MutableStateFlow<NgoApplicationUiModel?>(null)
    val activeNgoDetails: StateFlow<NgoApplicationUiModel?> = _activeNgoDetails.asStateFlow()

    // The draft form currently being filled in — used by NgoRegistrationScreen
    // for BOTH the register flow and the edit flow.
    private val _ngoApplication = MutableStateFlow(NgoApplicationUiModel())
    val ngoApplication: StateFlow<NgoApplicationUiModel> = _ngoApplication.asStateFlow()

    private val _fieldErrors = MutableStateFlow(NgoFormErrors())
    val fieldErrors: StateFlow<NgoFormErrors> = _fieldErrors.asStateFlow()

    private val _showTncDialog = MutableStateFlow(false)
    val showTncDialog: StateFlow<Boolean> = _showTncDialog.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    // Which fields the user has actually typed into at least once. A field's
    // error only ever displays once it's in this set — that's what makes
    // validation feel "live" (as soon as you start typing) without showing
    // every field as red the instant the screen opens.
    private val touchedFields = mutableSetOf<String>()
    private var pendingSubmitMode = NgoFormMode.REGISTER

    // ---------- Screen setup ----------

    fun startNgoRegistration() {
        _ngoApplication.value = NgoApplicationUiModel()
        touchedFields.clear()
        _fieldErrors.value = NgoFormErrors()
        _submissionState.value = SubmissionState.Idle
    }

    fun startNgoEdit() {
        _ngoApplication.value = _activeNgoDetails.value ?: NgoApplicationUiModel()
        touchedFields.clear()
        _fieldErrors.value = NgoFormErrors()
        _submissionState.value = SubmissionState.Idle
    }

    // ---------- Field setters (each marks its field "touched" and re-validates live) ----------

    fun updateOrganizationName(value: String) {
        _ngoApplication.update { it.copy(organizationName = value) }
        touchAndRevalidate("organizationName")
    }

    fun updateRegistrationType(type: NgoRegistrationType) {
        _ngoApplication.update { it.copy(registrationType = type) }
        revalidate() // don't force-touch the number field, but re-check it if it's already touched
    }

    fun updateRegistrationNumber(value: String) {
        _ngoApplication.update { it.copy(registrationNumber = value) }
        touchAndRevalidate("registrationNumber")
    }

    fun updateContactPersonName(value: String) {
        _ngoApplication.update { it.copy(contactPersonName = value) }
        touchAndRevalidate("contactPersonName")
    }

    fun updateContactEmail(value: String) {
        _ngoApplication.update { it.copy(contactEmail = value) }
        touchAndRevalidate("contactEmail")
    }

    fun updateContactPhone(value: String) {
        _ngoApplication.update { it.copy(contactPhone = value) }
        touchAndRevalidate("contactPhone")
    }

    fun updateCauseCategory(category: NgoCauseCategory) {
        _ngoApplication.update { it.copy(causeCategory = category) }
    }

    fun updateAddress(value: String) {
        _ngoApplication.update { it.copy(address = value) }
        touchAndRevalidate("address")
    }

    fun updateAgreedToTerms(value: Boolean) {
        _ngoApplication.update { it.copy(agreedToTerms = value) }
    }

    fun setCertificate(uri: Uri, fileName: String?) {
        _ngoApplication.update { it.copy(certificateUri = uri, certificateFileName = fileName) }
    }

    private fun touchAndRevalidate(field: String) {
        touchedFields.add(field)
        revalidate()
    }

    private fun revalidate() {
        val raw = NgoValidation.validate(_ngoApplication.value)
        _fieldErrors.value = NgoFormErrors(
            organizationName = raw.organizationName.takeIf { "organizationName" in touchedFields },
            registrationNumber = raw.registrationNumber.takeIf { "registrationNumber" in touchedFields },
            contactPersonName = raw.contactPersonName.takeIf { "contactPersonName" in touchedFields },
            contactEmail = raw.contactEmail.takeIf { "contactEmail" in touchedFields },
            contactPhone = raw.contactPhone.takeIf { "contactPhone" in touchedFields },
            address = raw.address.takeIf { "address" in touchedFields }
        )
    }

    // ---------- Submission ----------

    fun submitNgoApplication(mode: NgoFormMode) {
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

    /** Called from the "Agree & Submit" button in the TnC popup. */
    fun agreeToTermsAndSubmit() {
        _ngoApplication.update { it.copy(agreedToTerms = true) }
        _showTncDialog.value = false
        proceedWithSubmission(pendingSubmitMode)
    }

    fun dismissTncDialog() {
        _showTncDialog.value = false
    }

    private fun proceedWithSubmission(mode: NgoFormMode) {
        val application = _ngoApplication.value
        viewModelScope.launch {
            _submissionState.value = SubmissionState.Submitting
            delay(600) // simulated network delay for the demo, per Practical 7

            when (mode) {
                NgoFormMode.REGISTER -> {
                    // MVP: auto-approve, no admin review step for a brand-new registration.
                    _activeNgoDetails.value = application
                    _profile.update {
                        it.copy(ngoStatus = NgoStatus.APPROVED, ngoOrgName = application.organizationName)
                    }
                }
                NgoFormMode.EDIT -> {
                    // Deliberately DO NOT update _activeNgoDetails here — this
                    // simulates "your change is pending approval, old details
                    // stay active in the meantime." There's no real review
                    // queue in this MVP, so the edit is effectively discarded
                    // after showing the pending-approval message.
                }
            }

            _submissionState.value = SubmissionState.Success
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = SubmissionState.Idle
    }
}

sealed interface SubmissionState {
    data object Idle : SubmissionState
    data object Submitting : SubmissionState
    data object Success : SubmissionState
    data class Error(val message: String) : SubmissionState
}

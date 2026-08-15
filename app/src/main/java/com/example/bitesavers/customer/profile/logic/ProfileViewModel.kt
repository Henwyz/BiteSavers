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
    "contactEmail", "contactPhone", "causeCategory", "address", "reasonForChange"
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

    // Currently APPROVED, displayed NGO details. Only a REGISTER submission
    // writes here. EDIT submissions deliberately never touch this — old
    // details stay active until a real approval flow (not built in this
    // MVP) would apply the change. Cleared entirely by disableNgoAccount().
    private val _activeNgoDetails = MutableStateFlow<NgoApplicationUiModel?>(null)
    val activeNgoDetails: StateFlow<NgoApplicationUiModel?> = _activeNgoDetails.asStateFlow()

    // True once an edit has been submitted and is (simulated) awaiting
    // approval. MVP: nothing ever flips it back to false on its own — a
    // real backend would clear it on approval/rejection. Also reset by
    // disableNgoAccount().
    private val _hasPendingEdit = MutableStateFlow(false)
    val hasPendingEdit: StateFlow<Boolean> = _hasPendingEdit.asStateFlow()

    // The draft form currently being filled in — used by NgoRegistrationScreen
    // for both the register flow and the edit flow.
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

    // A field's error only ever displays once it's in this set. It's added
    // either on blur (if the field has content — see onFieldBlur), on
    // dropdown selection, or when the whole form is force-touched on submit.
    private val touchedFields = mutableSetOf<String>()
    private var currentMode = NgoFormMode.REGISTER
    private var pendingSubmitMode = NgoFormMode.REGISTER

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
        // Prefill from the active (approved) details, but deliberately reset
        // agreedToTerms to false and reasonForChange to blank — otherwise
        // the checkbox would show pre-ticked just because the original
        // registration had it ticked, and any old reason text would carry over.
        _ngoApplication.value = (_activeNgoDetails.value ?: NgoApplicationUiModel())
            .copy(agreedToTerms = false, reasonForChange = "")
        touchedFields.clear()
        _fieldErrors.value = NgoFormErrors()
        _submissionState.value = SubmissionState.Idle
    }

    // ---------- Field setters — update value + live-revalidate ONLY if already touched ----------

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

    /**
     * Call when a field loses focus. Only marks it "touched" (and therefore
     * eligible to show an error) if it actually has content — this avoids
     * flashing "invalid" while the user is still mid-typing on their very
     * first pass through the field, e.g. an email with no "@" yet.
     */
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

        // For an edit, check "did anything actually change" BEFORE running
        // full field validation — otherwise pressing submit on an untouched
        // edit form shows "reason is required" instead of the more useful
        // "you haven't changed anything" message.
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
        // agreedToTerms is deliberately excluded: startNgoEdit() always resets
        // the draft's agreedToTerms to false (see comment there), while the
        // saved active details still has it as true from the original
        // registration. Comparing it here would make every untouched edit
        // form look "changed" just because of that reset, which broke the
        // no-changes check entirely.
        val draft = _ngoApplication.value.trimmed().copy(reasonForChange = "", agreedToTerms = false)
        val activeComparable = active.trimmed().copy(reasonForChange = "", agreedToTerms = false)
        return draft == activeComparable
    }

    fun dismissNoChangesDialog() {
        _showNoChangesDialog.value = false
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
        val application = _ngoApplication.value.trimmed()
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
                    // Deliberately do NOT update _activeNgoDetails — old
                    // details remain active/displayed until a real approval
                    // flow (not built in this MVP) would apply the change.
                    _hasPendingEdit.value = true
                }
            }

            _submissionState.value = SubmissionState.Success
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = SubmissionState.Idle
    }

    // ---------- Disable NGO account ----------

    fun disableNgoAccount() {
        _activeNgoDetails.value = null
        _hasPendingEdit.value = false
        _profile.update { it.copy(ngoStatus = NgoStatus.NONE, ngoOrgName = null) }
    }
}

sealed interface SubmissionState {
    data object Idle : SubmissionState
    data object Submitting : SubmissionState
    data object Success : SubmissionState
    data class Error(val message: String) : SubmissionState
}

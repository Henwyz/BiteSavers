package com.example.bitesavers.data.mapper

import com.example.bitesavers.business.profile.data.BusinessProfileUiModel
import com.example.bitesavers.customer.profile.data.NgoApplicationUiModel
import com.example.bitesavers.customer.profile.data.NgoCauseCategory
import com.example.bitesavers.customer.profile.data.NgoRegistrationType
import com.example.bitesavers.customer.profile.data.NgoStatus
import com.example.bitesavers.customer.profile.data.UserProfileUiModel
import com.example.bitesavers.data.remote.dto.NgoApplicationDto
import com.example.bitesavers.data.remote.dto.NgoApplicationInsertDto
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.data.remote.dto.UserDto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ---------- users table <-> UserProfileUiModel ----------

fun UserDto.toUiModel(): UserProfileUiModel = UserProfileUiModel(
    id = id,
    name = name,
    email = email,
    avatarInitials = computeInitials(name),
    memberSinceLabel = formatMemberSince(createdAt),
    walletBalance = walletBalance,
    ngoStatus = ngoStatus.toEnumOrNull<NgoStatus>() ?: NgoStatus.NONE,
    ngoOrgName = ngoOrgName,
    mealsRescued = 0,
    moneySaved = 0.0,
    co2ReducedKg = 0.0
)

// ---------- ngo_applications table <-> NgoApplicationUiModel ----------

fun NgoApplicationDto.toUiModel(): NgoApplicationUiModel = NgoApplicationUiModel(
    organizationName = organizationName,
    registrationType = registrationType.toEnumOrNull<NgoRegistrationType>() ?: NgoRegistrationType.SSM,
    registrationNumber = registrationNumber,
    contactPersonName = contactPersonName,
    contactEmail = contactEmail,
    contactPhone = contactPhone,
    causeCategory = causeCategory?.toEnumOrNull<NgoCauseCategory>(),
    address = address,
    agreedToTerms = true, // implied — a saved row could only exist if ToS was agreed to at submission time
    certificateUri = null, // not persisted — see NgoApplicationInsertDto's comment
    certificateFileName = certificateFileName,
    reasonForChange = reasonForChange.orEmpty()
)

// Generates clean, human-readable primary key matching Supabase seed format (e.g. ngo_app_172530)
private fun generateNgoAppId(): String = "ngo_app_${System.currentTimeMillis().toString().takeLast(6)}"

/** Builds the row to insert when submitting the registration/edit form. */
fun NgoApplicationUiModel.toInsertDto(userId: String, status: String): NgoApplicationInsertDto =
    NgoApplicationInsertDto(
        id = generateNgoAppId(),
        userId = userId,
        organizationName = organizationName,
        registrationType = registrationType.name,
        registrationNumber = registrationNumber,
        contactPersonName = contactPersonName,
        contactEmail = contactEmail,
        contactPhone = contactPhone,
        causeCategory = causeCategory?.name,
        address = address,
        certificateFileName = certificateFileName,
        reasonForChange = reasonForChange.ifBlank { null },
        status = status
    )

// ---------- small helpers ----------

private fun formatTimeRange(opening: String?, closing: String?): String {
    if (opening.isNullOrBlank() && closing.isNullOrBlank()) return ""
    val start = opening?.take(5) ?: "--:--"
    val end = closing?.take(5) ?: "--:--"
    return "$start - $end"
}

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    runCatching { enumValueOf<T>(this) }.getOrNull()

private fun computeInitials(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

private fun formatMemberSince(createdAt: String?): String {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (pattern in patterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val cleaned = createdAt?.substringBefore(".")?.substringBefore("+")?.trim()
            val date = parser.parse(cleaned) ?: continue
            val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale.US)
            return "Member since ${displayFormat.format(date)}"
        } catch (e: Exception) {
            // try the next pattern
        }
    }
    return "Member since $createdAt"
}
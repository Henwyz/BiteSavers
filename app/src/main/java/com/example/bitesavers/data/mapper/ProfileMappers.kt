package com.example.bitesavers.data.mapper

import com.example.bitesavers.customer.profile.data.NgoApplicationUiModel
import com.example.bitesavers.customer.profile.data.NgoCauseCategory
import com.example.bitesavers.customer.profile.data.NgoRegistrationType
import com.example.bitesavers.customer.profile.data.NgoStatus
import com.example.bitesavers.customer.profile.data.UserProfileUiModel
import com.example.bitesavers.data.remote.dto.NgoApplicationDto
import com.example.bitesavers.data.remote.dto.NgoApplicationInsertDto
import com.example.bitesavers.data.remote.dto.UserDto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

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
    mealsRescued = mealsRescued
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

/** Builds the row to insert when submitting the registration/edit form. */
fun NgoApplicationUiModel.toInsertDto(userId: String, status: String): NgoApplicationInsertDto =
    NgoApplicationInsertDto(
        id = UUID.randomUUID().toString(),
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

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    runCatching { enumValueOf<T>(this) }.getOrNull()

private fun computeInitials(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

/**
 * ASSUMPTION: Postgrest/PostgREST typically returns timestamps in ISO-8601
 * with a "T" separator (e.g. "2026-08-25T12:27:52.531121+00:00"), which is
 * what this parses. Table Editor's own display ("2026-08-25 12:27:52...")
 * is Postgres's internal text form, not necessarily what the JSON API
 * actually sends — if this always falls through to the fallback string
 * below, log the raw `createdAt` value once to see its real format and
 * adjust the patterns tried here.
 */
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
            // Strip fractional seconds and timezone suffix before parsing —
            // both vary in length/format and aren't needed for a display label.
            val cleaned = createdAt?.substringBefore(".")?.substringBefore("+")?.trim()
            val date = parser.parse(cleaned) ?: continue
            val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale.US)
            return "Member since ${displayFormat.format(date)}"
        } catch (e: Exception) {
            // try the next pattern
        }
    }
    return "Member since $createdAt" // last-resort fallback, at least shows something real
}

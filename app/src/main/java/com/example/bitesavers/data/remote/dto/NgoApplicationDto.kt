package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Matches the `ngo_applications` table — used for reads. Each row is ONE
 * submission (initial registration OR a later edit), not a single row that
 * gets overwritten — the `status` column tracks each row's own state.
 *
 * ASSUMPTION (unverified — I only saw the `users` table's sample data, not
 * this table's): `status` values are assumed to be "PENDING" / "APPROVED",
 * and `registration_type` / `cause_category` are assumed to be stored as
 * the enum's exact name (e.g. "SSM", "FOOD_BANK"). Check a real row in
 * Table Editor to confirm — if the casing/wording is different, the
 * mapping functions in ProfileMappers.kt need matching adjustments.
 */
@Serializable
data class NgoApplicationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("organization_name") val organizationName: String,
    @SerialName("registration_type") val registrationType: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("contact_person_name") val contactPersonName: String,
    @SerialName("contact_email") val contactEmail: String,
    @SerialName("contact_phone") val contactPhone: String,
    @SerialName("cause_category") val causeCategory: String? = null,
    val address: String,
    @SerialName("certificate_file_name") val certificateFileName: String? = null,
    @SerialName("reason_for_change") val reasonForChange: String? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

/**
 * Insert-only payload — omits created_at/updated_at, assuming the table has
 * `DEFAULT now()` on both (standard Supabase practice). If insert fails
 * with a "null value in column created_at" error, that assumption is
 * wrong — add explicit ISO-8601 timestamp strings for both fields here.
 *
 * `id` is generated client-side (java.util.UUID) since the column is
 * `text NOT NULL` with no visible auto-generation — if the table actually
 * DOES auto-generate IDs, this just overrides it with our own, which is
 * harmless but redundant; ask your teammate if unsure.
 */
@Serializable
data class NgoApplicationInsertDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("organization_name") val organizationName: String,
    @SerialName("registration_type") val registrationType: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("contact_person_name") val contactPersonName: String,
    @SerialName("contact_email") val contactEmail: String,
    @SerialName("contact_phone") val contactPhone: String,
    @SerialName("cause_category") val causeCategory: String? = null,
    val address: String,
    @SerialName("certificate_file_name") val certificateFileName: String? = null,
    @SerialName("reason_for_change") val reasonForChange: String? = null,
    val status: String
)

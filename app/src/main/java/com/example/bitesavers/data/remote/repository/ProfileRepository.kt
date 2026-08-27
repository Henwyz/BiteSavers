package com.example.bitesavers.data.remote.repository

import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.NgoApplicationDto
import com.example.bitesavers.data.remote.dto.NgoApplicationInsertDto
import com.example.bitesavers.data.remote.dto.UserDto
import com.example.bitesavers.data.remote.dto.UserNgoStatusUpdateDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

/**
 * NOTE: the exact Postgrest DSL syntax below (`.select { filter { ... } }`,
 * `.update(dto) { filter { ... } }`) matches commonly-documented supabase-kt
 * usage, but the DSL has shifted slightly across versions. If any of these
 * don't compile against your teammate's installed version, check
 * https://github.com/supabase-community/supabase-kt or find another spot
 * in the project that already calls Postgrest successfully and match its
 * exact syntax instead.
 */
class ProfileRepository {

    private val postgrest = SupabaseClient.client.postgrest

    suspend fun getUser(userId: String): UserDto =
        postgrest.from("users")
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingle<UserDto>()

    /** All applications for this user, most recent first. */
    suspend fun getNgoApplications(userId: String): List<NgoApplicationDto> =
        postgrest.from("ngo_applications")
            .select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<NgoApplicationDto>()

    suspend fun insertNgoApplication(dto: NgoApplicationInsertDto) {
        postgrest.from("ngo_applications").insert(dto)
    }

    suspend fun updateUserNgoStatus(userId: String, status: String, orgName: String?) {
        postgrest.from("users")
            .update(UserNgoStatusUpdateDto(ngoStatus = status, ngoOrgName = orgName)) {
                filter { eq("id", userId) }
            }
    }
}

package com.example.bitesavers.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = "https://gspiwguhxnzdmzzqyqzx.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdzcGl3Z3VoeG56ZG16enF5cXp4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY2OTAyMjAsImV4cCI6MjEwMjI2NjIyMH0.LqiQIoalgS3WDfkdLoS4c9T0CkhcW1Mj-nhf4o5o0aA"
        ) {
            install(Postgrest) // Table queries
            install(Auth)      // Secure authentication engine
            install(Storage)   //File/Photo Storage Plugin
        }
    }
}
package com.example.bitesavers.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    // 👇 ADD 'by lazy' HERE 👇
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = "https://gspiwguhxnzdmzzqyqzx.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdzcGl3Z3VoeG56ZG16enF5cXp4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY2OTAyMjAsImV4cCI6MjEwMjI2NjIyMH0.LqiQIoalgS3WDfkdLoS4c9T0CkhcW1Mj-nhf4o5o0aA"
        ) {
            install(Postgrest) // Postgrest is the engine that handles table queries
        }
    }
}
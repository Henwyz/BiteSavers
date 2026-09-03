package com.example.bitesavers.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object GmailSender {

    // ⚠️ REPLACE THESE WITH YOUR OWN GMAIL CREDENTIALS FOR TESTING
    private const val SENDER_GMAIL = "jyanljx-pm25@student.tarc.edu.my" // 👈 Your Gmail address
    private const val GMAIL_APP_PASSWORD = "hiaf uoen rorw rjbu" // 👈 Your 16-character App Password

    suspend fun sendNgoClaimConfirmation(
        recipientEmail: String,
        ngoOrgName: String,
        orderId: String,
        itemName: String,
        quantity: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.auth", "true")
                put("mail.smtp.port", "465")
                put("mail.smtp.ssl.enable", "true")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_GMAIL, GMAIL_APP_PASSWORD.replace(" ", ""))
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_GMAIL, "BiteSavers Food Rescue"))
                setRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                subject = "🟢 [BiteSavers] Free Rescue Meal Claimed - $orderId"

                val emailContent = """
                    <h2>BiteSavers NGO Food Rescue Confirmation</h2>
                    <p>Dear <b>$ngoOrgName</b>,</p>
                    <p>This is an automated confirmation that a free surplus food claim has been <b>verified and collected</b> at the partner store:</p>
                    
                    <table style="border-collapse: collapse; width: 100%; max-width: 450px;">
                        <tr style="background-color: #f2f2f2;">
                            <td style="padding: 8px; border: 1px solid #ddd;"><b>Order ID</b></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">$orderId</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><b>Food Item</b></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">$itemName x$quantity</td>
                        </tr>
                        <tr style="background-color: #f2f2f2;">
                            <td style="padding: 8px; border: 1px solid #ddd;"><b>Claim Type</b></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">100% Free NGO Surplus Rescue</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><b>Status</b></td>
                            <td style="padding: 8px; border: 1px solid #ddd; color: #2e7d32;"><b>COLLECTED & VERIFIED</b></td>
                        </tr>
                    </table>
                    
                    <p style="margin-top: 16px; color: #555; font-size: 13px;">
                        <i>To prevent personal misuse of registered NGO accounts, an automated notice is sent whenever your organization's credentials are used to claim meals. If this pickup was unauthorized, please report it immediately in the BiteSavers app.</i>
                    </p>
                    
                    <p>Thank you for supporting zero food waste!</p>
                    <p><b>— BiteSavers Team</b></p>
                """.trimIndent()

                setContent(emailContent, "text/html; charset=utf-8")
            }

            Transport.send(message)
            Log.d("GmailSender", "✅ Automated NGO claim email successfully sent to $recipientEmail")
            true
        } catch (e: Exception) {
            Log.e("GmailSender", "❌ Failed to send automated email: ${e.message}", e)
            false
        }
    }
}
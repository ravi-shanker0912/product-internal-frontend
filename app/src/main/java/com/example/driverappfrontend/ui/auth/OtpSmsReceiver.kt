package com.example.driverappfrontend.ui.auth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * Listens for the SMS User Consent broadcast and hands back the system
 * consent-dialog Intent for the caller to launch. We use the User Consent
 * API rather than the SMS Retriever API because it works with any SMS body
 * — the Retriever API requires the message to end with an app-signature
 * hash, which the backend doesn't produce (see AuthService, which currently
 * only logs the OTP and doesn't send SMS at all).
 */
class OtpSmsReceiver(
    private val onConsentIntentAvailable: (Intent) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
        val extras = intent.extras ?: return
        val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status ?: return
        if (status.statusCode == CommonStatusCodes.SUCCESS) {
            @Suppress("DEPRECATION")
            val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
            consentIntent?.let(onConsentIntentAvailable)
        }
        // CommonStatusCodes.TIMEOUT or anything else: say nothing and let
        // the user fall back to typing the code manually.
    }
}

/** Finds an OTP_LENGTH-digit run in a raw SMS body, e.g. "Your code is 482913." */
fun extractOtpFromMessage(message: String): String? =
    Regex("\\b\\d{$OTP_LENGTH}\\b").find(message)?.value

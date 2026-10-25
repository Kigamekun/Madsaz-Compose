package id.madsaz.app.ui

import android.util.Log
import androidx.annotation.StringRes
import id.madsaz.app.R
import id.madsaz.app.data.billing.BillingException
import id.madsaz.app.data.remote.ApiException
import java.io.IOException

private const val TAG = "Madsaz"

/** Teks `error` dari BE berbahasa Inggris, jadi pengguna hanya melihat teks lokal yang dipetakan dari status/kode. */
@StringRes
fun Throwable.userMessageRes(): Int = when (this) {
    is ApiException -> when (code) {
        "invalid_credentials" -> R.string.error_invalid_credentials
        "email_taken" -> R.string.error_email_taken
        "invalid_input" -> R.string.error_invalid_input
        "free_limit_reached" -> R.string.error_free_limit
        "guest_limit_reached" -> R.string.error_guest_limit
        "terms_required" -> R.string.error_terms_required
        "email_unavailable" -> R.string.error_email_unavailable
        "analysis_in_progress" -> R.string.error_analysis_in_progress
        "upload_invalid" -> R.string.error_upload_invalid
        "storage_unavailable", "storage_rejected" -> R.string.error_storage
        "billing_unavailable", "play_verification_failed" -> R.string.error_billing_server
        "purchase_other_account", "purchase_already_linked" -> R.string.error_purchase_other_account
        "invalid_purchase_token", "purchase_mismatch" -> R.string.error_billing_failed
        "session_ended", "invalid_token", "authentication_required", "invalid_refresh_token" -> R.string.error_session_expired
        else -> if (status >= 500) R.string.error_server else R.string.error_unknown
    }
    is IOException -> R.string.error_network
    is BillingException -> R.string.error_billing_unavailable
    else -> {
        // Kontrak BE dilanggar atau bug: jangan disembunyikan, tapi pengguna cukup melihat pesan umum.
        Log.e(TAG, "Unexpected error", this)
        R.string.error_unknown
    }
}

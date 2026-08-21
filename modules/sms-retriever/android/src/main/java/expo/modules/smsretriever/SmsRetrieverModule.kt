package expo.modules.smsretriever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

private const val SMS_RECEIVED_EVENT = "onSmsReceived"
private const val SMS_TIMEOUT_EVENT = "onSmsTimeout"
private const val HASH_ALGORITHM = "SHA-256"
private const val NUM_HASHED_BYTES = 9
private const val NUM_BASE64_CHAR = 11

/**
 * Wraps the (silent, no-consent-dialog) Android SMS Retriever API: as long
 * as an incoming SMS ends with this app's 11-character signature hash (see
 * getAppSignatureHash below), Play Services delivers the full message body
 * to this broadcast receiver without any SMS/READ_SMS permission.
 */
class SmsRetrieverModule : Module() {
  private var receiver: BroadcastReceiver? = null

  override fun definition() = ModuleDefinition {
    Name("SmsRetriever")

    Events(SMS_RECEIVED_EVENT, SMS_TIMEOUT_EVENT)

    Function("getAppSignatureHash") {
      appSignatureHash(context)
    }

    AsyncFunction("startListening") { promise: expo.modules.kotlin.Promise ->
      val client = SmsRetriever.getClient(context)
      client.startSmsRetriever()
        .addOnSuccessListener { registerReceiver() }
        .addOnFailureListener { e -> promise.reject("ERR_SMS_RETRIEVER_START", e.message, e) }
      promise.resolve(null)
    }

    Function("stopListening") {
      unregisterReceiver()
    }

    OnDestroy {
      unregisterReceiver()
    }
  }

  private val context: Context
    get() = appContext.reactContext ?: throw IllegalStateException("React context is not available")

  private fun registerReceiver() {
    unregisterReceiver()
    val newReceiver = object : BroadcastReceiver() {
      override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
        val extras = intent.extras ?: return
        when ((extras.get(SmsRetriever.EXTRA_STATUS) as? Status)?.statusCode) {
          CommonStatusCodes.SUCCESS -> {
            val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
            if (message != null) sendEvent(SMS_RECEIVED_EVENT, mapOf("message" to message))
          }
          CommonStatusCodes.TIMEOUT -> sendEvent(SMS_TIMEOUT_EVENT, emptyMap<String, Any>())
        }
      }
    }
    receiver = newReceiver
    val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.registerReceiver(newReceiver, filter, Context.RECEIVER_EXPORTED)
    } else {
      @Suppress("UnspecifiedRegisterReceiverFlag")
      context.registerReceiver(newReceiver, filter)
    }
  }

  private fun unregisterReceiver() {
    receiver?.let {
      runCatching { context.unregisterReceiver(it) }
      receiver = null
    }
  }
}

/**
 * Computes the 11-char app signature hash the SMS Retriever API requires
 * incoming messages to end with. Public, documented algorithm (Google's own
 * SMS Retriever sample uses this exact computation) -- not a Play Services
 * call, so it works without any extra runtime dependency.
 */
private fun appSignatureHash(context: Context): String? {
  return try {
    val packageName = context.packageName
    val packageManager = context.packageManager
    @Suppress("DEPRECATION")
    val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
      info.signingInfo?.apkContentsSigners
    } else {
      val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
      info.signatures
    }
    val signature = signatures?.firstOrNull() ?: return null
    val appInfo = "$packageName ${signature.toCharsString()}"
    val digest = MessageDigest.getInstance(HASH_ALGORITHM)
    digest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
    val hashed = digest.digest().copyOfRange(0, NUM_HASHED_BYTES)
    Base64.encodeToString(hashed, Base64.NO_PADDING or Base64.NO_WRAP).substring(0, NUM_BASE64_CHAR)
  } catch (e: Exception) {
    null
  }
}

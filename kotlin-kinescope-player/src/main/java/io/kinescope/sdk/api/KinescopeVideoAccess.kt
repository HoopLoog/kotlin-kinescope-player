package io.kinescope.sdk.api

import android.os.Handler
import android.os.Looper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * Viewer authorization for videos restricted by "who can watch".
 *
 * Only the password mode can be passed without the viewer: email, SSO and unique codes need
 * a code from a mailbox or a browser login.
 */
object KinescopeVideoAccess {

    /** Cookie carrying the viewer token; see [io.kinescope.sdk.player.KinescopePlayerOptions.accessToken]. */
    const val ACCESS_COOKIE = "kinescope-user"

    // No logging interceptor on purpose: the request body is the password.
    private val client by lazy { OkHttpClient() }
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    /**
     * Exchanges the video password for a viewer token (a JWT valid for a few hours, bound to
     * this video). [callback] runs on the main thread with the token, or with
     * [KinescopeVideoAccessException.WrongPassword] / an [IOException].
     */
    fun authorizeWithPassword(
        videoId: String,
        password: String,
        callback: (Result<String>) -> Unit,
    ) {
        val url = KinescopeApiConfig.FETCH_BASE_URL.toHttpUrl().newBuilder()
            .addPathSegment(videoId)
            .addPathSegment("check-password")
            .build()
        val body = JSONObject()
            .put("password", password)
            .put("query_params", JSONObject())
            .toString()
            .toRequestBody("application/json".toMediaType())
        client.newCall(Request.Builder().url(url).post(body).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = deliver(callback, Result.failure(e))

            override fun onResponse(call: Call, response: Response) {
                val result = response.use {
                    when {
                        it.code == 403 -> Result.failure(KinescopeVideoAccessException.WrongPassword())
                        !it.isSuccessful -> Result.failure(IOException("check-password HTTP ${it.code}"))
                        else -> accessTokenFrom(it.headers("Set-Cookie"))
                            ?.let { token -> Result.success(token) }
                            ?: Result.failure(IOException("check-password returned no $ACCESS_COOKIE cookie"))
                    }
                }
                deliver(callback, result)
            }
        })
    }

    internal fun accessTokenFrom(setCookies: List<String>): String? =
        setCookies.firstNotNullOfOrNull { header ->
            header.substringBefore(';').trim()
                .takeIf { it.startsWith("$ACCESS_COOKIE=") }
                ?.removePrefix("$ACCESS_COOKIE=")
                ?.takeIf { it.isNotEmpty() }
        }

    private fun deliver(callback: (Result<String>) -> Unit, result: Result<String>) {
        mainHandler.post { callback(result) }
    }
}

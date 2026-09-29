package io.kinescope.sdk.api

import java.io.IOException

/**
 * Why `{videoId}.json` refused the playback config. Delivered to `loadVideo(onFailed = …)`
 * and to [KinescopeVideoAccess] callbacks.
 *
 * Extends [IOException] so it can travel through OkHttp interceptors and Retrofit `onFailure`.
 */
sealed class KinescopeVideoAccessException(message: String) : IOException(message) {

    /**
     * The video is restricted to authorized viewers ("who can watch"): instead of JSON the
     * server answered with its access page. For [KinescopeVideoAuthType.PASSWORD] get a token
     * with [KinescopeVideoAccess.authorizeWithPassword] and pass it via
     * [io.kinescope.sdk.player.KinescopePlayerOptions.accessToken].
     */
    class AuthRequired(val authType: KinescopeVideoAuthType) :
        KinescopeVideoAccessException("Viewer authorization required: $authType")

    /** The player is not allowed on the sent `Referer` ("where the player can be shown"). */
    class Forbidden(val httpCode: Int) :
        KinescopeVideoAccessException("Playback config forbidden (HTTP $httpCode)")

    /** [KinescopeVideoAccess.authorizeWithPassword] was rejected. */
    class WrongPassword : KinescopeVideoAccessException("Wrong video password")
}

/** `authType` of the access page served instead of the playback config. */
enum class KinescopeVideoAuthType {
    PASSWORD,
    EMAIL,
    SSO,
    UNIQUE_CODE,
    UNKNOWN,
    ;

    internal companion object {
        private val AUTH_TYPE = Regex("""authType\s*:\s*["']([a-z_]+)["']""")

        fun fromAccessPage(html: String): KinescopeVideoAuthType =
            when (AUTH_TYPE.find(html)?.groupValues?.get(1)) {
                "password" -> PASSWORD
                "email" -> EMAIL
                "sso" -> SSO
                "unique_code" -> UNIQUE_CODE
                else -> UNKNOWN
            }
    }
}

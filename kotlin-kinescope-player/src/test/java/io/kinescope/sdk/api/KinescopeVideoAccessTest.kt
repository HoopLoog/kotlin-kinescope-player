package io.kinescope.sdk.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KinescopeVideoAccessTest {

    @Test
    fun fromAccessPage_readsAuthTypeOfTheServedPage() {
        assertEquals(KinescopeVideoAuthType.PASSWORD, KinescopeVideoAuthType.fromAccessPage(page("password")))
        assertEquals(KinescopeVideoAuthType.EMAIL, KinescopeVideoAuthType.fromAccessPage(page("email")))
        assertEquals(KinescopeVideoAuthType.SSO, KinescopeVideoAuthType.fromAccessPage(page("sso")))
        assertEquals(KinescopeVideoAuthType.UNIQUE_CODE, KinescopeVideoAuthType.fromAccessPage(page("unique_code")))
    }

    @Test
    fun fromAccessPage_unknownOrMissing_isUnknown() {
        assertEquals(KinescopeVideoAuthType.UNKNOWN, KinescopeVideoAuthType.fromAccessPage(page("magic_link")))
        assertEquals(KinescopeVideoAuthType.UNKNOWN, KinescopeVideoAuthType.fromAccessPage("<html></html>"))
    }

    @Test
    fun accessTokenFrom_picksViewerCookieValue() {
        val headers = listOf(
            "other=1; Path=/",
            "kinescope-user=eyJ.payload.sig; Path=/; HttpOnly; Secure; SameSite=None",
        )
        assertEquals("eyJ.payload.sig", KinescopeVideoAccess.accessTokenFrom(headers))
    }

    @Test
    fun accessTokenFrom_absentOrEmpty_isNull() {
        assertNull(KinescopeVideoAccess.accessTokenFrom(emptyList()))
        assertNull(KinescopeVideoAccess.accessTokenFrom(listOf("kinescope-user=; Max-Age=0")))
        assertNull(KinescopeVideoAccess.accessTokenFrom(listOf("kinescope-user-x=abc")))
    }

    private fun page(authType: String) = """
        <script>
          playApi.create({
            isLimited: false,
            authType: "$authType",
          })
        </script>
    """.trimIndent()
}

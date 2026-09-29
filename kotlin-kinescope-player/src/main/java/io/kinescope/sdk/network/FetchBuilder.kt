package io.kinescope.sdk.network

import com.squareup.moshi.Moshi
import io.kinescope.sdk.api.KinescopeFetch
import io.kinescope.sdk.api.KinescopeApiConfig
import io.kinescope.sdk.api.KinescopeVideoAccessException
import io.kinescope.sdk.api.KinescopeVideoAuthType
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object FetchBuilder {

    private const val ACCESS_PAGE_PEEK_BYTES = 64L * 1024

    private fun getOkhttpClient(referer: String) =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                    .apply {
                        // The viewer access token travels as a cookie.
                        redactHeader("Cookie")
                        redactHeader("Set-Cookie")
                    }
            )
            .addInterceptor(Interceptor { chain ->
                var request: Request = chain.request()
                request = request.newBuilder().header("Referer", referer).build()
                chain.proceed(request)
            })
            .addInterceptor(Interceptor { chain -> rejectAccessPage(chain.proceed(chain.request())) })
            .build()

    /**
     * A restricted video answers `{videoId}.json` with 200 and an HTML access page instead of
     * JSON; without this Moshi fails with an opaque parse error.
     */
    private fun rejectAccessPage(response: okhttp3.Response): okhttp3.Response {
        val contentType = response.body?.contentType()
        if (!response.isSuccessful || contentType?.subtype != "html") return response
        val page = response.peekBody(ACCESS_PAGE_PEEK_BYTES).string()
        response.close()
        throw KinescopeVideoAccessException.AuthRequired(KinescopeVideoAuthType.fromAccessPage(page))
    }

    private fun getMoshi(): Moshi {
        return Moshi.Builder()
            .build()
    }

    private fun getBuilder(referer: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(KinescopeApiConfig.FETCH_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
            .client(getOkhttpClient(referer))
            .build()
    }

    fun getKinescopeFetch(referer: String): KinescopeFetch =
        getBuilder(referer).create(KinescopeFetch::class.java)
}

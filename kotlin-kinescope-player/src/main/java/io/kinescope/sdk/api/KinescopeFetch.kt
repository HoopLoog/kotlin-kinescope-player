package io.kinescope.sdk.api

import io.kinescope.sdk.models.videos.KinescopeVideo
import io.kinescope.sdk.utils.SDK_TYPE
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface KinescopeFetch {
    @GET(KinescopeApiConfig.VIDEO_JSON)
    fun getVideo(
        @Path(KinescopeApiConfig.VIDEO_ID_PARAM) videoId: String,
        @Query("sdk") sdk: String = SDK_TYPE,
        @Query("drmauthtoken") drmAuthToken: String? = null,
    ): Call<KinescopeVideo>

    /** [getVideo] with the viewer access cookie; a separate method keeps the 0.1.17 signature intact. */
    @GET(KinescopeApiConfig.VIDEO_JSON)
    fun getVideoWithCookie(
        @Path(KinescopeApiConfig.VIDEO_ID_PARAM) videoId: String,
        @Header("Cookie") cookie: String?,
        @Query("sdk") sdk: String = SDK_TYPE,
        @Query("drmauthtoken") drmAuthToken: String? = null,
    ): Call<KinescopeVideo>
}

package io.kinescope.sdk.models.videos

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class KinescopeVideoApi (
    @Json(name = "id") val id:String,
    @Json(name = "title")  val title: String,
): Serializable
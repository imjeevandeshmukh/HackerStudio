package com.bytelogs.hackerstudio.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SongModel (
	@SerializedName("song") val song: String,
	@SerializedName("url") val url: String,
	@SerializedName("artists") val artists: String,
	@SerializedName("cover_image") val cover_image: String
):Serializable
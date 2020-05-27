package com.gitlab.marcodsl.server.dto

import com.google.gson.annotations.SerializedName

data class AssetResponse(
    @SerializedName("resource_name") val resourceName: String,
    @SerializedName("asset_content") val assetContent: String,
    @SerializedName("asset_filename") val assetFileName: String,
    @SerializedName("asset_path") val assetPath: String
)
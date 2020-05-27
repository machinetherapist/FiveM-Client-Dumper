package com.gitlab.marcodsl.server.dto

import com.google.gson.annotations.SerializedName

data class ManifestResponse(
    @SerializedName("resource_name") val resourceName: String,
    @SerializedName("manifest_content") val manifestContent: String,
    @SerializedName("manifest_filename") val manifestFileName: String
)
package com.gitlab.marcodsl

import com.gitlab.marcodsl.cdp.CEFClient
import com.gitlab.marcodsl.server.dto.AssetResponse
import com.gitlab.marcodsl.server.dto.ManifestResponse
import com.gitlab.marcodsl.utils.logger
import com.gitlab.marcodsl.utils.toPlainText
import java.io.File
import java.net.InetAddress


class Resource(val name: String, private val rootFolder: String) {

    lateinit var manifest: ManifestResponse
    var manifestType: ManifestType =
        ManifestType.NO_MANIFEST

    val assets: MutableList<AssetResponse> = mutableListOf()

    private var files: MutableList<String> = mutableListOf()

    fun fetchManifest() {
        val script =
            "fetchAsBlob(\"%URL\").then(convertBlobToBase64).then(e=>{const t={resource_name:\"%RESOURCE_NAME\",manifest_content:e,manifest_filename:\"%FILENAME\"},n={method:\"POST\",headers:{Accept:\"application/json\",\"Content-Type\":\"application/json\"},body:JSON.stringify(t)};e.includes(\"data:\")||fetch(\"http://%HTTP_SERVER:40120/manifest\",n).then(console.log)});"
                .replace("%URL", "nui://$name/%FILE")
                .replace("%RESOURCE_NAME", name)
                .replace("%FILENAME", "%FILE")
                .replace("%HTTP_SERVER", InetAddress.getLocalHost().hostAddress)

        CEFClient.executeJavascript(script.replace("%FILE", "__resource.lua"))
        CEFClient.executeJavascript(script.replace("%FILE", "fxmanifest.lua"))
    }

    fun fetchAssets() {
        logger.info("[$name] Fetching assets...")

        val script =
            "fetchAsBlob(\"%URL\").then(convertBlobToBase64).then(e=>{const t={resource_name:\"%RESOURCE_NAME\",asset_content:e,asset_filename:\"%FILENAME\",asset_path:\"%PATH\"},n={method:\"POST\",headers:{Accept:\"application/json\",\"Content-Type\":\"application/json\"},body:JSON.stringify(t)};e.includes(\"data:\")||fetch(\"http://%HTTP_SERVER:40120/asset\",n).then(console.log)});"

        for (file in files) {
            val fileName = if (file.contains('/')) file.split('/').last() else file

            CEFClient.executeJavascript(
                script.replace("%URL", "nui://$name/$file")
                    .replace("%RESOURCE_NAME", name)
                    .replace("%FILENAME", fileName)
                    .replace("%PATH", file)
                    .replace("%HTTP_SERVER", InetAddress.getLocalHost().hostAddress)
            )
        }
    }

    fun processManifestFile() {
        val manifest = this.manifest.manifestContent.toPlainText()
        val matches = "([\"'])(?:(?=(\\\\?))\\2.)*?\\1".toRegex().findAll(manifest)

        files = matches.map { it.groupValues[0] }.map { it.replace("'", "").replace("\"", "") }
            .filter {
                it.manifestCondition()
            }.toMutableList()
    }

    private fun String.manifestCondition(): Boolean {
        if (this.isEmpty() || this.contains("@") || this.last() == '/')
            return false
        return true
    }

    fun saveManifestToFile() {
        File("$rootFolder/$name").apply {
            if (this.exists())
                this.deleteRecursively()

            this.mkdirs()
        }

        File("$rootFolder/$name/${manifestType.fileName}").apply {
            if (this.exists())
                this.delete()

            this.parentFile.mkdirs()
            this.createNewFile()

            this.bufferedWriter().use { it.write(manifest.manifestContent.toPlainText()) }
        }
    }

    fun saveAssetsToFile() {
        for (asset in assets) {
            File("$rootFolder/$name/${asset.assetPath}").apply {
                if (!this.exists()) {
                    this.parentFile.mkdirs()
                    this.createNewFile()
                }

                this.bufferedWriter().use { it.write(asset.assetContent.toPlainText()) }
            }
        }
    }

    @Suppress("unused")
    enum class ManifestType(val fileName: String) {

        OLD_MANIFEST("__resource.lua"),
        FXMANIFEST("fxmanifest.lua"),
        NO_MANIFEST("none");

        companion object {
            fun fromFileName(fileName: String): ManifestType = values().first { it.fileName == fileName }
        }

    }

}
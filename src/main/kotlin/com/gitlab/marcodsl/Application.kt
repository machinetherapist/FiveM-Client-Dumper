package com.gitlab.marcodsl

import com.gitlab.marcodsl.exceptions.DumperException
import com.gitlab.marcodsl.server.ServerFactory
import com.gitlab.marcodsl.utils.fromJson
import com.gitlab.marcodsl.utils.logger
import com.gitlab.marcodsl.utils.sleepWhile
import java.io.File
import java.net.URL
import kotlin.system.exitProcess


class Application(private val address: String, private val port: Int, outputFolder: File) {

    private val serverInfo by lazy { retrieveServerInfo() }
    internal val resources by lazy {
        serverInfo.resources.map {
            Resource(
                it,
                outputFolder.name
            )
        }.toMutableList()
    }

    internal var lastRequest: Long = 0

    init {
        val server = ServerFactory.createNewServer(this)

        if (!outputFolder.exists())
            outputFolder.mkdir()

        if (!outputFolder.isDirectory)
            throw DumperException("O arquivo de saída não é um diretório.")

        server.start()

        logger.info("Fetching manifest files...")
        resetFetchTimer()
        resources.forEach(Resource::fetchManifest)
        waitFetchComplete()

        resources.removeIf { it.manifestType == Resource.ManifestType.NO_MANIFEST }
        logger.info("Fetched ${resources.size} manifest files.\n")

        resources.forEach(Resource::processManifestFile)
        resources.forEach(Resource::saveManifestToFile)

        logger.info("Fetching resources assets...")

        resetFetchTimer()
        resources.forEach(Resource::fetchAssets)
        waitFetchComplete()

        logger.info("All resources fetched.")

        logger.info("Saving resources to file...")
        resources.forEach(Resource::saveAssetsToFile)
        logger.info("Done. Client-side dumped to ${outputFolder.absolutePath}")

        exitProcess(0)
    }

    private fun retrieveServerInfo(): ServerInfo {
        val url = "http://$address:$port/info.json"
        val content = URL(url).readText()

        return gson.fromJson(content)
    }

    private fun waitFetchComplete() =
        sleepWhile(lastRequest < 0 || System.currentTimeMillis() - lastRequest > 5000)

    private fun resetFetchTimer() {
        lastRequest = -1
    }

}
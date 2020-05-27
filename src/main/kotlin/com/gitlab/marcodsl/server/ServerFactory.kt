package com.gitlab.marcodsl.server

import com.gitlab.marcodsl.Resource
import com.gitlab.marcodsl.Application
import com.gitlab.marcodsl.server.dto.AssetResponse
import com.gitlab.marcodsl.server.dto.ManifestResponse
import com.gitlab.marcodsl.utils.logger
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import java.text.DateFormat

object ServerFactory {

    fun createNewServer(application: Application): NettyApplicationEngine = embeddedServer(Netty, 40120) {
        install(ContentNegotiation) {
            gson {
                setDateFormat(DateFormat.LONG)
                setPrettyPrinting()
            }
        }

        routing {

            post("manifest") {
                val response = call.receive<ManifestResponse>()

                val resource = application.resources.firstOrNull { it.name == response.resourceName }
                if (resource != null) {
                    logger.info("[${response.resourceName}] Manifest file fetched.")

                    resource.manifest = response
                    resource.manifestType = Resource.ManifestType.fromFileName(response.manifestFileName)
                }

                application.lastRequest = System.currentTimeMillis()
                call.respond(HttpStatusCode.OK)
            }

            post("asset") {
                val response = call.receive<AssetResponse>()

                val resource = application.resources.firstOrNull { it.name == response.resourceName }

                if (resource != null) {
                    logger.info("[${response.resourceName}] File `${response.assetFileName}` fetched.")
                    resource.assets.add(response)
                }

                application.lastRequest = System.currentTimeMillis()
                call.respond(HttpStatusCode.OK)
            }

        }
    }

}
@file:JvmName("Main")

package com.gitlab.marcodsl

import com.gitlab.marcodsl.utils.logger
import com.google.gson.GsonBuilder
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import java.io.File
import kotlin.system.exitProcess


internal val gson by lazy { GsonBuilder().setPrettyPrinting().create() }

fun main(args: Array<String>) {

    val parser = DefaultParser()
    val options = prepareOptions()

    try {

        val commandLine = parser.parse(options, args)

        val address = commandLine.getOptionValue("address")
        val port = commandLine.getOptionValue("port", "30120")
        val outputFolder = commandLine.getOptionValue("ouptut", address)

        try {
            Application(address, port.toInt(), File(outputFolder))
        } catch (exception: Exception) {
            logger.error("Não foi possível prosseguir com a aplicação: ${exception.message}")
            exitProcess(-1)
        }

    } catch (exception: ParseException) {
        val jarPath = exception::class.java.protectionDomain.codeSource.location.path

        println(exception.message)
        HelpFormatter().printHelp(File(jarPath).name, options)
    }

}

private fun prepareOptions(): Options {

    return Options().apply {
        addRequiredOption("a", "address", true, "Server address")
        addOption("p", "port", true, "Server port")
        addOption("o", "output", true, "Output folder")
    }
}


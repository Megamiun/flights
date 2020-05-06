package br.com.gabryel.flights

import br.com.gabryel.flights.cli.StreamHandler
import br.com.gabryel.flights.cli.StreamServer
import br.com.gabryel.flights.common.Server
import br.com.gabryel.flights.common.Edge
import br.com.gabryel.flights.common.RouteManager
import br.com.gabryel.flights.rest.RestServer
import br.com.gabryel.flights.rest.RoutesHandler
import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val inputStream = getInputStream(args.firstOrNull())
    val routeManager = RouteManager.with(getLines(inputStream))

    val clients = listOf(StreamServer(StreamHandler.cliHandler(routeManager)), RestServer(RoutesHandler(routeManager)))
    clients.forEach(Server::start)

    try {
        while (true) { }
    } catch (e: InterruptedException) {
        clients.forEach(Server::stop)
    }
}

private fun getInputStream(file: String?): InputStream {
    if (file.isNullOrEmpty())
        return Server::class.java.getResourceAsStream("/default.csv")

    return File(file).inputStream()
}

private fun getLines(stream: InputStream): List<Edge> =
    stream.reader().readLines().map {
        val (origin, end, value) = it.split(",")
        Edge(origin, end, value.toInt())
    }
package br.com.gabryel.flights

import br.com.gabryel.flights.cli.CliClient
import br.com.gabryel.flights.common.Client
import br.com.gabryel.flights.common.Edge
import br.com.gabryel.flights.common.RouteManager
import br.com.gabryel.flights.rest.RestClient
import java.io.File
import java.io.InputStream

fun main(args: Array<String>) {
    val inputStream = getInputStream(args.firstOrNull())
    val routeManager = RouteManager(getLines(inputStream).toMutableMap())

    val clients = listOf(CliClient(routeManager), RestClient(routeManager))

    clients.forEach(Client::start)

    try {
        while (true) { }
    } catch (e: InterruptedException) {
        clients.forEach(Client::stop)
    }
}

private fun getInputStream(file: String?): InputStream {
    if (file.isNullOrEmpty())
        return Client::javaClass.javaClass.getResourceAsStream("/default.csv")

    return File(file).inputStream()
}

private fun getLines(stream: InputStream): Map<String, List<Edge>> =
    stream.reader().readLines().map {
        val (origin, end, value) = it.split(",")

        origin to (end to Integer.valueOf(value))
    }.groupBy({ it.first }) { it.second }
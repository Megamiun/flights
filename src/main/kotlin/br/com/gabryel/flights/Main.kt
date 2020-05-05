package br.com.gabryel.flights

import br.com.gabryel.flights.cli.CliClient
import br.com.gabryel.flights.common.Client
import br.com.gabryel.flights.common.Edge
import br.com.gabryel.flights.common.RouteManager
import br.com.gabryel.flights.rest.RestClient
import java.io.File

fun main(args: Array<String>) {
    val file = args.first()
    val routeManager = RouteManager(getLines(file).toMutableMap())

    val clients = listOf(CliClient(routeManager), RestClient(routeManager))

    clients.forEach(Client::start)

    while (true) { }
}

private fun getLines(file: String): Map<String, List<Edge>> {
    return File(file).readLines().map {
        val (origin, end, value) = it.split(",")

        origin to (end to Integer.valueOf(value))
    }.groupBy({ it.first }) { it.second }
}
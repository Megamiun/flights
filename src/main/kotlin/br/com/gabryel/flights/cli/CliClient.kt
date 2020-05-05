package br.com.gabryel.flights.cli

import br.com.gabryel.flights.common.Client
import br.com.gabryel.flights.common.Route
import br.com.gabryel.flights.common.RouteManager
import java.util.*
import java.util.concurrent.Executors

class CliClient(private val routeManager: RouteManager): Client {

    val executor = Executors.newSingleThreadExecutor()

    override fun start() {
        executor.execute {
            val scanner = Scanner(System.`in`)
            print("Please enter the route: ")

            while (scanner.hasNextLine()) {
                val (origin, end) = scanner.nextLine().split("-")

                println(routeManager.findRoute(origin, end).asString())
            }
        }
    }

    override fun finish() {
        executor.shutdown()
    }

    private fun Pair<Route, Int>?.asString(): String {
        this ?:
            return "There is no route between the two points. Could you try again?"

        val path = first.asSequence().joinToString(" - ") { it.value }

        return "$path > $second"
    }
}
package br.com.gabryel.flights.cli

import br.com.gabryel.flights.common.Client
import br.com.gabryel.flights.common.Route
import br.com.gabryel.flights.common.RouteManager
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executors

class CliClient(private val routeManager: RouteManager): Client {

    val executor = Executors.newSingleThreadExecutor()

    override fun start() {
        executor.execute {
            val scanner = Scanner(System.`in`)
            while (true) {
                try {
                    print("Please enter the route: ")
                    System.out.flush()

                    val (origin, end) = scanner.nextLine().split("-")

                    val route = routeManager.findRoute(origin, end).asString()
                    println("Best route: $route")
                } catch (e: InterruptedException) {
                    println("Goodbye, my friends")
                } catch (e: Exception) {
                    println("There was an error executing this query. Maybe there was something wrong in the query?")
                }
            }
        }
    }

    override fun stop() {
        executor.shutdownNow()
    }

    private fun Pair<Route, Int>?.asString(): String {
        this ?: return "There is no route between the two points. Could you try again?"
        return first.getFormattedRouteFor(second)
    }
}
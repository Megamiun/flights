package br.com.gabryel.flights.cli

import br.com.gabryel.flights.common.RouteManager
import java.io.InputStream
import java.io.OutputStream

class StreamHandler(
    private val routeManager: RouteManager,
    inputStream: InputStream,
    outputStream: OutputStream
) {
    companion object {
        fun cliHandler(routeManager: RouteManager) = StreamHandler(routeManager, System.`in`, System.out)
    }

    private val reader = inputStream.bufferedReader()
    private val writer = outputStream.bufferedWriter()

    fun execute() {
        try {
            writer.append("Please enter the route: ")
            writer.flush()

            val (origin, end) = reader.readLine().split("-")

            val path = routeManager.findRoute(origin, end)?.getFormattedPath()

            if (path != null) writer.appendln("Best route: $path")
            else writer.appendln("There is no route between the two points. Could you try again?")
        } catch (e: Exception) {
            writer.appendln("There was an error executing this query. Maybe there was something wrong in the query?")
        }

        writer.flush()
    }
}
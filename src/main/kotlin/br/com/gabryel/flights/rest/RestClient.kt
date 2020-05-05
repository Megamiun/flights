package br.com.gabryel.flights.rest

import br.com.gabryel.flights.common.Client
import br.com.gabryel.flights.common.Route
import br.com.gabryel.flights.common.RouteManager
import br.com.gabryel.flights.rest.model.AddRouteRequest
import br.com.gabryel.flights.rest.model.PathRequest
import br.com.gabryel.flights.rest.model.PathResponse
import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class RestClient(private val routeManager: RouteManager): Client {

    private val gson = Gson()

    private val server = createServer()

    override fun start() = server.start()

    override fun finish() = server.stop(0)

    private fun createServer(): HttpServer {
        val serverPort = 8000
        val server = HttpServer.create(InetSocketAddress(serverPort), 0)

        server.createContext("/api/routes") { exchange ->
            when(exchange.requestMethod) {
                "GET" -> getRoutes(exchange)
                "POST" -> postRoutes(exchange)
                "PUT" -> putRoutes(exchange)
                else -> exchange.write(405)
            }
        }

        server.executor = Executors.newCachedThreadPool()

        return server
    }

    private fun postRoutes(exchange: HttpExchange) {
        val request = gson.fromJson(exchange.requestBody.reader(), PathRequest::class.java)
        exchange.answerWithPath(request.origin, request.end)
    }

    private fun getRoutes(exchange: HttpExchange) {
        val query = exchange.requestURI.query?.split("&").orEmpty()

        val origin = findParameter(query, "origin")
        val end = findParameter(query, "end")

        exchange.answerWithPath(origin, end)
    }

    private fun putRoutes(exchange: HttpExchange) {
        val request = gson.fromJson(exchange.requestBody.reader(), AddRouteRequest::class.java)

        exchange.insert(request.origin, request.end, request.price)
    }

    private fun HttpExchange.insert(origin: String? = null, end: String? = null, priceString: String? = null) {
        if (origin.isNullOrBlank() || end.isNullOrBlank() || priceString.isNullOrBlank()) {
            writeObject(400, Error("No 'origin' or 'end' or 'price' parameter was given"))
            return
        }

        val price = Integer.getInteger(priceString)

        if (price == null) {
            writeObject(400, Error("No numerical 'price' was given"))
            return
        }

        routeManager.insertRoute(origin, end, price)
        write(200)
    }

    private fun HttpExchange.answerWithPath(origin: String? = null, end: String? = null) {
        if (origin.isNullOrBlank() || end.isNullOrBlank()) {
            writeObject(400, Error("No 'origin' or 'end' parameter was given"))
            return
        }

        val result = routeManager.findRoute(origin, end)

        if (result == null) {
            write(204)
            return
        }

        val route = result.first
        val price = result.second
        val path = route.asSequence().toList().map(Route::value)

        writeObject(200, PathResponse(path, getFormattedString(route, price), price))
    }

    private fun <T> HttpExchange.writeObject(status: Int, message: T? = null) {
        if (message == null) {
            write(status)
            return
        }

        write(status, gson.toJson(message).toByteArray())
    }

    private fun HttpExchange.write(status: Int, bytes: ByteArray = ByteArray(0)) {
        sendResponseHeaders(status, bytes.size.toLong())
        responseBody.write(bytes)
        responseBody.close()
    }

    private fun getFormattedString(route: Route, value: Int): String {
        val path = route.asSequence().joinToString(" - ") { it.value }

        return "$path > $value"
    }

    private fun findParameter(query: List<String>, parameter: String) =
        query.firstOrNull { it.startsWith(parameter) }.orEmpty()
            .substringAfter("=", "")
}
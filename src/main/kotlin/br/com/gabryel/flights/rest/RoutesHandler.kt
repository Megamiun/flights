package br.com.gabryel.flights.rest

import br.com.gabryel.flights.common.Edge
import br.com.gabryel.flights.common.RouteManager
import br.com.gabryel.flights.rest.model.AddRouteRequest
import br.com.gabryel.flights.rest.model.ErrorMessage
import br.com.gabryel.flights.rest.model.PathRequest
import br.com.gabryel.flights.rest.model.PathResponse
import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.nio.charset.Charset

class RoutesHandler(
    private val routeManager: RouteManager,
    private val charset: Charset = Charsets.UTF_8
): HttpHandler {

    private val gson = Gson()

    override fun handle(exchange: HttpExchange) {
        when(exchange.requestMethod) {
            "GET" -> getRoutes(exchange)
            "POST" -> postRoutes(exchange)
            "PUT" -> putRoutes(exchange)
            else -> exchange.write(405)
        }
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
            writeObject(400, ErrorMessage("No 'origin' or 'end' or 'price' parameter was given"))
            return
        }

        val price = priceString.toIntOrNull()

        if (price == null) {
            writeObject(400, ErrorMessage("No numerical 'price' was given"))
            return
        }

        routeManager.insertRoute(Edge(origin, end, price))
        write(200)
    }

    private fun HttpExchange.answerWithPath(origin: String? = null, end: String? = null) {
        if (origin.isNullOrBlank() || end.isNullOrBlank()) {
            writeObject(400, ErrorMessage("No 'origin' or 'end' parameter was given"))
            return
        }

        val result = routeManager.findRoute(origin, end)

        if (result == null) {
            write(204)
            return
        }

        writeObject(200, PathResponse(result.asList(), result.getFormattedPath(), result.price))
    }

    private fun <T> HttpExchange.writeObject(status: Int, message: T? = null) {
        if (message == null) {
            write(status)
            return
        }

        write(status, gson.toJson(message).toByteArray(charset))
    }

    private fun HttpExchange.write(
        status: Int,
        bytes: ByteArray = ByteArray(0),
        contentType: String = "application/json"
    ) {
        responseHeaders.set("content-type", "$contentType; charset=${charset.displayName()}")
        sendResponseHeaders(status, getLength(bytes))
        responseBody.write(bytes)
        responseBody.close()
    }

    private fun getLength(bytes: ByteArray) = if (bytes.isNotEmpty()) bytes.size.toLong() else -1

    private fun findParameter(query: List<String>, parameter: String) =
        query.firstOrNull { it.startsWith(parameter) }.orEmpty()
            .substringAfter("=", "")
}
package br.com.gabryel.flights.server

import br.com.gabryel.flights.common.Path
import br.com.gabryel.flights.common.Edge
import br.com.gabryel.flights.common.RouteManager
import br.com.gabryel.flights.rest.RoutesHandler
import br.com.gabryel.flights.rest.model.PathResponse
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

class RoutesHandlerTest {

    private val gson = Gson()

    @Test
    fun `when method is not GET, PUT or POST, then should write a 405`() {
        val hook = spy<(Int, Long) -> Unit>()
        val exchange = createExchange("PATCH", "api/routes", sendResponseHeaders = hook)

        RoutesHandler(mock()).handle(exchange)

        verify(hook)(405, -1)
    }

    @Test
    fun `when method is GET and no origin was given, then should write a 400`() {
        val hook = spy<(Int, Long) -> Unit>()
        val exchange = createExchange("GET", "api/routes?end=B", sendResponseHeaders = hook)

        RoutesHandler(mock()).handle(exchange)

        verify(hook)(eq(400), any())
    }

    @Test
    fun `when method is GET and no end was given, then should write a 400`() {
        val hook = spy<(Int, Long) -> Unit>()
        val exchange = createExchange("GET", "api/routes?origin=A", sendResponseHeaders = hook)

        RoutesHandler(mock()).handle(exchange)

        verify(hook)(eq(400), any())
    }

    @Test
    fun `when method is GET and no path was found, then should write a 204`() {
        val hook = spy<(Int, Long) -> Unit>()
        val exchange = createExchange("GET", "api/routes?origin=A&end=B", sendResponseHeaders = hook)

        val routeManager = mock<RouteManager> {
            on { findRoute("A", "B") } doReturn null
        }

        RoutesHandler(routeManager).handle(exchange)

        verify(hook)(204, -1)
    }

    @Test
    fun `when method is GET and a path was found, then should write a 200`() {
        val hook = spy<(Int, Long) -> Unit>()
        val exchange = createExchange("GET", "api/routes?origin=A&end=B", sendResponseHeaders = hook)

        val routeManager = mock<RouteManager> {
            on { findRoute("A", "B") } doReturn Path("A", 1, Path("B", 0))
        }

        RoutesHandler(routeManager).handle(exchange)

        verify(hook)(eq(200), any())
    }

    @Test
    fun `when method is GET and a path was found, then should write the content to json`() {
        val stream = ByteArrayOutputStream()
        val exchange = createExchange("GET", "api/routes?origin=A&end=B", responseBodyStream = stream)

        val routeManager = mock<RouteManager> {
            on { findRoute("A", "B") } doReturn Path("B", 1, Path("A", 0))
        }

        RoutesHandler(routeManager).handle(exchange)

        val response = gson.fromJson(stream.toByteArray().toString(Charsets.UTF_8), PathResponse::class.java)

        assertAll(
            { assertThat("price", response.price, equalTo(1)) },
            { assertThat("path", response.path, hasItems("A", "B")) },
            { assertThat("formattedPath", response.formattedPath, equalTo("A - B > \$1")) }
        )
    }

    @Test
    fun `when method is POST and a path was found, then should write the content to json`() {
        val output = ByteArrayOutputStream()
        val input = """{"origin": "A", "end": "B"}"""
        val exchange = createExchange("POST", "api/routes", responseBodyStream = output, requestBodyString = input)

        val routeManager = mock<RouteManager> {
            on { findRoute("A", "B") } doReturn Path("B", 1, Path("A", 0))
        }

        RoutesHandler(routeManager).handle(exchange)

        val response = gson.fromJson(output.toByteArray().toString(Charsets.UTF_8), PathResponse::class.java)

        assertAll(
            { assertThat("price", response.price, equalTo(1)) },
            { assertThat("path", response.path, hasItems("A", "B")) },
            { assertThat("formattedPath", response.formattedPath, equalTo("A - B > \$1")) }
        )
    }

    @Test
    fun `when method is PUT and price was not given, then should write a 400`() {
        val hook = spy<(Int, Long) -> Unit>()
        val input = """{"origin": "A", "end": "B"}"""
        val exchange = createExchange("PUT", "api/routes", requestBodyString = input, sendResponseHeaders = hook)

        RoutesHandler(mock()).handle(exchange)

        verify(hook)(eq(400), any())
    }

    @Test
    fun `when method is PUT and price is not numerical, then should write a 400`() {
        val hook = spy<(Int, Long) -> Unit>()
        val input = """{"origin": "A", "end": "B", "price": "A - B"}"""
        val exchange = createExchange("PUT", "api/routes", requestBodyString = input, sendResponseHeaders = hook)

        RoutesHandler(mock()).handle(exchange)

        verify(hook)(eq(400), any())
    }

    @Test
    fun `when method is PUT, then should add a route to route manager`() {
        val input = """{"origin": "A", "end": "B", "price": 45}"""
        val exchange = createExchange("PUT", "api/routes", requestBodyString = input)

        val routeManager = mock<RouteManager>()
        RoutesHandler(routeManager).handle(exchange)

        verify(routeManager).insertRoute(Edge("A", "B", 45))
    }

    private fun createExchange(
        method: String,
        uri: String,
        headers: Headers = Headers(),
        responseBodyStream: OutputStream = ByteArrayOutputStream(),
        requestBodyString: String = "",
        sendResponseHeaders: (Int, Long) -> Unit = { _, _ -> }
    ): HttpExchange {
        return mock {
            on { it.sendResponseHeaders(any(), any()) } doAnswer { inv ->
                sendResponseHeaders(inv.getArgument(0), inv.getArgument(1))
            }
            on { responseBody } doReturn responseBodyStream
            on { responseHeaders } doReturn headers

            on { requestBody } doReturn requestBodyString.byteInputStream()
            on { requestMethod } doReturn method
            on { requestURI } doReturn URI(uri)
        }
    }
}
package br.com.gabryel.flights.cli

import br.com.gabryel.flights.common.BacktrackPath
import br.com.gabryel.flights.common.RouteManager
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class StreamHandlerTest {

    @Test
    fun `given invalid input, should write error`() {
        val output = ByteArrayOutputStream()
        val input = "AB".byteInputStream()

        StreamHandler(mock(), input, output).execute()

        assertThat(
            output.toByteArray().toString(Charsets.UTF_8),
            containsString("There was an error executing this query.")
        )
    }

    @Test
    fun `given an input that has no path, when asked for path, should write that it didn't find any path`() {
        val output = ByteArrayOutputStream()
        val input = "A-B".byteInputStream()

        StreamHandler(mock(), input, output).execute()

        assertThat(
            output.toByteArray().toString(Charsets.UTF_8),
            containsString("There is no route between the two points.")
        )
    }

    @Test
    fun `given an input that has a path, when asked for path, should write it`() {
        val output = ByteArrayOutputStream()
        val input = "A-B".byteInputStream()

        val routeManager = mock<RouteManager> {
            on { findRoute("A", "B") } doReturn BacktrackPath("B", 0, BacktrackPath("A", 1))
        }

        StreamHandler(routeManager, input, output).execute()

        assertThat(
            output.toByteArray().toString(Charsets.UTF_8),
            containsString("Best route: A - B > 1")
        )
    }

    @Test
    fun `given there ae two requests to handler, should ask a second time`() {
        val output = ByteArrayOutputStream()
        val input = "A-B\nA-B".byteInputStream()

        val handler = StreamHandler(mock(), input, output)

        handler.execute()
        output.reset()
        handler.execute()

        assertThat(
            output.toByteArray().toString(Charsets.UTF_8),
            containsString("There is no route between the two points.")
        )
    }
}
package br.com.gabryel.flights.common

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class RouteManagerTest {

    @Test
    fun `when asked for a path that the start and end is equal, should return itself with price 0`() {
        val manager = RouteManager()

        val result = manager.findRoute("BRC", "BRC")

        assertAll(
            { assertThat("node", result?.first?.value, `is`("BRC")) },
            { assertThat("price", result?.second, `is`(0)) }
        )
    }

    @Test
    fun `given there is just a route, when asked for a path from origin to origin, should return a single item list with only origin`() {
        val items = mutableMapOf("BRC" to listOf("GRU" to 5))
        val manager = RouteManager(items)

        val result = manager.findRoute("BRC", "BRC")

        assertAll(
            { assertThat("first node", result?.first?.value, `is`("BRC")) },
            { assertThat("second node", result?.first?.next, `is`(nullValue())) },
            { assertThat("price", result?.second, `is`(0)) }
        )
    }

    @Test
    fun `given there is just a route, when asked for a path from origin to end, should return a single item list with origin and end`() {
        val items = mutableMapOf("BRC" to listOf("GRU" to 5))
        val manager = RouteManager(items)

        val result = manager.findRoute("BRC", "GRU")

        assertAll(
            { assertThat("first node", result?.first?.value, `is`("BRC")) },
            { assertThat("second node", result?.first?.next?.value, `is`("GRU")) },
            { assertThat("price", result?.second, `is`(5)) }
        )
    }

    @Test
    fun `given there is just a route, when asked for a path from end to origin, should return nothing`() {
        val items = mutableMapOf("BRC" to listOf("GRU" to 5))
        val manager = RouteManager(items)

        val result = manager.findRoute("GRU", "BRC")

        assertThat(result, `is`(nullValue()))
    }

    @Test
    fun `given there are multiple routes, when asked for a path that takes two steps, should return path`() {
        val items = mutableMapOf(
            "BRC" to listOf("GRU" to 5),
            "GRU" to listOf("CGL" to 10)
        )

        val manager = RouteManager(items)

        val result = manager.findRoute("BRC", "CGL")

        assertAll(
            { assertThat("first node", result?.first?.value, `is`("BRC")) },
            { assertThat("second node", result?.first?.next?.value, `is`("GRU")) },
            { assertThat("third node", result?.first?.next?.next?.value, `is`("CGL")) },
            { assertThat("price", result?.second, `is`(15)) }
        )
    }

    @Test
    fun `given there are multiple routes, when asked for a path, should return route with lesser value`() {
        val items = mutableMapOf(
            "GRU" to listOf("BRC" to 10, "CDG" to 75, "SCL" to 20, "ORL" to 56),
            "BRC" to listOf("SCL" to 5),
            "ORL" to listOf("CDG" to 5),
            "SCL" to listOf("ORL" to 20)
        )

        val manager = RouteManager(items)

        val result = manager.findRoute("GRU", "CDG")

        assertThat("price", result?.second, `is`(40))
    }
}
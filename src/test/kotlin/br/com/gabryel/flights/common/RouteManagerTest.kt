package br.com.gabryel.flights.common

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class RouteManagerTest {

    @Test
    fun `when asked for a path where points doesn't exists, should return nothing`() {
        val manager = RouteManager.with(listOf(Edge("ORL", "CDG", 100)))

        val result = manager.findRoute("BRC", "GRU")

        assertThat(result, `is`(nullValue()))
    }

    @Test
    fun `when asked for a path where there is no viable path, should return nothing`() {
        val manager = RouteManager.with(listOf(
            Edge("ORL", "CDG", 100),
            Edge("BRC", "GRU", 100)
        ))

        val result = manager.findRoute("ORL", "GRU")

        assertThat(result, `is`(nullValue()))
    }


    @Test
    fun `given there is just a route, when asked for a path from origin to origin, should return a single item list with only origin`() {
        val manager = RouteManager.with(listOf(Edge("BRC", "GRU", 5)))

        val result = manager.findRoute("BRC", "BRC")

        assertAll(
            { assertThat("nodes", result?.asList(), hasItem("BRC")) },
            { assertThat("price", result?.price, `is`(0)) }
        )
    }

    @Test
    fun `given there is just a route, when asked for a path from origin to end, should return a single item list with origin and end`() {
        val manager = RouteManager.with(listOf(Edge("BRC", "GRU", 5)))

        val result = manager.findRoute("BRC", "GRU")

        assertAll(
            { assertThat("nodes", result?.asList(), hasItems("BRC", "GRU")) },
            { assertThat("price", result?.price, `is`(5)) }
        )
    }

    @Test
    fun `given there is just a route, when asked for a path from end to origin, should return path`() {
        val manager = RouteManager.with(listOf(Edge("BRC", "GRU", 5)))

        val result = manager.findRoute("GRU", "BRC")
        assertAll(
            { assertThat("nodes", result?.asList(), hasItems("GRU", "BRC")) },
            { assertThat("price", result?.price, `is`(5)) }
        )
    }

    @Test
    fun `given there are multiple routes, when asked for a path that takes two steps, should return path`() {
        val manager = RouteManager.with(listOf(
            Edge("BRC", "GRU", 5),
            Edge("GRU", "CGL", 10)
        ))

        val result = manager.findRoute("BRC", "CGL")

        assertAll(
            { assertThat("nodes", result?.asList(), hasItems("BRC", "GRU", "CGL")) },
            { assertThat("price", result?.price, `is`(15)) }
        )
    }

    @Test
    fun `given there are multiple routes, when asked for a path, should return route with lesser value`() {
        val manager = RouteManager.with(listOf(
            Edge("GRU", "BRC", 10),
            Edge("GRU", "CDG", 75),
            Edge("GRU", "SCL", 20),
            Edge("GRU", "ORL", 56),
            Edge("BRC", "SCL", 5),
            Edge("ORL", "CDG", 5),
            Edge("SCL", "ORL", 20)
        ))

        val result = manager.findRoute("GRU", "CDG")

        assertThat("price", result?.price, `is`(40))
    }
}
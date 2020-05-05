package br.com.gabryel.flights.common

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class RouteTest {

    @Test
    fun `given there are x levels of routes, when asked for their sequence, should return a list with x items`() {
        val route = Route("A", Route("B", Route("C")))

        assertThat(route.asSequence().toList(), hasItems("A", "B", "C"))
    }

    @Test
    fun `given there are x levels of routes, when asked for their string representation, should concatenate in order`() {
        val route = Route("A", Route("B", Route("C")))

        assertThat(route.getFormattedRouteFor(45), `is`("A - B - C > 45"))
    }
}
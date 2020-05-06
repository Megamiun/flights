package br.com.gabryel.flights.common

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class RouteTest {

    @Test
    fun `given there are x levels of routes, when asked for their sequence, should return a list with x items`() {
        val route = BacktrackPath("A", 0, BacktrackPath("B", 0, BacktrackPath("C", 0)))

        assertThat(route.asList().toList(), hasItems("A", "B", "C"))
    }

    @Test
    fun `given there are x levels of routes, when asked for their string representation, should concatenate in order`() {
        val route = BacktrackPath("A", 1, BacktrackPath("B", 2, BacktrackPath("C", 3)))

        assertThat(route.getFormattedPath(), `is`("C - B - A > \$6"))
    }
}
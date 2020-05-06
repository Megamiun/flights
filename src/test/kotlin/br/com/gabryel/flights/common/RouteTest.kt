package br.com.gabryel.flights.common

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class RouteTest {

    @Test
    fun `given there are x levels of routes, when asked for their sequence, should return a list with x items`() {
        val route = Path("A", 0, Path("B", 0, Path("C", 0)))

        assertThat(route.asList().toList(), hasItems("A", "B", "C"))
    }

    @Test
    fun `given there are x levels of routes, when asked for their string representation, should concatenate in order`() {
        val route = Path("A", 1, Path("B", 2, Path("C", 3)))

        assertThat(route.getFormattedPath(), `is`("C - B - A > \$6"))
    }
}
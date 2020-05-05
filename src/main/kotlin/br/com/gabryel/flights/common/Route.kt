package br.com.gabryel.flights.common

data class Route(val value: String, val next: Route? = null) {
    fun asSequence() = generateSequence(this) { it.next }.map(Route::value)

    fun getFormattedRouteFor(value: Int): String {
        val path = asSequence().joinToString(" - ")
        return "$path > $value"
    }
}
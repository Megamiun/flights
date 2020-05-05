package br.com.gabryel.flights.common

data class Route(val next: Route?, val value: String) {
    fun asSequence() = generateSequence(this) { it.next }

    fun getFormattedRouteFor(value: Int): String {
        val path = asSequence().joinToString(" - ") { it.value }
        return "$path > $value"
    }
}
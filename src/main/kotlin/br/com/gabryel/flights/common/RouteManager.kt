package br.com.gabryel.flights.common

typealias Edge = Pair<String, Int>

class RouteManager(private val routes: MutableMap<String, List<Edge>> = mutableMapOf()) {

    fun insertRoute(origin: String, end: String, price: Int) {
        routes.compute(origin) { _, old ->
            val newRoute = end to price
            old?: return@compute listOf(newRoute)

            old + newRoute
        }
    }

    fun findRoute(origin: String, end: String): Pair<Route, Int>? {
        val distances = mutableMapOf(origin to (origin to 0))
        val toVisit = mutableSetOf(origin)
        val visited = mutableSetOf<String>()

        while (toVisit.isNotEmpty()) {
            val current = toVisit.minBy { distances[it]?.second?: Int.MAX_VALUE }
                ?: return null

            visited += current
            toVisit -= current

            val currentData = distances[current]
                ?: return null

            if (current == end)
                return mountRoute(origin, end, distances)?.let { it to currentData.second }

            routes[current]
                ?.filterNot { it.first in visited }
                ?.forEach { (next, distance) ->
                    toVisit += next
                    distances.updateDistanceFor(next, current, distance + currentData.second)
                }
        }

        return null
    }

    private fun MutableMap<String, Edge>.updateDistanceFor(next: String, current: String, distance: Int) {
        compute(next) { _, old ->
            val newPath = current to distance
            old ?: return@compute newPath

            if (old.second < newPath.second) old
            else newPath
        }
    }

    private fun mountRoute(start: String, current: String, distances: Map<String, Edge>, tail: Route? = null): Route? {
        val node = distances[current]
            ?: return null

        if (current == start) return Route(start, tail)

        return mountRoute(start, node.first, distances, Route(current, tail))
    }
}
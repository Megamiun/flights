package br.com.gabryel.flights.common

typealias Edge = Pair<String, Int>

class RouteManager(private val routes: MutableMap<String, List<Edge>>) {

    fun insertRoute(origin: String, end: String, price: Int) {
        routes.compute(origin) { _, old ->
            val newRoute = end to price
            old?: return@compute listOf(newRoute)

            old + newRoute
        }
    }

    fun findRoute(origin: String, end: String): Pair<Route, Int>? {
        val distances = mutableMapOf(origin to (origin to 0))
        val toVisit = routes.keys.toMutableSet()

        while (toVisit.isNotEmpty()) {
            val current = toVisit.minBy { distances[it]?.second?: Int.MAX_VALUE }
                ?: return null

            val currentData = distances[current]
                ?: return null

            if (current == end)
                return mountRoute(origin, end, distances)?.let { it to currentData.second }

            toVisit -= current

            routes[current]?.forEach { (next, distance) ->
                distances.updateDistanceFor(next, currentData.second + distance)
            }
        }

        return null
    }

    private fun MutableMap<String, Edge>.updateDistanceFor(next: String, newDistance: Int) {
        compute(next) { key, old ->
            val newPath = key to newDistance
            old ?: return@compute newPath

            if (old.second < newPath.second) old
            else newPath
        }
    }

    private fun mountRoute(start: String, current: String, distances: Map<String, Edge>, tail: Route? = null): Route? {
        val node = distances[current]
            ?: return null

        if (current == start) return Route(tail, start)

        return mountRoute(start, node.first, distances,
            Route(tail, current)
        )
    }
}
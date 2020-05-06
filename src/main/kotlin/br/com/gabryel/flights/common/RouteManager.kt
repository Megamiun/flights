package br.com.gabryel.flights.common

import java.io.ByteArrayOutputStream
import java.io.OutputStream

class RouteManager(private val output: OutputStream) {

    private val routesCache: MutableMap<String, MutableMap<String, MutableList<Edge>>> = mutableMapOf()

    companion object {
        fun with(routes: List<Edge>, output: OutputStream = ByteArrayOutputStream()): RouteManager {
            val manager = RouteManager(output)
            routes.forEach(manager::insertRoute)
            return manager
        }
    }

    fun insertRoute(edge: Edge) {
        output.write("${edge.point1},${edge.point2},${edge.price}\n".toByteArray())
        output.flush()
        
        insert(edge.point1, edge.point2, edge)
        insert(edge.point2, edge.point1, edge)
    }

    fun findRoute(origin: String, end: String): BacktrackPath? {
        val distances = mutableMapOf(origin to BacktrackPath(origin, 0))
        val toVisit = routesCache.keys.toMutableSet()

        if (origin !in toVisit || end !in toVisit)
            return null

        while (toVisit.isNotEmpty()) {
            val current = toVisit.minBy { distances[it]?.price?: Int.MAX_VALUE }
            val currentData = distances[current]
                ?: break

            toVisit -= currentData.node

            if (current == end)
                return currentData

            routesCache[current]
                ?.filter { it.key in toVisit }
                ?.flatMap { (next, edges) -> edges.map { edge -> next to edge } }
                ?.forEach { (next, edge) ->
                    distances.updatePathFor(next, edge.price, currentData)
                }
        }

        return null
    }

    private fun MutableMap<String, BacktrackPath>.updatePathFor(nextNode: String, nextNodePrice: Int, currentPath: BacktrackPath) {
        compute(nextNode) { _, oldPath ->
            val newPath = BacktrackPath(nextNode, nextNodePrice, currentPath)
            oldPath ?: return@compute newPath

            if (oldPath.price < newPath.price) oldPath
            else newPath
        }
    }

    private fun insert(origin: String, end: String, edge: Edge) {
        routesCache
            .getOrPut(origin) { mutableMapOf() }
            .getOrPut(end) { mutableListOf() }
            .add(edge)
    }
}
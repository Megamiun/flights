package br.com.gabryel.flights.rest

import br.com.gabryel.flights.common.Server
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class RestServer(private val routesHandler: RoutesHandler): Server {

    private val server = createServer()

    override fun start() = server.start()

    override fun stop() = server.stop(0)

    private fun createServer(): HttpServer {
        val serverPort = 8000
        val server = HttpServer.create(InetSocketAddress(serverPort), 0)

        server.createContext("/api/routes", routesHandler)
        server.executor = Executors.newCachedThreadPool()
        return server
    }
}
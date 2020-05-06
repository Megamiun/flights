package br.com.gabryel.flights.cli

import br.com.gabryel.flights.common.Server
import java.util.concurrent.Executors

class StreamServer(private val streamHandler: StreamHandler): Server {
    private val executor = Executors.newSingleThreadExecutor()

    override fun start() {
        executor.execute {
            while (true) { streamHandler.execute() }
        }
    }

    override fun stop() {
        executor.shutdownNow()
    }
}
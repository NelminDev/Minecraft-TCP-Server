package me.nelmin.spigot

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

class TcpServer(private val port: Int) : Runnable {
    private var serverSocket: ServerSocket = ServerSocket(port)
    private val executor = Executors.newCachedThreadPool()
    private val clients = CopyOnWriteArrayList<Socket>() // Thread-safe list of clients
    @Volatile private var running = true

    init {
        println("TCP Server is running on port $port")
    }

    override fun run() {
        while (running) {
            try {
                val clientSocket = serverSocket.accept()
                clients.add(clientSocket) // Add the new client to the list
                println("New client connected: ${clientSocket.inetAddress.hostAddress}")

                executor.submit { handleClient(clientSocket) }
            } catch (e: Exception) {
                if (running) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleClient(socket: Socket) {
        BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
            PrintWriter(socket.getOutputStream(), true).use { writer ->
                var message: String?

                while (reader.readLine().also { message = it } != null) {
                    println("Received: $message")
                    writer.println("Echo: $message") // Echo back to the client
                }
            }
        }
        println("Client disconnected: ${socket.inetAddress.hostAddress}")
        clients.remove(socket) // Remove the client from the list
        socket.close()
    }

    fun sendMessageToClients(message: String) {
        for (client in clients) {
            try {
                PrintWriter(client.getOutputStream(), true).println(message)
            } catch (e: Exception) {
                println("Failed to send message to a client: ${e.message}")
            }
        }
    }

    fun stop() {
        running = false
        executor.shutdownNow()
        for (client in clients) {
            client.close() // Close all client connections
        }
        serverSocket.close()
        println("TCP Server stopped.")
    }
}
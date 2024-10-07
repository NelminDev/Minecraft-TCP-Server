package me.nelmin.spigot

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.Properties

class MinecraftTCP : JavaPlugin() {
    private lateinit var tcpServer: TcpServer

    private val configFile = File(dataFolder.absolutePath, "minecraft_tcp.config")
    private val config = Properties()

    override fun onEnable() {
        logger.info("Starting TCP Server...")

        if (!dataFolder.exists()) dataFolder.mkdirs()
        if (!configFile.exists()) configFile.createNewFile()

        config.load(configFile.inputStream())

        if (
            config.isEmpty
            || config.getProperty("port").toIntOrNull() == null
            || config.getProperty("port").toInt() !in 0..65535
        ) {
            config.setProperty("port", "4444")
            config.store(configFile.writer(), null)
        }

        val port: Int = config.getProperty("port").toInt()

        tcpServer = TcpServer(port)
        Thread(tcpServer).start()

        Bukkit.getPluginManager().registerEvents(ChatListener(tcpServer), this)
    }

    override fun onDisable() {
        logger.info("Stopping TCP Server...")
        tcpServer.stop()
    }
}

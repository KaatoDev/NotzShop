package notzshop

import net.milkbowl.vault.economy.Economy
import notzshop.commands.NShopC
import notzshop.commands.ShopC
import notzshop.events.GuiEv
import notzshop.events.JoinLeaveEv
import notzshop.guis.ShopGUI
import notzshop.managers.ShopM.load
import notzshop.notzapi.NotzAPI
import notzshop.notzapi.NotzAPI.Companion.placeholderManager
import notzshop.notzapi.apis.NotzYAML
import notzshop.notzapi.utils.MessageU.c
import notzshop.notzapi.utils.MessageU.send
import org.bukkit.Bukkit
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class Main : JavaPlugin() {
    companion object {
        lateinit var pathRaw: String
        lateinit var notzAPI: NotzAPI
        lateinit var plugin: JavaPlugin

        lateinit var cf: NotzYAML
        lateinit var msgf: NotzYAML
        lateinit var itf: NotzYAML

        lateinit var shopGUI: ShopGUI

        lateinit var econ: Economy
    }

    override fun onEnable() {
        plugin = this
        pathRaw = dataFolder.absolutePath
        cf = NotzYAML(this, "config")
        msgf = NotzYAML(this, "messages")
        itf = NotzYAML(this, "items")

        notzAPI = NotzAPI(msgf)
        shopGUI = ShopGUI()

        object : BukkitRunnable() {
            override fun run() {
                setupMain()
                load()
            }
        }.runTaskLater(this, (2 * 20).toLong())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun setupMain() {
        regCommands()
        regEvents()
        regTab()
        letters()

        val rsp: RegisteredServiceProvider<Economy> = server.servicesManager.getRegistration(
            Economy::class.java
        )

        if (server.pluginManager.getPlugin("Vault") != null) econ =
            rsp.provider else {
            Bukkit.getConsoleSender().sendMessage(c("&4ECONOMIA NÃO SETADA KRL"))
            server.pluginManager.disablePlugin(this)
        }

        if (Bukkit.getPlayerExact("Gago3242") != null && Bukkit.getPlayerExact("Gago3242").isOnline)
            send(Bukkit.getPlayerExact("Gago3242"), "&eNotzShop iniciado.")
    }

    private fun regEvents() {
        Bukkit.getPluginManager().registerEvents(GuiEv(), this)
        Bukkit.getPluginManager().registerEvents(JoinLeaveEv(), this)
    }

    private fun regCommands() {
        getCommand("shop").executor = ShopC()
        getCommand("nshop").executor = NShopC()
    }

    private fun regTab() {
        getCommand("shop").tabCompleter = ShopC()
        getCommand("nshop").tabCompleter = NShopC()
    }

    private fun letters() {
        Bukkit.getConsoleSender().sendMessage((placeholderManager.set("{prefix} &2Inicializado com sucesso.").plus(
                    c("\n&f┳┓    &6┏┓┓     "
                    + "\n&f┃┃┏┓╋┓&6┗┓┣┓┏┓┏┓"
                    + "\n&f┛┗┗┛┗┗&6┗┛┛┗┗┛┣┛"
                    + "\n&f      &6      ┛ "))))
    }
}

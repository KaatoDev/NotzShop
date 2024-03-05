package notzshop.commands

import notzshop.Main.Companion.shopGUI
import notzshop.managers.ShopM.clearPlayerBag
import notzshop.notzapi.utils.MessageU.c
import notzshop.notzapi.utils.MessageU.formatMoney
import notzshop.notzapi.utils.MessageU.send
import notzshop.notzapi.utils.MessageU.sendHeader
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.*

class NShopC : TabExecutor {
    lateinit var p: Player
    override fun onCommand(sender: CommandSender?, cmd: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender !is Player)
            return false

        p = sender

        when (args!!.size) {
            1 -> if (args[0].lowercase() == "setprice") send(p, "&eUtilize &f/&enshop setprice &f<&eitem&f> <&epreço&f>&e.") else help()
            2 -> {
                when (args[0].lowercase()) {
                    "setprice" -> send(p, "&eUtilize &f/&enshop setprice &f<&eitem&f> <&epreço&f>&e.")
                    "clearbag" -> if (Bukkit.getPlayer(args[1]) != null && Bukkit.getPlayer(args[1]).isOnline) {clearPlayerBag(p, Bukkit.getPlayer(args[1]))} else send(p, "&cEste player não existe ou está offline.")
                    else -> help()
                }
            }
            3 -> if (args[0].lowercase() == "setprice") {
                val item = args[1].lowercase()

                try {
                    if (shopGUI.getTotalItems().contains(item)) {
                        send(p, "&eO preço do item &f${item}&e foi alterado de &2$&a${formatMoney(shopGUI.getPrice(item).toDouble())}&e para &2$&a${formatMoney(args[2].toDouble())}")
                        shopGUI.setPrice(item, args[2].toInt())

                    } else send(p, "&cO item &f${item}&c não existe.")

                } catch (e: IllegalFormatConversionException) {
                    send(p, "&eUtilize números apenas.")
                } catch (e: NumberFormatException) {
                    send(p, "&eUtilize números válidos.")
                }

            } else help()
            else -> help()
        }
        return true
    }

    override fun onTabComplete(p: CommandSender?, cmd: Command?, label: String?, args: Array<out String>?): MutableList<String> {
        return if (args!!.isNotEmpty() && args[0] == "setprice")
            shopGUI.getTotalItems().toMutableList()
        else Collections.emptyList()
    }

    private fun help() {
        sendHeader(p)
        p.sendMessage(c("&f/&enshop setprice &f<&eitem&f> <&epreço&f>&e."))
    }
}
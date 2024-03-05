package notzshop.commands

import notzshop.Main.Companion.shopGUI
import notzshop.managers.ShopM.claimBagItem
import notzshop.managers.ShopM.openBag
import notzshop.notzapi.utils.MessageU.send
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.*

class ShopC : TabExecutor {
    override fun onCommand(p: CommandSender?, cmd: Command?, label: String?, args: Array<out String>?): Boolean {
        if (p !is Player)
            return false

        when (label) {
            "mochila", "bag" -> {
                openBag(p)
                return true
            }
            "resgatar", "claimbag" -> {
                claimBagItem(p, false)
                return true
            }
        }

        if (args!!.isEmpty())
            shopGUI.open(p)
        else send(p, "&eUtilize apenas &f/&eloja")

        return true
    }

    override fun onTabComplete(p0: CommandSender?, p1: Command?, p2: String?, p3: Array<out String>?): MutableList<String> {
        return Collections.emptyList()
    }
}
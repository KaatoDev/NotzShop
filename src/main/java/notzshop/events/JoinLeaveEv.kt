package notzshop.events

import notzshop.managers.ShopM.clearPlayer
import notzshop.managers.ShopM.createCarts
import notzshop.notzapi.utils.MenuU.createLastMenu
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinLeaveEv : Listener {
    @EventHandler
    fun joinEvent(e: PlayerJoinEvent) {
        createLastMenu(e.player)
        createCarts(e.player)
    }

    @EventHandler
    fun leaveEvent(e: PlayerQuitEvent) {
        clearPlayer(e.player)
    }
}
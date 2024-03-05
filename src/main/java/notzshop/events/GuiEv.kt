package notzshop.events

import notzshop.Main.Companion.plugin
import notzshop.Main.Companion.shopGUI
import notzshop.managers.ShopM.claimBagItem
import notzshop.managers.ShopM.isCart
import notzshop.managers.ShopM.setCart
import notzshop.managers.ShopM.shopItem
import notzshop.notzapi.NotzAPI.Companion.itemManager
import notzshop.notzapi.utils.MenuU.openMenu
import notzshop.notzapi.utils.MessageU.c
import notzshop.notzapi.utils.MessageU.send
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.scheduler.BukkitRunnable

class GuiEv : Listener {
    val menuPlayer = hashMapOf<Player, String>()

    @EventHandler
    fun clickGUI(e: InventoryClickEvent) {
        if (e.clickedInventory != null && e.inventory != null && pass(e.inventory.title) && e.currentItem != null && e.currentItem.type != Material.AIR) {

            e.isCancelled = true

            val p = e.whoClicked as Player
            val item = e.currentItem
            menuPlayer[p] = e.inventory.title

            if (e.inventory.title.contains(c("&e[&fMochila de")) && claimBagItem(p, item, true, e.isShiftClick))
                return

            if (item.type != Material.STAINED_GLASS_PANE && item.itemMeta.hasLore() && item.itemMeta.lore.isNotEmpty() && item != itemManager.getItem("default")) {
                if ((!openMenu(p, item) || isCart(item)) && !item.itemMeta.displayName.contains("página", true)) {

                    if (itemManager.containsItem(item) || isCart(item)) {
                        shopItem(p, item, e.click, e.clickedInventory, e.clickedInventory.indexOfFirst { it != null && it.itemMeta.displayName == itemManager.getItem("cart").itemMeta.displayName })

                    } else if (!e.inventory.title.contains("Como comprar")) send(p, "&cNão foi possível abrir este menu.")
                }
            }
        }
    }

    @EventHandler
    fun openGUI(e: InventoryOpenEvent) {
        if (pass(e.inventory.title) && e.inventory.find { it != null && it.itemMeta.displayName == itemManager.getItem("cart").itemMeta.displayName } != null) {
            val p = e.player as Player

            setCart(p, e.inventory, e.inventory.indexOfFirst { it != null && it.itemMeta.displayName == itemManager.getItem("cart").itemMeta.displayName })

            if (e.inventory.title.contains("Como comprar"))
                tutorialSet(p)
        }
    }

    @EventHandler
    fun closeMenu(e: InventoryCloseEvent) {
        if (e.inventory.title != null && pass(e.inventory.title))
            menuPlayer[e.player as Player] = ""
    }

    private fun pass(title: String): Boolean {
        return title.contains(c("&6&l[&f&lLoja&6&l]"))
                || title.contains(c("&e[&fMochila de"))
    }

    private fun tutorialSet(p: Player) {
        object : BukkitRunnable() {
            override fun run() {
                if (p.openInventory.title.contains("Como comprar") && p.openInventory.getItem(4).type == Material.GOLD_NUGGET)
                    shopGUI.openTutorial(p)

                this.cancel()
            }
        }.runTaskLater(plugin, (5 * 20).toLong())
    }
}
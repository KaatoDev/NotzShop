package notzshop.managers

import notzshop.Main.Companion.econ
import notzshop.Main.Companion.shopGUI
import notzshop.managers.DatabaseM.clearBag
import notzshop.managers.DatabaseM.getBag
import notzshop.managers.DatabaseM.getTutorial
import notzshop.managers.DatabaseM.setBag
import notzshop.notzapi.NotzAPI.Companion.itemManager
import notzshop.notzapi.gui.ChestPages
import notzshop.notzapi.utils.EventU.setFunction
import notzshop.notzapi.utils.MenuU.getLastMenu
import notzshop.notzapi.utils.MessageU
import notzshop.notzapi.utils.MessageU.c
import notzshop.notzapi.utils.MessageU.formatMoney
import notzshop.notzapi.utils.MessageU.send
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object ShopM {
    private val cart = hashMapOf<Player, MutableList<ItemStack>>()
    private val undoCart = hashMapOf<Player, MutableList<MutableList<ItemStack>>>()
    private val bags = hashMapOf<Player, MutableList<ItemStack>>()
    val tutorial = hashMapOf<Player, Boolean>()

    fun clearPlayer(p: Player) {
        cart.remove(p)
        undoCart.remove(p)
        bags.remove(p)
    }

    fun isTutorial(p: Player): Boolean {
        return tutorial[p] == null || tutorial[p] == false
    }

    fun shopItem(p: Player, item: ItemStack, click: ClickType, inv: Inventory, cartSlot: Int) {
        val itemC = item.clone()

        if (itemC.hasItemMeta() && itemC.itemMeta.lore[1] == c("&fitem no seu carrinho."))
            addItem(p, itemC, click, inv, cartSlot)

        else if (itemManager.getItem("cart").itemMeta.displayName == itemC.itemMeta.displayName)
            removeItem(p, click, inv, cartSlot)

        else if (itemManager.equalsItem(itemC, "buy") && click == ClickType.DOUBLE_CLICK)
            buyCartContents(p, inv, cartSlot)
    }

    private fun addItem(p: Player, item: ItemStack, click: ClickType, inv: Inventory, cartSlot: Int) {
        val ti = item.clone()
        when (click) {
            ClickType.LEFT -> ti.amount = 1
            ClickType.SHIFT_LEFT -> ti.amount = 64
            ClickType.RIGHT -> ti.amount = 576
            ClickType.SHIFT_RIGHT -> ti.amount = 2304
            ClickType.DOUBLE_CLICK -> return

            else -> {
                send(p, "&eClique com os botões corretos!")
                return
            }
        }

        val ct = inv.getItem(cartSlot).clone()
        var lore = ct.itemMeta.lore

        if (lore[0] == c("&cAtualmente está vazio."))
            lore = mutableListOf(c("&eConteúdo do carrinho:"))

        if (containsCart(p, item)) {
            lore[0] = c("${item.itemMeta.displayName}&f: &b${findItemCart(p, item).amount + ti.amount}")
            findItemCart(p, item).amount += ti.amount
        } else {
            lore.add(c("${item.itemMeta.displayName}&f: &b${ti.amount}"))
            cart[p]!!.add(ti)
        }

        val meta = ct.itemMeta
        meta.lore = lore
        ct.setItemMeta(meta)
        inv.setItem(cartSlot, ct)

        send(p, "&eO item ${item.itemMeta.displayName}&e foi adicionado no seu carrinho.")
        setCart(p, inv, cartSlot)
    }

    private fun removeItem(p: Player, click: ClickType, inv: Inventory, cartSlot: Int) {
        if (cart[p]!!.isEmpty() && undoCart[p]!!.isEmpty()) {
            send(p, "&cSeu carrinho já está vazio!")
            return
        }

        when (click) {
            ClickType.CONTROL_DROP, ClickType.DOUBLE_CLICK, ClickType.SHIFT_RIGHT -> {
                undoCart[p]!!.add(cart[p]!!)
                cart[p]!!.clear()
                send(p, "&eSeu carrinho foi esvaziado!")
            }

            ClickType.DROP, ClickType.RIGHT -> {
                if (undoCart[p]!!.isNotEmpty()) {
                    cart[p] = undoCart[p]!!.last()
                    undoCart[p]!!.removeLast()
                } else cart[p]!!.removeLast()
            }

            else -> MessageU.sendHoverCMD(
                p,
                "&8[&eVeja seu carrinho da loja aqui!&8]",
                getCartContents(p),
                "/shop",
                true
            )
        }

        setCart(p, inv, cartSlot)
    }

    private fun buyCartContents(p: Player, inv: Inventory, cartSlot: Int) {
        if (cart[p]!!.size == 1 || cart[p]!!.count {
                val item = it.clone()
                val cop = cart[p]!![0].clone()
                item.amount = 1
                cop.amount = 1
                item == cop
            } == cart[p]!!.size) {

            cart[p]!!.forEach {
                val item = it.clone()
                item.amount = 1
                val price = it.amount * shopGUI.getPrice(item).toDouble()

                if (econ.getBalance(p) >= price) {
                    if (econ.withdrawPlayer(p, price).transactionSuccess()) {
                        val meta = it.itemMeta

                        meta.lore.clear()
                        item.itemMeta = meta


                        addToBag(p, it)
                        inv.setItem(cartSlot, itemManager.getItem("cart"))
                        send(p, "&eVocê comprou ${it.amount}&e de ${it.itemMeta.displayName}&e por &2$&a " + formatMoney(price))

                    } else send(p, "&cNão foi possível realizar a compra de ${it.itemMeta.displayName}&c.")

                } else {
                    send(p, "&cSaldo insuficiente!")
                    cart[p]!!.clear()
                    return
                }
            }

        } else {
            send(p, "&eVocê comprou os seguintes itens:")
            val items = hashMapOf<String, ItemStack>()

            cart[p]!!.forEach {
                val item = it.clone()
                item.amount = 1
                val price = it.amount * shopGUI.getPrice(item).toDouble()

                if (econ.getBalance(p) >= price) {
                    if (items.containsKey(it.itemMeta.displayName))
                        items[it.itemMeta.displayName]!!.amount += it.amount

                    else items[it.itemMeta.displayName] = it.clone()

                    send(p, "&eVocê comprou ${it.amount}&e de ${it.itemMeta.displayName}&e po1r &2$&a " + formatMoney(price))

                } else {
                    send(p, "&cSaldo insuficiente!")
                    cart[p]!!.clear()
                    return
                }
            }

            items.values.forEach {
                val item = it.clone()
                item.amount = 1

                val price = it.amount * shopGUI.getPrice(item).toDouble()

                if (econ.getBalance(p) >= price) {
                    if (econ.withdrawPlayer(p, price).transactionSuccess()) {
                        val meta = it.itemMeta

                        meta.lore.clear()
                        item.itemMeta = meta

                        addToBag(p, it)
                        inv.setItem(cartSlot, itemManager.getItem("cart"))
                        send(p, "&e+ ${it.amount}&e de ${it.itemMeta.displayName}&e po2r &2$&a " + formatMoney(price))

                    } else send(p, "&cNão foi possível realizar a compra de ${it.itemMeta.displayName}&c.")

                } else {
                    send(p, "&cSaldo insuficiente!")
                    undoCart[p]!!.add(cart[p]!!)
                    cart[p]!!.clear()
                    return
                }
            }
        }
        setBag(p, bags[p]!!.toSet())

        if (cart[p]!!.isNotEmpty()) {
            undoCart[p]!!.add(cart[p]!!)
            cart[p]!!.clear()

        } else if (undoCart[p]!!.isNotEmpty()) {
            cart[p] = undoCart[p]!!.last()
            undoCart[p]!!.removeLast()

        } else send(p, "&cSeu carrinho está vazio!")

        setCart(p, inv, cartSlot)
    }

    private fun getCartContents(p: Player): Array<String> {
        return cart[p]!!.map {
            val item = it.clone()
            item.amount = 1
            "&f" + it.itemMeta.displayName + "&f: " + it.amount + " &b[&2$&a" + formatMoney(
                (it.amount * shopGUI.getPrice(
                    item
                )).toDouble()
            ) + "&b]" + if (cart[p]!!.last() != it) "\n" else ""
        }.toTypedArray()
    }

    fun createBag() {
        setFunction("bag") { p ->
            openBag(p)
        }

        setFunction("claimBag") { p ->
            claimBagItem(p, true)
            setBag(p, bags[p]!!.toSet())
        }
    }

    fun openBag(p: Player) {
        if (bags[p] == null)
            bags[p] = getBag(p)

        if (bags[p]!!.isNotEmpty()) {
            val bag = ChestPages(p, p.name + "mochila", "&e[&fMochila de &a&l${p.name}&e]", 28, bags[p]!!)
            bag.setItemPage(46, itemManager.getItem("claimBag"))

            p.openInventory(bag.page(1))

        } else send(p, "&eSua mochila está vazia!")

    }

    private fun addToBag(p: Player, item: ItemStack) {
        if (bags[p] == null)
            bags[p] = getBag(p)

        val ti = item.clone()
        val meta = ti.itemMeta

        if (bags[p]!!.any { it.itemMeta.displayName == item.itemMeta.displayName }) {

            val id = bags[p]!!.indices.find { bags[p]!![it].itemMeta.displayName == item.itemMeta.displayName && bags[p]!![it].type == item.type }!!

            val amount = item.amount + bags[p]!![id].itemMeta.lore[1].substring(2).toInt()
            meta.lore = listOf(c("&eQuantidade&f:"), c("&b$amount"))

            ti.setItemMeta(meta)
            ti.amount = 1

            bags[p]!![id] = ti

        } else {
            meta.lore = listOf(c("&eQuantidade&f:"), c("&b${item.amount}"))

            ti.setItemMeta(meta)
            ti.amount = 1

            bags[p]!!.add(ti)
        }

    }

    fun claimBagItem(p: Player, item: ItemStack, updateMenu: Boolean, shiftClick: Boolean): Boolean {
        if (!bags[p]!!.contains(item))
            return false

        claimBagItem(p, item, updateMenu)

        if (shiftClick && containsBag(p, item)) {
            var ti = findItemBag(p, item).clone()

            while (bags[p]!!.isNotEmpty() && p.inventory.contents.contains(null) && containsBag(p, item)) {
                claimBagItem(p, ti, false)
                if (containsBag(p, item))
                    ti = findItemBag(p, ti).clone()
            }

            openBag(p)
            if (bags[p]!!.isEmpty())
                getLastMenu(p)
        }

        return true
    }

    fun claimBagItem(p: Player, updateMenu: Boolean) {
        if (bags[p] == null)
            bags[p] = getBag(p)

        if (bags[p]!!.isNotEmpty()) {
            if (p.inventory.contents.contains(null)) {
                while (p.inventory.contents.contains(null) && bags[p]!!.isNotEmpty())
                    claimBagItem(p, bags[p]!!.first().clone(), false)

                if (bags[p]!!.isEmpty())
                    getLastMenu(p)

                if (updateMenu) {
                    openBag(p)

                    if (bags[p]!!.isNotEmpty())
                        send(p, "&eNem todos os itens da sua mochila foram resgatados.")
                    else {
                        getLastMenu(p)
                        send(p, "&aVocê resgatou todos os itens da sua mochila!")
                    }
                }

            } else send(p, "&eEsvazie seu inventário para resgatar os itens!")

        } else send(p, "&cSua mochila está vazia!")
    }

    private fun claimBagItem(p: Player, item: ItemStack, updateMenu: Boolean): Int {
        if (bags[p]!!.isNotEmpty()) {
                if (p.inventory.contents.contains(null) && bags[p]!!.isNotEmpty()) {

                    val ti = item.clone()
                    ti.setItemMeta(null)

                    val open = ItemStack(ti.type).maxStackSize
                    var qtt = item.itemMeta.lore[1].substring(2).toInt()

                    ti.amount = if (qtt > open) {
                        qtt -= open
                        val tt = ti.clone()
                        tt.amount = qtt

                        val meta = item.itemMeta
                        meta.lore = listOf(meta.lore[0], c("&b$qtt"))

                        findItemBag(p, item).setItemMeta(meta)

                        open

                    } else {
                        bags[p]!!.remove(item)
                        qtt
                    }

                    p.inventory.addItem(ti)

                    if (updateMenu) {
                        openBag(p)
                        if (bags[p]!!.isEmpty())
                            getLastMenu(p)
                    }

                    setBag(p, bags[p]!!.toSet())
            } else send(p, "&eEsvazie seu inventário para resgatar os itens!")

        } else send(p, "&cSua mochila está vazia!")
        return -1
    }

    fun isCart(item: ItemStack): Boolean {
        return item.itemMeta.displayName == itemManager.getItem("cart").itemMeta.displayName
    }

    fun setCart(p: Player, inv: Inventory, cartSlot: Int) {
        if (cart[p]!!.isEmpty()) {
            inv.setItem(cartSlot, itemManager.getItem("cart"))
            return
        }

        val lore = mutableListOf(c("&eConteúdo do carrinho:"))


        lore.addAll(cart[p]!!.map { c("${it.itemMeta.displayName}&f: &b${it.amount}") })

        val ct = inv.getItem(cartSlot)
        val meta = ct.itemMeta
        meta.lore = lore
        ct.setItemMeta(meta)
        inv.setItem(cartSlot, ct)

    }

    fun createCarts(p: Player) {
        cart[p] = mutableListOf()
        undoCart[p] = mutableListOf()
    }

    fun load() {
        Bukkit.getOnlinePlayers().forEach { createCarts(it); tutorial[it] = getTutorial(it) }
    }

    private fun containsCart(p: Player, item: ItemStack): Boolean {
        return cart[p]!!.any { it.itemMeta.displayName == item.itemMeta.displayName && it.type == item.type }
    }

    private fun findItemCart(p: Player, item: ItemStack): ItemStack {
        return cart[p]!!.find { it.itemMeta.displayName == item.itemMeta.displayName && it.type == item.type }!!
    }

    private fun containsBag(p: Player, item: ItemStack): Boolean {
        return bags[p]!!.any { it.itemMeta.displayName == item.itemMeta.displayName && it.type == item.type }
    }

    private fun findItemBag(p: Player, item: ItemStack): ItemStack {
        return bags[p]!!.find { it.itemMeta.displayName == item.itemMeta.displayName && it.type == item.type }!!
    }

    fun clearPlayerBag(p: Player, target: Player) {
        clearBag(target)
        send(p, "&eA mochila de &f${target.name}&e foi esvaziada com sucesso.")
    }
}
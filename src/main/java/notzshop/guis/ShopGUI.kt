package notzshop.guis

import notzshop.Main.Companion.itf
import notzshop.Main.Companion.shopGUI
import notzshop.managers.DatabaseM.confirmTutorial
import notzshop.managers.ShopM
import notzshop.managers.ShopM.createBag
import notzshop.managers.ShopM.isTutorial
import notzshop.notzapi.NotzAPI.Companion.itemManager
import notzshop.notzapi.apis.NotzGUI
import notzshop.notzapi.apis.NotzItems.buildItem
import notzshop.notzapi.apis.NotzItems.glass
import notzshop.notzapi.gui.ChestPages
import notzshop.notzapi.utils.EventU.setFunction
import notzshop.notzapi.utils.MenuU.addItemMenu
import notzshop.notzapi.utils.MenuU.openMenu
import notzshop.notzapi.utils.MenuU.resetLastMenu
import notzshop.notzapi.utils.MessageU.c
import notzshop.notzapi.utils.MessageU.formatMoney
import org.bukkit.Material.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Collections.emptyList

class ShopGUI {
    private val loja = "&6&l[&f&lLoja&6&l]"
    private val itemsToBuy = mutableListOf<ItemStack>()

    private var tutorial: NotzGUI
    private var tuto: NotzGUI
    private var shopMenu: NotzGUI
    private var geralMenu: ChestPages
    private var glassMenu: ChestPages

    private lateinit var geralMenuList: Set<ItemStack>
    private lateinit var woodMenuList: Set<ItemStack>
    private lateinit var clayMenuList: Set<ItemStack>
    private lateinit var glassMenuList: Set<ItemStack>
    private lateinit var woolMenuList: Set<ItemStack>
    private lateinit var potions2MenuList: Set<ItemStack>
    private lateinit var brewMenuList: Set<ItemStack>
    private lateinit var farmmaterialMenuList: Set<ItemStack>
    private lateinit var farm2MenuList: Set<ItemStack>
    private lateinit var dyesMenuList: Set<ItemStack>
    private lateinit var leavesMenuList: Set<ItemStack>
    private lateinit var foodMenuList: Set<ItemStack>
    private lateinit var toolsMenuList: Set<ItemStack>
    private lateinit var diverseMenuList: Set<ItemStack>
    private lateinit var cashMenuList: Set<ItemStack>
    private lateinit var geralList: Set<String>
    private lateinit var woodList: Set<String>
    private lateinit var clayList: Set<String>
    private lateinit var glassList: Set<String>
    private lateinit var woolList: Set<String>
    private lateinit var potions2List: Set<String>
    private lateinit var brewList: Set<String>
    private lateinit var farmmaterialList: Set<String>
    private lateinit var farm2List: Set<String>
    private lateinit var dyesList: Set<String>
    private lateinit var leavesList: Set<String>
    private lateinit var foodList: Set<String>
    private lateinit var toolsList: Set<String>
    private lateinit var diverseList: Set<String>
    private lateinit var cashList: Set<String>

    private val prices = hashMapOf<String, Int>()
    private val pricesIS = hashMapOf<ItemStack, Int>()

    // INIT - START

    init {
        itemManager.addItems(hashMapOf(
            "shopcarton" to buildItem(MINECART, "&b&lCarrinho de compras", emptyList(), true),
            "shopcartoff" to buildItem(STORAGE_MINECART, "&b&lCarrinho de compras", listOf("&f&oAtualmente o seu", "&f&ocarrinho está vazio."), false),
            "buy" to buildItem(DIAMOND, "&e&lFinalizar compra", emptyList(), false),

            "blocksMenu" to buildItem(168, 2, "&e&lBlocos", listOf("&7&oClique para acessar", "&7&oo menu de blocos."), false),
            "potionsMenu" to buildItem(373, "&3&lPoções", listOf("&7&oClique para acessar", "&7&oo menu de poções."), true),
            "farmMenu" to buildItem(CACTUS, "&2&lFarm", listOf("&7&oClique para acessar", "&7&oo menu de farm."), false),
            "decoMenu" to buildItem(161, 1, "&a&lDecorações", listOf("&7&oClique para acessar", "&7&oo menu de decorações."), false),
            "othersMenu" to buildItem(DIAMOND_PICKAXE, "&7&lOutros", listOf("&7&oClique para acessar", "&7&oo menu de variedades."), false),
            "cashMenu" to buildItem(BEACON, "&7&oCash &6&l[Em breve]", listOf("&f&oClique para acessar", "&f&oo menu de itens especiais."), false),

            "geralMenu" to buildItem(168, 2, "&3&lDiversos", listOf("&7Categoria:", "&3Blocos&7 Diversos."), false),
            "woodMenu" to buildItem(5, 1, "&e&lMadeiras", listOf("&7Categoria:", "&eMadeiras&7."), false),
            "clayMenu" to buildItem(159, 6, "&d&lArgilas", listOf("&7Categoria:", "&dArgilas&7."), false),
            "glassMenu" to buildItem(95, 10, "&5&lVidros", listOf("&7Categoria:", "&5Vidros&7."), false),
            "woolMenu" to buildItem(35, 11, "&9&lLãs", listOf("&7Categoria:", "&9Lãs&7."), false),

            "potions2Menu" to buildItem(373, "&3&lPoções", listOf("&7Categoria:", "&3Poções&7."), false),
            "brewMenu" to buildItem(SPECKLED_MELON, "&e&lIngredientes", listOf("&7Categoria:", "&eIngredientes&7."), false),

            "farmmaterialMenu" to buildItem(12, 1, "&e&lBlocos para farm", listOf("&7Categoria:", "&eBlocos para farm&7."), false),
            "farm2Menu" to buildItem(CACTUS, "&2&lPlantações", listOf("&7Categoria:", "&2Plantações&7."), false),

            "dyesMenu" to buildItem(351, 14, "&3&lCorantes", listOf("&7Categoria:", "&3Corantes&7."), false),
            "leavesMenu" to buildItem(18, 3, "&2&lFolhas", listOf("&7Categoria:", "&2Folhas&7."), false),

            "foodMenu" to buildItem(357, "&e&lComidas", listOf("&7Categoria:", "&eComidas&7."), false),
            "toolsMenu" to buildItem(276, "&b&lFerramentas", listOf("&7Categoria:", "&bFerramentas&7."), false),
            "diverseMenu" to buildItem(ITEM_FRAME, "&6&lDiversos", listOf("&7Categoria:", "&6Diversos&7."), false),

            "tutorial1" to itemManager.buildItemFromFile("tutorial1"),
            "tutorial2" to itemManager.buildItemFromFile("tutorial2"),
            "tutorial3" to itemManager.buildItemFromFile("tutorial3"),

            "bag" to itemManager.buildItemFromFile("bag"),
            "buy" to itemManager.buildItemFromFile("buy"),
            "cart" to itemManager.buildItemFromFile("cart"),
            "info" to itemManager.buildItemFromFile("info"),
            "claimBag" to itemManager.buildItemFromFile("claimBag")
        ))

        createBag()
        buildMenuItems()

        tutorial = NotzGUI(null, 4, "shopMenu", "$loja &e&lComo comprar")
        tutorial.setPanel(0, false)
        setItems(tutorial, hashMapOf(
            4 to "preopenshop",
            11 to "tutorial1",
            13 to "tutorial2",
            15 to "tutorial3",
            29 to "info",
            34 to "cart"
        ))
        tutorial.setItem(20, glass(2))
        tutorial.setItem(28, glass(2))
        tutorial.setItem(30, glass(2))
        tutorial.setItem(25, glass(2))
        tutorial.setItem(33, glass(2))
        tutorial.setItem(35, glass(2))

        shopMenu = NotzGUI(null, 4, "shopMenu", "$loja &e&lMenu")
        shopMenu.setPanel(0, false)
        shopMenu.setPanel(11, true)
        setItems(shopMenu, hashMapOf(
            10 to "blocksMenu",
            12 to "potionsMenu",
            14 to "farmMenu",
            16 to "decoMenu",
            20 to "othersMenu",
            24 to "cashMenu"
        ))

        tuto = tutorial.clone()
        tuto.setItem(4, itemManager.getItem("openshop"))

        setFunction("info") { p -> if (!p.inventory.title.contains("Como comprar")) openMenu(p, tuto)}
        setFunction("openshop") { p -> open(p) }

        buildSubMenu("blocksMenu", "&e&lBlocos", 3, 4, hashMapOf(
            10 to "geralMenu",
            11 to "woodMenu",
            13 to "clayMenu",
            15 to "glassMenu",
            16 to "woolMenu"
        ))

        buildSubMenu("potionsMenu", "&3&lPoções", 3, 4, hashMapOf(
            11 to "potions2Menu",
            15 to "brewMenu"
        ))

        buildSubMenu("farmMenu", "&2&lFarm", 3, 5, hashMapOf(
            11 to "farmmaterialMenu",
            15 to "farm2Menu"
        ))

        buildSubMenu("decoMenu", "&a&lDecorações", 3, 1, hashMapOf(
            10 to "dyesMenu",
            11 to "clayMenu",
            13 to "glassMenu",
            15 to "woolMenu",
            16 to "leavesMenu"
        ))

        buildSubMenu("othersMenu", "&7&lOutros", 3, 7, hashMapOf(
            11 to "foodMenu",
            13 to "toolsMenu",
            15 to "diverseMenu"
        ))

        buildMenu("cashMenu", "&6&lCash", 6, 0, hashMapOf(), setOf())
        buildMenu("woodMenu", "&e&lMadeiras", 4, 12, hashMapOf(), woodMenuList)
        buildMenu("clayMenu", "&d&lArgilas", 5, 6, hashMapOf(), clayMenuList)
        buildMenu("woolMenu", "&9&lLãs", 5, 11, hashMapOf(), woolMenuList)
        buildMenu("potions2Menu", "&3&lPoções", 3, 9, hashMapOf(), potions2MenuList)
        buildMenu("brewMenu", "&3&lIngredientes", 4, 4, hashMapOf(), brewMenuList)
        buildMenu("farmmaterialMenu", "&e&lBlocos para farm", 3, 1, hashMapOf(), farmmaterialMenuList)
        buildMenu("farm2Menu", "&2&lPlantações", 4, 13, hashMapOf(), farm2MenuList)
        buildMenu("dyesMenu", "&3&lCorantes", 5, 1, hashMapOf(), dyesMenuList)
        buildMenu("leavesMenu", "&2&lFolhas", 3, 13, hashMapOf(), leavesMenuList)
        buildMenu("foodMenu", "&e&lComidas", 5, 14, hashMapOf(), foodMenuList)
        buildMenu("toolsMenu", "&b&lFerramentas", 4, 3, hashMapOf(), toolsMenuList)
        buildMenu("diverseMenu", "&6&lDiversos", 6, 15, hashMapOf(), diverseMenuList)


        geralMenu = ChestPages(null, "geralMenu", "$loja &e&lBlocos diversos", 28, geralMenuList.toList())
        geralMenu.setItemPage(46, "bag")
        geralMenu.setItemPage(47, "info")
        geralMenu.setItemPage(51, "buy")
        geralMenu.setItemPage(52, "cart")
        addItemMenu(itemManager.getItem("geralMenu"), geralMenu.pageRaw(1))

        glassMenu = ChestPages(null, "glassMenu", "$loja &5&lVidros", 28, glassMenuList.toList())
        glassMenu.setItemPage(46, "bag")
        glassMenu.setItemPage(47, "info")
        glassMenu.setItemPage(51, "buy")
        glassMenu.setItemPage(52, "cart")
        addItemMenu(itemManager.getItem("glassMenu"), glassMenu.pageRaw(1))
    }

    // INIT - END

    fun open(p: Player) {
        resetLastMenu(p)

        if (isTutorial(p)) {
            openTutorial(p)
        } else openMenu(p, shopMenu)
    }

    fun openTutorial(p: Player) {
        resetLastMenu(p)

        if (isTutorial(p)) {
            openMenu(p, tutorial)

            ShopM.tutorial[p] = true
            confirmTutorial(p)

        } else {
            openMenu(p, tuto)
        }
    }

    private fun buildSubMenu(menuName: String, title: String, rows: Int, frame: Int, items: HashMap<Int, String>) {
        val menu = NotzGUI(null, rows, menuName, "$loja&r $title")
        val slots = rows*9-1

        menu.setPanel(0, false)
        if (frame > 0)
            menu.setPanel(frame, true)
        menu.setup()

        items.putAll(hashMapOf(
            slots-7 to "bag",
            slots-1 to "cart"
        ))

        if (items.isNotEmpty())
            setItems(menu, items)

        addItemMenu(itemManager.getItem(menuName), menu)
    }

    private fun buildMenu(menuName: String, title: String, rows: Int, frame: Int, setItems: HashMap<Int, String>, addItems: Set<ItemStack>) {
        val menu = NotzGUI(null, rows, menuName, "$loja&r $title")
        val slots = rows*9-1

        menu.setPanel(frame, true)
        menu.setup()

        setItems.putAll(hashMapOf(
            slots-7 to "bag",
            slots-6 to "info",
            slots-2 to "buy",
            slots-1 to "cart"
        ))

        if (setItems.isNotEmpty())
            setItems(menu, setItems)
        menu.addItems(addItems)

        itemsToBuy.addAll(addItems)
        addItemMenu(itemManager.getItem(menuName), menu)
    }

// ------------------------------

    fun setPrice(item: String, price: Int) {
        prices[item] = price
        buildItemShop(item)

        itf.config!!.set("price.$item", price)
        itf.saveConfig()

        shopGUI = ShopGUI()
    }


    private fun setItems(menu: NotzGUI, items: HashMap<Int, String>) {
        items.forEach { menu.setItem(it.key, it.value)}
    }

    fun getPrice(item: ItemStack): Int {
        return pricesIS[item]!!
    }

    fun getPrice(item: String): Int {
        return prices[item]!!
    }

    private fun buildItemShop(name: String): ItemStack {
        val item = itemManager.buildItemFromFile(name)
        val meta = item.itemMeta
        val price = itf.config!!.getInt("price.${meta.lore[0]}")

        meta.lore = listOf(
            c("&fClique para adicionar este"),
            c("&fitem no seu carrinho."),
            "",
            c("&bUnidade&f: &2$&a${formatMoney(price.toDouble())}"),
            c("&bPack&f: &2$&a${formatMoney(price.toDouble() * 64)}"),
            c("&bHotbar&f: &2$&a${formatMoney(price.toDouble() * 64*9)}"),
            c("&bInventário&f: &2$&a${formatMoney(price.toDouble() * 64*9*4)}")
        )

        item.setItemMeta(meta)
        itemManager.addItem(name, item)

        pricesIS[item] = price

        return item
    }

    private fun buildItemsShop(items: Set<String>): Set<ItemStack> {
        return items.toList().map {
            buildItemShop(it)
        }.toSet()
    }

    private fun buildMenuItems() {
        geralList = "stone granite polished_granite diorite polished_diorite andesite polished_andesite grass dirt coarse_dirt podzol cobblestone gravel sandstone sandstone_chiseled sandstone_smooth brick_block mossy_cobblestone clay netherrack lit_pumpkin brown_mushroom_block red_mushroom_block mycelium nether_brick end_stone quartz_block chiseled_quartz_block pillar_quartz_block prismarine prismarine_bricks dark_prismarine red_sandstone red_sandstone_chiseled".split(" ").toSet()
        woodList = "wooden_plank_oak wooden_plank_spruce wooden_plank_birch wooden_plank_jungle wooden_plank_acacia wooden_plank_dark_oak wood_oak wood_spruce wood_birch wood_jungle wood_acacia_oak wood_dark_oak".split(" ").toSet()
        clayList = "stained_clay_white stained_clay_orange stained_clay_magenta stained_clay_light_blue stained_clay_yellow stained_clay_lime stained_clay_pink stained_clay_gray stained_clay_light_gray stained_clay_cyan stained_clay_purple stained_clay_blue stained_clay_bown stained_clay_green stained_clay_red stained_clay_black hardened_clay".split(" ").toSet()
        glassList = "glass stained_glass_white stained_glass_orange stained_glass_magenta stained_glass_light_blue stained_glass_yellow stained_glass_lime stained_glass_pink stained_glass_gray stained_glass_light_grey stained_glass_cyan stained_glass_purple stained_glass_blue stained_glass_brown stained_glass_green stained_glass_red stained_glass_black glass_pane stained_glass_pane_white stained_glass_pane_orange stained_glass_pane_magenta stained_glass_pane_light_blue stained_glass_pane_yellow stained_glass_pane_lime stained_glass_pane_pink stained_glass_pane_gray stained_glass_pane_light_gray stained_glass_pane_cyan stained_glass_pane_purple stained_glass_pane_blue stained_glass_pane_brown stained_glass_pane_green stained_glass_pane_red stained_glass_pane_black".split(" ").toSet()
        woolList = "wool orange_wool magenta_wool light_blue_wool yellow_wool lime_wool pink_wool gray_wool light_gray_wool cyan_wool purple_wool blue_wool brown_wool green_wool red_wool black_wool".split(" ").toSet()
        potions2List = "swiftness_potion fire_resistance_potion healing_potion strength_potion invisibility_potion".split(" ").toSet()
        brewList = "gunpowder redstone glowstone_dust sugar potion spider_eye fermented_spider_eye blaze_powder magma_cream brewing_stand speckled_melon golden_carrot".split(" ").toSet()
        farmmaterialList = "sand red_sand soul_sand string diamond_hoe water_bucket sea_lantern".split(" ").toSet()
        farm2List = "cactus wheat_seeds sugar_cane pumpkin_seeds melon_seeds nether_wart carrot potato".split(" ").toSet()
        dyesList = "bone_meal rose_red_dye cactus_green_dye cocoa_bean lapis_lazuli purple_dye cyan_dye light_gray_dye gray_dye pink_dye lime_dye dandelion_yellow_dye light_blue_dye magenta_dye orange_dye ink_sack".split(" ").toSet()
        leavesList = "leaves_oak leaves_spruce leaves_birch leaves_jungle leaves_acacia leaves_dark_oak".split(" ").toSet()
        foodList = "apple mushroom_stew bread cooked_porkchop golden_apple enchanted_golden_apple cooked_fished cooked_fished_salmon cake cookie cooked_beef cooked_chicken pumpkin_pie cooked_rabbit rabbit_stew cooked_mutton".split(" ").toSet()
        toolsList = "bow arrow diamond_sword diamond_shovel diamond_pickaxe diamond_axe compass fishing_rod clock shears".split(" ").toSet()
        diverseList = "noteblock ender_chest bookshelf chest ice jukebox glowstone iron_bars redstone_lamp trapped_chest hopper slime sea_lantern packed_ice painting book writable_book item_frame flower_pot map cauldron banner".split(" ").toSet()
        cashList = "beacon experience_bottle".split(" ").toSet()

        getTotalItems().forEach { prices[it] = itf.config!!.getInt("price.$it")}

        geralMenuList = buildItemsShop(geralList)
        woodMenuList = buildItemsShop(woodList)
        clayMenuList = buildItemsShop(clayList)
        glassMenuList = buildItemsShop(glassList)
        woolMenuList = buildItemsShop(woolList)
        potions2MenuList = buildItemsShop(potions2List)
        brewMenuList = buildItemsShop(brewList)
        farmmaterialMenuList = buildItemsShop(farmmaterialList)
        farm2MenuList = buildItemsShop(farm2List)
        dyesMenuList = buildItemsShop(dyesList)
        leavesMenuList = buildItemsShop(leavesList)
        foodMenuList = buildItemsShop(foodList)
        toolsMenuList = buildItemsShop(toolsList)
        diverseMenuList = buildItemsShop(diverseList)
        cashMenuList = buildItemsShop(cashList)

        itemsToBuy.addAll(setOf(geralMenuList,
            woodMenuList,
            clayMenuList,
            glassMenuList,
            woolMenuList,
            potions2MenuList,
            brewMenuList,
            farmmaterialMenuList,
            farm2MenuList,
            dyesMenuList,
            leavesMenuList,
            foodMenuList,
            toolsMenuList,
            diverseMenuList,
            cashMenuList).flatten())

    }


    fun getTotalItems(): Set<String> {
        return setOf(geralList, woodList, clayList, glassList, woolList, potions2List, brewList, farmmaterialList, farm2List, dyesList, leavesList, foodList, toolsList, diverseList, cashList).flatten().toSet()
    }
}
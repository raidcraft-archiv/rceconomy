package de.raidcraft.rceconomy.shopsign.ui;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.chestui.ChestUI;
import de.raidcraft.api.chestui.Menu;
import de.raidcraft.api.chestui.menuitems.MenuItem;
import de.raidcraft.api.chestui.menuitems.MenuItemAPI;
import de.raidcraft.api.chestui.menuitems.MenuItemAllowedPlacing;
import de.raidcraft.rceconomy.MenuUtil;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.shopsign.ShopSign;
import de.raidcraft.util.ItemUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Philip on 12.01.2016.
 */
public class SellMenu {

    private RCEconomyPlugin plugin;
    private static SellMenu instance;

    public static SellMenu get() {

        if(instance == null) {
            instance = new SellMenu(RaidCraft.getComponent(RCEconomyPlugin.class));
        }

        return instance;
    }

    public SellMenu(RCEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    private void addPriceButton(Menu menu, ShopSign shopSign, EditedSign changeValues, String text, double priceChange) {

        ItemStack itemStack;
        itemStack = new ItemStack(new ItemStack(Material.INK_SACK, 1, DyeColor.MAGENTA.getData()));
        ItemUtils.setDisplayName(itemStack, text);
        menu.addMenuItem(new MenuItemAPI() {
            @Override
            public void trigger(Player player) {
                changeValues.setPrice(changeValues.getPrice() + priceChange);
                open(player, shopSign, changeValues);
            }
        }.setItem(itemStack));
    }

    public boolean open(Player player, ShopSign shopSign, EditedSign changeValues) {

        Menu menu = new Menu("Handelsschild: Verkauf");
        menu.setPlacingItemsMustSameType(true);
        ItemStack itemStack;

        int gold = (int)(shopSign.getPrice() / 100D);
        int silver = (int)(shopSign.getPrice() - (int)(gold*100D));
        int bronze = (int)(shopSign.getPrice() * 100D) % 100;
        String formattedBalance = RaidCraft.getEconomy().getFormattedAmount(shopSign.getPrice());

        if(gold > 64) gold = 1;
        if(silver > 64) silver = 1;
        if(bronze > 64) bronze = 1;

        MenuUtil.addPlaceholder(menu, 4);

        // Helping book
        itemStack = new ItemStack(Material.BOOK);
        ItemUtils.setDisplayName(itemStack, "Hilfe:");
        ItemUtils.setLore(itemStack,
                MenuUtil.splitSentence("Hier kannst du einstellen welches Item für welchen Preis verkauft werden soll. " +
                        "Ziehe die Items welche du verkaufen möchtest einfach in die freien Inventarslots und stelle rechts " +
                        "den Preis pro Item ein."));
        menu.addMenuItem(new MenuItem().setItem(itemStack));

        MenuUtil.addPlaceholder(menu, 4);

        addPriceButton(menu, shopSign, changeValues, "+1 Gold", 100D);
        addPriceButton(menu, shopSign, changeValues, "+1 Silber", 1D);
        addPriceButton(menu, shopSign, changeValues, "+1 Bronze", 0.1D);

        MenuUtil.addPlaceholder(menu, 2);

        menu.placingSlot();
        menu.placingSlot();
        menu.placingSlot();
        menu.placingSlot();

        itemStack = new ItemStack(new ItemStack(Material.GOLD_INGOT, gold));
        ItemUtils.setDisplayName(itemStack, "Aktueller Verkaufspreis pro Item");
        ItemUtils.setLore(itemStack, formattedBalance);
        menu.addMenuItem(new MenuItem().setItem(itemStack));

        itemStack = new ItemStack(new ItemStack(Material.IRON_INGOT, silver));
        ItemUtils.setDisplayName(itemStack, "Aktueller Verkaufspreis pro Item");
        ItemUtils.setLore(itemStack, formattedBalance);
        menu.addMenuItem(new MenuItem().setItem(itemStack));

        itemStack = new ItemStack(new ItemStack(Material.CLAY_BRICK, bronze));
        ItemUtils.setDisplayName(itemStack, "Aktueller Verkaufspreis pro Item");
        ItemUtils.setLore(itemStack, formattedBalance);
        menu.addMenuItem(new MenuItem().setItem(itemStack));

        MenuUtil.addPlaceholder(menu, 2);

        menu.placingSlot();
        menu.placingSlot();
        menu.placingSlot();
        menu.placingSlot();

        addPriceButton(menu, shopSign, changeValues, "-1 Gold", -100D);
        addPriceButton(menu, shopSign, changeValues, "-1 Silber", -1D);
        addPriceButton(menu, shopSign, changeValues, "-1 Bronze", -0.1D);

        MenuUtil.addPlaceholder(menu, 2);

        menu.placingSlot();
        menu.placingSlot();
        menu.placingSlot();
        menu.placingSlot();

        MenuUtil.addPlaceholder(menu, 7);

        // Back
        itemStack = new ItemStack(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()));
        ItemUtils.setDisplayName(itemStack, "Schließen");
        menu.addMenuItem(new MenuItemAPI() {
            @Override
            public void trigger(Player player) {
                menu.close();
            }
        }.setItem(itemStack));

        // Apply
        itemStack = new ItemStack(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()));
        ItemUtils.setDisplayName(itemStack, "Speichern");
        menu.addMenuItem(new MenuItemAPI() {
            @Override
            public void trigger(Player player) {

                // Count items
                shopSign.setMaterial(menu.getPlacedItemUniqueType());
                shopSign.setMaxNumber(menu.getPlacedItemsAmount());

                shopSign.updateValues(changeValues);
                shopSign.updateSign();
                menu.close();
            }
        }.setItem(itemStack));

        // Add existing items
        int itemsLeft = changeValues.getItemNumber();
        while(itemsLeft > 0) {
            itemStack = new ItemStack(changeValues.getMaterial(), Math.min(64, itemsLeft));
            menu.addItemInPlacingSlot(itemStack);
            itemsLeft -= itemStack.getAmount();
        }

        // Open menu
        ChestUI.getInstance().openMenu(player, menu);

        return true;
    }
}

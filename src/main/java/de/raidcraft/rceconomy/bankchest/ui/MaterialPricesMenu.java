package de.raidcraft.rceconomy.bankchest.ui;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.chestui.ChestUI;
import de.raidcraft.api.chestui.Menu;
import de.raidcraft.api.chestui.menuitems.MenuItem;
import de.raidcraft.api.chestui.menuitems.MenuItemAPI;
import de.raidcraft.rceconomy.MenuUtil;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.bankchest.BankMaterialManager;
import de.raidcraft.rceconomy.tables.TBankMaterial;
import de.raidcraft.util.ItemUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import java.util.List;

/**
 * Created by Philip on 04.01.2016.
 */
public class MaterialPricesMenu {

    private RCEconomyPlugin plugin;
    private static MaterialPricesMenu instance;

    public static MaterialPricesMenu get() {

        if(instance == null) {
            instance = new MaterialPricesMenu(RaidCraft.getComponent(RCEconomyPlugin.class));
        }

        return instance;
    }

    public MaterialPricesMenu(RCEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean open(Player player, int page) {

        final int totalPages;
        List<TBankMaterial> bankMaterials = BankMaterialManager.get().getAllMaterials();
        if(bankMaterials != null && !bankMaterials.isEmpty()) {
            totalPages = (int)Math.ceil((double)bankMaterials.size() / (double) MenuUtil.MAX_SLOTS_PER_MENU);
        } else {
            totalPages = 1;
        }

        Menu menu = new Menu("Börsen Items (" + page + "/" + totalPages + ")");
        ItemStack itemStack;

        MenuUtil.addPlaceholder(menu, 4);

        // Helping book
        itemStack = new ItemStack(Material.BOOK);
        ItemUtils.setDisplayName(itemStack, "Hilfe:");
        ItemUtils.setLore(itemStack,
                MenuUtil.splitSentence("Hier siehst du alle Items die von der Raid-Craft Bank an- und verkauft werden. Items können in der Bank an Schilder oder in Bankkisten verkauft werden."));
        menu.addMenuItem(new MenuItem().setItem(itemStack));

        MenuUtil.addPlaceholder(menu, 4);

        int bankMaterialCount = 0;
        if(bankMaterials == null || bankMaterials.size() == 0) {
            itemStack = new ItemStack(Material.BARRIER);
            ItemUtils.setDisplayName(itemStack, "Keine Items verfügbar!");
            menu.addMenuItem(new MenuItem().setItem(itemStack));
            bankMaterialCount = 1;
        } else {
            int i = 0;
            for(TBankMaterial bankMaterial : bankMaterials) {
                i++;
                int currentPage = (int)Math.ceil((double)i / (double)MenuUtil.MAX_SLOTS_PER_MENU);
                if(currentPage != page && page <= totalPages) continue;
                bankMaterialCount++;
                MenuUtil.addMaterialItem(menu, bankMaterial, new MenuItem());
            }
        }

        for(int i = 0; i < MenuUtil.MAX_SLOTS_PER_MENU - bankMaterialCount; i++) {
            menu.empty();
        }

        MenuUtil.addPlaceholder(menu, 7);

        // Back
        itemStack = new Wool(DyeColor.RED).toItemStack();
//        itemStack = new ItemStack(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()));
        ItemUtils.setDisplayName(itemStack, "Schließen");
        menu.addMenuItem(new MenuItemAPI() {
            @Override
            public void trigger(Player player) {
                menu.close();
            }
        }.setItem(itemStack));

        // Next Page
        itemStack = new ItemStack(new ItemStack(Material.MINECART));
        ItemUtils.setDisplayName(itemStack, "Nächste Seite");
        menu.addMenuItem(new MenuItemAPI() {
            @Override
            public void trigger(Player player) {
                if(totalPages == 1) {
                    return;
                } else if(page == totalPages) {
                    MaterialPricesMenu.get().open(player, 1);
                } else {
                    MaterialPricesMenu.get().open(player, page + 1);
                }
            }
        }.setItem(itemStack));

        // Open menu
        ChestUI.getInstance().openMenu(player, menu);

        return true;
    }
}

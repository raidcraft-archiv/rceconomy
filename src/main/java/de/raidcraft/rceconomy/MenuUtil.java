package de.raidcraft.rceconomy;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.chestui.Menu;
import de.raidcraft.api.chestui.menuitems.MenuItem;
import de.raidcraft.api.chestui.menuitems.MenuItemAPI;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rceconomy.tables.TBankMaterial;
import de.raidcraft.util.ItemUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Philip on 04.01.2016.
 */
public class MenuUtil {

    private static Map<String, ItemStack> skullCache = new HashMap<>();
    public static int MAX_SLOTS_PER_MENU = 36;

    public static void addPlaceholder(Menu menu, int number) {

        ItemStack itemStack;

        for(int i = 0; i < number; i++) {
            itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemUtils.setDisplayName(itemStack, "~");
            menu.addMenuItem(new MenuItem().setItem(itemStack));
        }
    }

    private static int WORDS_PER_LINE = 5;
    public static String[] splitSentence(String sentence) {

        String[] words = sentence.split("\\s+");
        String[] lines = new String[(int)Math.ceil(((double)words.length / (double)WORDS_PER_LINE))];

        int lineCount = 0;
        int wordCount = 0;
        for(String word : words) {
            wordCount++;

            if(lines[lineCount] == null) {
                lines[lineCount] = "";
            }
            if(lines[lineCount] != null && !lines[lineCount].isEmpty()) {
                lines[lineCount] += " ";
            }
            lines[lineCount] += word;

            if(wordCount == WORDS_PER_LINE) {
                wordCount = 0;
                lineCount++;
            }
        }
        return lines;
    }

    public static void addMaterialItem(Menu menu, TBankMaterial bankMaterial, MenuItemAPI menuItem) {

        Economy economy = RaidCraft.getEconomy();
        Material material = ItemUtils.getItem(bankMaterial.getMaterial());
        if(material == null) {
            addPlaceholder(menu, 1);
            return;
        }
        ItemStack itemStack = new ItemStack(material);
        ItemUtils.setDisplayName(itemStack, ItemUtils.getFriendlyName(material));

        ChatColor buy = ChatColor.GOLD;
        if(!bankMaterial.isBuy()) {
            buy = ChatColor.STRIKETHROUGH;
        }

        ChatColor sell = ChatColor.GOLD;
        if(!bankMaterial.isSell()) {
            sell = ChatColor.STRIKETHROUGH;
        }

        ItemUtils.setLore(itemStack,
                ChatColor.GOLD.toString() + buy + "Ankaufspreis: " + ChatColor.RESET + economy.getFormattedAmount(bankMaterial.getPriceBuy()),
                ChatColor.GOLD.toString() + sell + "Verkaufspreis: " + ChatColor.RESET + economy.getFormattedAmount(bankMaterial.getPriceSell()));
        menu.addMenuItem(menuItem.setItem(itemStack));
    }
}

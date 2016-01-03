package de.raidcraft.rceconomy.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.util.InventoryUtils;
import de.raidcraft.util.ItemUtils;
import de.raidcraft.util.SignUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Philip on 03.01.2016.
 */
public class BankSign implements Listener {

    private static String BANK_SIGN_TAG = "BANK-ANKAUF";

    private String[] formatSign(Material material, double amount) {
        String[] lines = new String[4];

        lines[0] = ChatColor.YELLOW + "[" + ChatColor.GREEN + BANK_SIGN_TAG + ChatColor.YELLOW + "]";
        lines[3] = RaidCraft.getComponent(RCEconomyPlugin.class).getApi().getFormattedAmount(amount);

        // Material colors
        switch(material) {
            case DIAMOND:
            case DIAMOND_BLOCK:
                lines[2] = ChatColor.AQUA + material.name();
                break;
            case GOLD_NUGGET:
            case GOLD_INGOT:
            case GOLD_BLOCK:
                lines[2] = ChatColor.GOLD + material.name();
                break;
            default:
                lines[2] = ChatColor.DARK_AQUA + material.name();
        }

        return lines;
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {

        // Check sign tag
        if(!SignUtil.strip(event.getLine(0)).equals("[" + BANK_SIGN_TAG + "]"))
        {
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission("banksign.create")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Bankschilder aufstellen!");
            return;
        }

        // Get item
        Material material = null;
        material = ItemUtils.getItem(event.getLine(2));
        if(material == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "In der vorletzten Zeile muss der Materialname/ID angegeben sein!");
            return;
        }

        // Get amount
        double amount = 0;
        try {
            amount = Double.parseDouble(event.getLine(3));
        } catch(Exception e) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "In der letzten Zeile muss der Ankaufspreis in Kupfer angegeben sein!");
            return;
        }

        // Format lines
        String[] formattedLines = formatSign(material, amount);
        for(int i = 0; i < 4; i ++) {
            event.setLine(i, formattedLines[i]);
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {

        if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        // Check if sign
        Sign sign = SignUtil.getSign(event.getClickedBlock());
        if(sign == null)
        {
            return;
        }

        // Check sign tag
        if(!SignUtil.strip(sign.getLine(0)).equals("[" + BANK_SIGN_TAG + "]"))
        {
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission("banksign.use")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Bankschilder benutzen!");
            return;
        }

        // Get item
        Material material = null;
        material = ItemUtils.getItem(sign.getLine(2));
        if(material == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Dieses Bankschild ist defekt (unbekanntes Material)!");
            return;
        }

        // Get amount
        double amount = 0;
        amount = RaidCraft.getComponent(RCEconomyPlugin.class).getApi().parseCurrencyInput(sign.getLine(3));
        if(amount == 0) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Dieses Bankschild ist defekt (falscher Geldbetrag)!");
            return;
        }

        // Format lines
        String[] formattedLines = formatSign(material, amount);
        for(int i = 0; i < 4; i ++) {
            sign.setLine(i, formattedLines[i]);
        }
        sign.update();

        // Check if player has items in inventory (and how much)
        int itemInventoryCount = 0;
        for(ItemStack itemStack : event.getPlayer().getInventory().all(material).values())
        {
            itemInventoryCount += itemStack.getAmount();
        }
        if(itemInventoryCount == 0) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du hast keine Items zu verkaufen!");
            return;
        }

        // Subtract one items
        int itemExchangeNum = 1;
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            itemExchangeNum = itemInventoryCount;
        }
        event.getPlayer().getInventory().removeItem(new ItemStack[] { new ItemStack(material, itemExchangeNum)});
        event.getPlayer().updateInventory();

        // Give money
        RaidCraft.getComponent(RCEconomyPlugin.class).getApi().add(event.getPlayer().getUniqueId(),
                amount * itemExchangeNum, BalanceSource.TRADE,
                itemExchangeNum + " " + ItemUtils.getFriendlyName(material) + " an die Bank verkauft");
    }
}

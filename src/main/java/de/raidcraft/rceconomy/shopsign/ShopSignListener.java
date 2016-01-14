package de.raidcraft.rceconomy.shopsign;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.util.ItemUtils;
import de.raidcraft.util.SignUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Philip on 12.01.2016.
 */
public class ShopSignListener implements Listener {

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {

        ShopSign shopSign = new ShopSign(event.getLines());
        if(!shopSign.isValid()) {
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission("shopsign.create")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Shopschilder aufstellen!");
            return;
        }

        if(!event.getLine(1).isEmpty() || !event.getLine(2).isEmpty() || !event.getLine(3).isEmpty()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Die unteren drei Zeilen müssen unbeschrieben sein!");
            return;
        }

        shopSign.setPlayerId(event.getPlayer().getUniqueId());

        // Format lines
        String[] formattedLines = shopSign.getFormattedLines();
        for(int i = 0; i < 4; i ++) {
            event.setLine(i, formattedLines[i]);
        }

        event.getPlayer().sendMessage(
                ChatColor.GREEN + "Das Handelsschild wurde erfolgreich erstellt!");
    }

    @EventHandler
     public void onSignClick(PlayerInteractEvent event) {

        if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_AIR) ||
                event.getPlayer().isSneaking()) {
            return;
        }

        // Check if sign
        if(!SignUtil.isSign(event.getClickedBlock())) {
            return;
        }

        Sign sign = SignUtil.getSign(event.getClickedBlock());
        if(sign == null)
        {
            return;
        }

        ShopSign shopSign = new ShopSign(sign);
        if(!shopSign.isValid()) {
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission("shopsign.use")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Handelsschilder benutzen!");
            return;
        }

        shopSign.interact(event.getPlayer(), event.getAction());
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {

        // Check if sign
        if(!SignUtil.isSign(event.getBlock())) {
            return;
        }

        Sign sign = SignUtil.getSign(event.getBlock());
        if(sign == null)
        {
            return;
        }

        ShopSign shopSign = new ShopSign(sign);
        if(!shopSign.isValid()) {
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission("shopsign.create")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Handelsschilder abreissen!");
            return;
        }

        if(shopSign.getType() == ShopSign.ShopSignType.SELL) {
            int itemsLeft = shopSign.getMaxNumber();
            while (itemsLeft > 0) {
                ItemStack itemStack = new ItemStack(shopSign.getMaterial(), Math.min(64, itemsLeft));
                sign.getLocation().getWorld().dropItemNaturally(sign.getLocation(), itemStack);
                itemsLeft -= itemStack.getAmount();
            }
        }
        else if (shopSign.getType() == ShopSign.ShopSignType.BUY) {
            int itemsLeft = shopSign.getBoughtNumber();
            while (itemsLeft > 0) {
                ItemStack itemStack = new ItemStack(shopSign.getMaterial(), Math.min(64, itemsLeft));
                sign.getLocation().getWorld().dropItemNaturally(sign.getLocation(), itemStack);
                itemsLeft -= itemStack.getAmount();
            }
        }

        event.getPlayer().sendMessage(ChatColor.GREEN + "Handelsschild entfernt.");
    }
}

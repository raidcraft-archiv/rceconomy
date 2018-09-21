package de.raidcraft.rceconomy.expsign;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.chestshop.ShopUseConformer;
import de.raidcraft.rceconomy.tables.TAccount;
import de.raidcraft.util.PlayerExperienceUtil;
import de.raidcraft.util.SignUtil;
import de.raidcraft.util.UUIDUtil;
import io.ebean.EbeanServer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by Philip on 25.01.2016.
 */
public class ExpSignListener implements Listener {

    private static int EXP_PER_SELL = 100;

    private static String EXP_SHOP_TAG = "EXP-Handel";

    private String[] formatSign(ExpShopType shopType, Player owner, double price) {
        String[] lines = new String[4];

        lines[0] = ChatColor.YELLOW + "[" + ChatColor.GREEN + EXP_SHOP_TAG + ChatColor.YELLOW + "]";
        if(shopType == ExpShopType.ADMIN_BUY || shopType == ExpShopType.ADMIN_SELL) {
            lines[1] = ChatColor.AQUA.toString() + "1" + ChatColor.WHITE +
                    "-" + ChatColor.AQUA + "Server";
        } else {
            lines[1] = ChatColor.AQUA.toString() + UUIDUtil.getPlayerId(owner.getUniqueId()) + ChatColor.WHITE +
                    "-" + ChatColor.AQUA + owner.getName().substring(0, Math.min(9, owner.getName().length()));
        }
        lines[2] = ChatColor.BLACK + shopType.getDisplayText();
        lines[3] = RaidCraft.getEconomy().getFormattedAmount(price);

        return lines;
    }

    private int getOwnerId(String line) {

        if(line == null || line.isEmpty()) {
            return 0;
        }

        String[] parts = line.split("-");

        if(parts.length < 2) {
            return 0;
        }

        int id = 0;
        try {
            id = Integer.parseInt(parts[0]);
        } catch(NumberFormatException e) {
            return 0;
        }
        return id;
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {

        // Check sign tag
        if(!SignUtil.strip(event.getLine(0)).equalsIgnoreCase(EXP_SHOP_TAG)) {
            return;
        }

        // Get type
        ExpShopType shopType = ExpShopType.getByDisplayText(ChatColor.stripColor(event.getLine(2)));
        if(shopType == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Gebe in der dritten Zeile den Typ des Shops an (Ankauf oder Verkauf)!");
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission(shopType.getPermission())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keinen EXP-Shop aufstellen!");
            return;
        }

        // Get price
        double price = RaidCraft.getEconomy().parseCurrencyInput(ChatColor.stripColor(event.getLine(3)));
        if(price == 0) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Gebe in der vierten Zeile den Preis an (dieser darf nicht 0 sein)!");
            return;
        }

        // Format lines
        String[] formattedLines = formatSign(shopType, event.getPlayer(), price);
        for(int i = 0; i < 4; i ++) {
            event.setLine(i, formattedLines[i]);
        }

        event.getPlayer().sendMessage(
                ChatColor.GREEN + "Der EXP-Shop wurde erfolgreich erstellt!");
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {

        if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_AIR)
                || event.getPlayer().isSneaking()) {
            ShopUseConformer.unregister(getClass().getSimpleName(), event.getPlayer().getUniqueId());
            return;
        }

        // Check if sign
        if(!SignUtil.isSign(event.getClickedBlock())) {
            ShopUseConformer.unregister(getClass().getSimpleName(), event.getPlayer().getUniqueId());
            return;
        }

        Sign sign = SignUtil.getSign(event.getClickedBlock());
        if(sign == null) {
            ShopUseConformer.unregister(getClass().getSimpleName(), event.getPlayer().getUniqueId());
            return;
        }

        // Check sign tag
        if(!SignUtil.strip(sign.getLine(0)).equalsIgnoreCase(EXP_SHOP_TAG)) {
            ShopUseConformer.unregister(getClass().getSimpleName(), event.getPlayer().getUniqueId());
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission("expshop.use")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keinen EXP-Shop benutzen!");
            ShopUseConformer.unregister(getClass().getSimpleName(), event.getPlayer().getUniqueId());
            return;
        }

        // Get type
        ExpShopType shopType = ExpShopType.getByDisplayText(ChatColor.stripColor(sign.getLine(2)));
        if(shopType == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Unbekannter Shop Typ!");
            return;
        }

        // Get price
        double price = RaidCraft.getEconomy().parseCurrencyInput(ChatColor.stripColor(sign.getLine(3)));
        if(price == 0) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Der Preis dieses Shops ist inkorrekt!");
            return;
        }

        // Check if owner
        int ownerId = getOwnerId(ChatColor.stripColor(sign.getLine(1)));
        if(ownerId == 0) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Dieser Shop ist kaputt!");
            return;
        }
        if(ownerId != 1 && UUIDUtil.getUuidFromPlayerId(ownerId).equals(event.getPlayer().getUniqueId()))
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Dieser Shop gehört dir!");
            return;
        }

        if(shopType == ExpShopType.SELL || shopType == ExpShopType.ADMIN_SELL) {

            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Der Verkauf von EXP wird derzeit nicht unterstützt!");
            return;
        }
        else if(shopType == ExpShopType.BUY || shopType == ExpShopType.ADMIN_BUY) {

            PlayerExperienceUtil expUtil = new PlayerExperienceUtil(event.getPlayer());

            // Check if player has enough exp
            if(!expUtil.hasExp(EXP_PER_SELL)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Du benötigst mindestens " + EXP_PER_SELL + "EXP zum verkaufen!");
                event.getPlayer().sendMessage(ChatColor.RED + "Aktuell besitzt du " + expUtil.getCurrentExp() + "EXP.");
                return;
            }

            // Check if confirmed
            if(!ShopUseConformer.checkOrRegister(getClass().getSimpleName(), event.getPlayer().getUniqueId(), sign.getLocation(), shopType.name(), event.getAction())) {
                event.getPlayer().sendMessage(ChatColor.GOLD + "Klicke erneut um den Verkauf von " +
                        EXP_PER_SELL + "EXP");
                event.getPlayer().sendMessage(ChatColor.GOLD + "für " +
                        RaidCraft.getEconomy().getFormattedAmount(price) +
                        ChatColor.GOLD + " zu bestätigen!");
                event.setCancelled(true);
                return;
            }

            if(shopType == ExpShopType.ADMIN_BUY) {
                // Add to seller
                RaidCraft.getEconomy().add(event.getPlayer().getUniqueId(), price, BalanceSource.TRADE,
                        EXP_PER_SELL + "EXP an den Server verkauft");
            } else {
                // Check player balance
                if (!RaidCraft.getEconomy().hasEnough(UUIDUtil.getUuidFromPlayerId(ownerId), price)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(
                            ChatColor.RED + "Der Shopbesitzer kann sich den Kauf nicht leisten!");
                    return;
                }

                // Subtract from player
                RaidCraft.getEconomy().substract(UUIDUtil.getUuidFromPlayerId(ownerId), price, BalanceSource.TRADE,
                        EXP_PER_SELL + "EXP von " + event.getPlayer().getName() + " gekauft");
                // Add to seller
                RaidCraft.getEconomy().add(event.getPlayer().getUniqueId(), price, BalanceSource.TRADE,
                        EXP_PER_SELL + "EXP an " +
                                UUIDUtil.getNameFromUUID(UUIDUtil.getUuidFromPlayerId(ownerId)) + " verkauft");
                // Add EXP to buyer
                Player owner = Bukkit.getPlayer(UUIDUtil.getUuidFromPlayerId(ownerId));
                if(owner != null) {
                    PlayerExperienceUtil ownerExpUtil = new PlayerExperienceUtil(owner);
                    ownerExpUtil.setExp(ownerExpUtil.getCurrentExp() + EXP_PER_SELL);
                    owner.sendMessage(ChatColor.GREEN + "Dir wurden " + EXP_PER_SELL + "EXP verkauft!");
                } else {
                    EbeanServer database = RaidCraft.getComponent(RCEconomyPlugin.class).getDatabase();
                    TAccount tAccount = database.find(TAccount.class).where()
                            .eq("name", UUIDUtil.getUuidFromPlayerId(ownerId).toString().toLowerCase()).findOne();
                    tAccount.setExp(tAccount.getExp() + EXP_PER_SELL);
                    database.update(tAccount);
                }
            }

            // Subtract items from player
            expUtil.setExp(expUtil.getCurrentExp() - EXP_PER_SELL);

            return;
        }

        // Prevent shop destruction
        event.setCancelled(true);
        return;
    }
}

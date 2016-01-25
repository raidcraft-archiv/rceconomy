package de.raidcraft.rceconomy.chestshop;

import com.mojang.util.UUIDTypeAdapter;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rceconomy.bankchest.BankChestManager;
import de.raidcraft.rceconomy.tables.TBankChest;
import de.raidcraft.util.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Philip on 25.01.2016.
 */
public class ChestshopListener implements Listener {

    private static String CHESTSHOP_TAG = "Shop";

    public enum ShopType{
        SELL("Verkauf", "chestshop.create"),
        BUY("Ankauf", "chestshop.create"),
        ADMIN_SELL("Server-Verkauf", "chesthop.admin"),
        ADMIN_BUY("Server-Ankauf", "chestshop.admin");

        private String displayText;
        private String permission;

        private ShopType(String displayText, String permission) {
            this.displayText = displayText;
            this.permission = permission;
        }

        public String getDisplayText() {
            return displayText;
        }

        public String getPermission() {
            return permission;
        }

        public static ShopType getByDisplayText(String displayText) {
            for(ShopType shopType : ShopType.values()) {
                if(shopType.getDisplayText().equalsIgnoreCase(displayText)) {
                    return shopType;
                }
            }
            return null;
        }
    }
    private String[] formatSign(ShopType shopType, Player owner, double price) {
        String[] lines = new String[4];

        lines[0] = ChatColor.YELLOW + "[" + ChatColor.GREEN + CHESTSHOP_TAG + ChatColor.YELLOW + "]";
        lines[1] = ChatColor.AQUA.toString() + UUIDUtil.getPlayerId(owner.getUniqueId()) + ChatColor.WHITE +
                "-" + ChatColor.AQUA + owner.getName();
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
        if(!SignUtil.strip(event.getLine(0)).equalsIgnoreCase(CHESTSHOP_TAG))
        {
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission(ShopType.SELL.getPermission())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keinen Shop aufstellen!");
            return;
        }

        // Chest
        Chest chest;
        Block block = event.getBlock().getRelative(0, -1, 0);
        if(block == null || !(block.getState() instanceof Chest)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Das Schild muss über einer Kisten angebracht werden!");
            return;
        }
        chest = (Chest)block.getState();

        // Get type
        ShopType shopType = ShopType.getByDisplayText(ChatColor.stripColor(event.getLine(2)));
        if(shopType == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Gebe in der dritten Zeile den Typ des Shops an (Ankauf oder Verkauf)!");
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

        // Create item frame
        //TODO: Item frame creation

        event.getPlayer().sendMessage(
                ChatColor.GREEN + "Der Shop wurde erfolgreich erstellt!");
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {

        if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
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

        // Check sign tag
        if(!SignUtil.strip(sign.getLine(0)).equalsIgnoreCase(CHESTSHOP_TAG))
        {
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission("chestshop.use")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Bankkisten benutzen!");
            return;
        }

        // Chest
        Chest chest;
        Block block = event.getClickedBlock().getRelative(0, -1, 0);
        if(block == null || !(block.getState() instanceof Chest)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Die Kiste dieses Shops fehlt!");
            return;
        }
        chest = (Chest)block.getState();

        // Get type
        ShopType shopType = ShopType.getByDisplayText(ChatColor.stripColor(sign.getLine(2)));
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
        int ownerId = getOwnerId(sign.getLine(1));
        if(UUIDUtil.getUuidFromPlayerId(ownerId).equals(event.getPlayer().getUniqueId()))
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Dieser Shop gehört dir!");
            return;
        }

        if(shopType == ShopType.SELL) {

            // Get current item
            ItemStack itemStack = null;
            for(ItemStack currentItem : chest.getInventory().getContents()) {
                // Ignore damaged or enchanted items
                if(currentItem == null || currentItem.getType() == Material.AIR ||
                        currentItem.getDurability() != 0 || currentItem.getEnchantments().size() != 0) {
                    continue;
                }
                itemStack = currentItem;
                break;
            }
            if(itemStack == null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Dieser Shop is leer!");
                return;
            }

            int itemAmount = 1;
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                itemAmount = Math.min(itemStack.getAmount(), 64);
            }
            double totalPrice = itemAmount * price;

            // Check player balance
            if(!RaidCraft.getEconomy().hasEnough(event.getPlayer().getUniqueId(), totalPrice)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(
                        ChatColor.RED + "Du kannst dir den Kauf nicht leisten (benötigt " +
                                RaidCraft.getEconomy().getFormattedAmount(totalPrice) + ChatColor.RED + ")!");
                return;
            }

            // Subtract from player
            RaidCraft.getEconomy().substract(event.getPlayer().getUniqueId(), totalPrice, BalanceSource.TRADE,
                    itemAmount + "x" + ItemUtils.getFriendlyName(itemStack.getType()) + " von " +
                            UUIDUtil.getNameFromUUID(UUIDUtil.getUuidFromPlayerId(ownerId)) + " gekauft");
            // Add to seller
            RaidCraft.getEconomy().add(UUIDUtil.getUuidFromPlayerId(ownerId), totalPrice, BalanceSource.TRADE,
                    itemAmount + "x" + ItemUtils.getFriendlyName(itemStack.getType()) + " an " +
                           event.getPlayer().getName() + " verkauft");
            // Add item to player
            InventoryUtils.addOrDropItems(event.getPlayer(), new ItemStack(itemStack.getType(), itemAmount));
            // Subtract items from chest
            chest.getInventory().removeItem(new ItemStack(itemStack.getType(), itemAmount));
        }
        else if(shopType == ShopType.BUY) {

            // Get space left
            int spaceLeft = 0;
            Material material = null;
            for(ItemStack currentItem : chest.getInventory().getContents()) {
                if(currentItem == null) {
                    spaceLeft = 64;
                    if(material != null) break;
                } else {
                    material = currentItem.getType();
                    if (spaceLeft > 0) break;
                }
            }

            if(spaceLeft == 0 || material == null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Dieser Shop kauft derzeit nichts an!");
                return;
            }

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

            int itemAmount = 1;
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                itemAmount = Math.min(spaceLeft, itemInventoryCount);
            }
            double totalPrice = itemAmount * price;

            // Check player balance
            if(!RaidCraft.getEconomy().hasEnough(UUIDUtil.getUuidFromPlayerId(ownerId), totalPrice)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(
                        ChatColor.RED + "Der Shopbesitzer kann sich den Kauf nicht leisten!");
                return;
            }

            // Subtract from player
            RaidCraft.getEconomy().substract(UUIDUtil.getUuidFromPlayerId(ownerId), totalPrice, BalanceSource.TRADE,
                    itemAmount + "x" + ItemUtils.getFriendlyName(material) + " von " +
                            UUIDUtil.getNameFromUUID(UUIDUtil.getUuidFromPlayerId(ownerId)) + " gekauft");
            // Add to seller
            RaidCraft.getEconomy().add(event.getPlayer().getUniqueId(), totalPrice, BalanceSource.TRADE,
                    itemAmount + "x" + ItemUtils.getFriendlyName(material) + " an " +
                            event.getPlayer().getName() + " verkauft");
            // Add item to chest
            chest.getInventory().addItem(new ItemStack(material, itemAmount));
            // Subtract items from player
            event.getPlayer().getInventory().removeItem(new ItemStack(material, itemAmount));

            return;
        }

        // Prevent shop destruction
        event.setCancelled(true);
        return;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {

        if(!(event.getInventory().getHolder() instanceof Chest) &&
                !(event.getInventory().getHolder() instanceof DoubleChest)) {
            return;
        }

        Sign sign1 = null;
        Sign sign2 = null;

        if(event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();
            Block signBlock = chest.getLocation().getBlock().getRelative(0, 1, 0);
            if(!SignUtil.isSign(signBlock)) {
                return;
            }
            sign1 = SignUtil.getSign(signBlock);
        }

        else if(event.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest)event.getInventory().getHolder();
            Block signBlock1 = doubleChest.getLocation().getBlock().getRelative(0, 1, 0);
            if(SignUtil.isSign(signBlock1)) {
                sign1 = SignUtil.getSign(signBlock1);
            }

            // Find second chest block
            do {
                Block chestBlock2;

                chestBlock2 = doubleChest.getLocation().getBlock().getRelative(1, 0, 0);
                if ((sign2 = getSign(chestBlock2)) != null) {
                    break;
                }

                chestBlock2 = doubleChest.getLocation().getBlock().getRelative(-1, 0, 0);
                if ((sign2 = getSign(chestBlock2)) != null) {
                    break;
                }

                chestBlock2 = doubleChest.getLocation().getBlock().getRelative(0, 0, 1);
                if ((sign2 = getSign(chestBlock2)) != null) {
                    break;
                }

                chestBlock2 = doubleChest.getLocation().getBlock().getRelative(0, 0, -1);
                if ((sign2 = getSign(chestBlock2)) != null) {
                    break;
                }
            } while(false);
        }

        // Check sign
        Location location = null;
        Sign sign = null;
        if((sign1 != null && SignUtil.strip(sign1.getLine(0)).equalsIgnoreCase(CHESTSHOP_TAG))) {
            location = sign1.getLocation();
            sign = sign1;
        }
        else if((sign2 != null && SignUtil.strip(sign2.getLine(0)).equalsIgnoreCase(CHESTSHOP_TAG))) {
            location = sign2.getLocation();
            sign = sign2;
        }
        else if(location == null || sign == null) {
            return;
        }

        //TODO: Update item frame
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if(!(event.getInventory().getHolder() instanceof Chest) &&
                !(event.getInventory().getHolder() instanceof DoubleChest)) {
            return;
        }

        Sign sign1 = null;
        Sign sign2 = null;

        if(event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();
            Block signBlock = chest.getLocation().getBlock().getRelative(0, 1, 0);
            if(!SignUtil.isSign(signBlock)) {
                return;
            }
            sign1 = SignUtil.getSign(signBlock);
        }

        else if(event.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest)event.getInventory().getHolder();
            Block signBlock1 = doubleChest.getLocation().getBlock().getRelative(0, 1, 0);
            if(SignUtil.isSign(signBlock1)) {
                sign1 = SignUtil.getSign(signBlock1);
            }

            // Find second chest block
            do {
                Block chestBlock2;

                chestBlock2 = doubleChest.getLocation().getBlock().getRelative(1, 0, 0);
                if ((sign2 = getSign(chestBlock2)) != null) {
                    break;
                }

                chestBlock2 = doubleChest.getLocation().getBlock().getRelative(-1, 0, 0);
                if ((sign2 = getSign(chestBlock2)) != null) {
                    break;
                }

                chestBlock2 = doubleChest.getLocation().getBlock().getRelative(0, 0, 1);
                if ((sign2 = getSign(chestBlock2)) != null) {
                    break;
                }

                chestBlock2 = doubleChest.getLocation().getBlock().getRelative(0, 0, -1);
                if ((sign2 = getSign(chestBlock2)) != null) {
                    break;
                }
            } while(false);
        }

        // Check sign
        Location location = null;
        Sign sign = null;
        if((sign1 != null && SignUtil.strip(sign1.getLine(0)).equalsIgnoreCase(CHESTSHOP_TAG))) {
            location = sign1.getLocation();
            sign = sign1;
        }
        else if((sign2 != null && SignUtil.strip(sign2.getLine(0)).equalsIgnoreCase(CHESTSHOP_TAG))) {
            location = sign2.getLocation();
            sign = sign2;
        }
        else if(location == null || sign == null) {
            return;
        }

        //TODO: Update item frame
    }

    private Sign getSign(Block chestBlock2) {

        if(!(chestBlock2 instanceof Chest)) {
            return null;
        }

        Block signBlock = chestBlock2.getRelative(0, 1, 0);
        if(SignUtil.isSign(signBlock)) {
            return SignUtil.getSign(signBlock);
        }
        return null;
    }
}

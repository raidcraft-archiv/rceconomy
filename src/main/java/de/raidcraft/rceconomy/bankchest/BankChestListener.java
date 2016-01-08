package de.raidcraft.rceconomy.bankchest;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rceconomy.tables.TBankChest;
import de.raidcraft.util.SignUtil;
import de.raidcraft.util.UUIDUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Philip on 07.01.2016.
 */
public class BankChestListener implements Listener {

    private static String BANK_CHEST_TAG = "Bankkiste";
    private static String FREE_TAG = "~ Frei ~";
    private static String SINGLE_PERMISSION = "bankchest.use.single";
    private static String DOUBLE_PERMISSION = "bankchest.use.double";
    private static String ADMIN_PERMISSION = "bankchest.admin";

    private String[] formatSign(Inventory inventory, TBankChest bankChest) {
        String[] lines = new String[4];

        lines[0] = ChatColor.YELLOW + "[" + ChatColor.GREEN + BANK_CHEST_TAG + ChatColor.YELLOW + "]";
        if(bankChest != null) {
            lines[1] = ChatColor.AQUA.toString() + bankChest.getId() + ChatColor.WHITE +
                    "-" + ChatColor.AQUA + UUIDUtil.getNameFromUUID(bankChest.getPlayerId());
        } else {
            lines[1] = ChatColor.AQUA.toString() + FREE_TAG;
        }
        lines[2] = ChatColor.BLACK + "Aktueller Wert:";
        lines[3] = RaidCraft.getEconomy().getFormattedAmount(BankChestManager.get().getContentValue(null, inventory, false));

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
        if(!SignUtil.strip(event.getLine(0)).equalsIgnoreCase(BANK_CHEST_TAG))
        {
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission("bankchest.create")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Bankkisten aufstellen!");
            return;
        }

        // Chest
        Chest chest;
        Block block = event.getBlock().getRelative(0, -1, 0);
        if(block == null || !(block.getState() instanceof Chest)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Unter dem Schild muss eine Kiste stehen!");
            return;
        }
        chest = (Chest)block.getState();

        // Format lines
        String[] formattedLines = formatSign(chest.getInventory(), null);
        for(int i = 0; i < 4; i ++) {
            event.setLine(i, formattedLines[i]);
        }

        event.getPlayer().sendMessage(
                ChatColor.GREEN + "Das Bankkistenschild wurde erfolgreich erstellt!");
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
        if(!SignUtil.strip(sign.getLine(0)).equalsIgnoreCase(BANK_CHEST_TAG))
        {
            return;
        }

        // Check permissions
        if(!event.getPlayer().hasPermission(SINGLE_PERMISSION) &&
                !event.getPlayer().hasPermission(DOUBLE_PERMISSION)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Bankkisten benutzen!");
            return;
        }

        // Chest
        Chest chest;
        Block block = event.getClickedBlock().getRelative(0, -1, 0);
        if(block == null || !(block.getState() instanceof Chest)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Unter dem Schild muss eine Kiste stehen!");
            return;
        }
        chest = (Chest)block.getState();

        // Owner
        String owner = SignUtil.strip(sign.getLine(1));

        BankChestManager.BankChestType bankChestType;

        // Set new owner
        if(event.getPlayer().isSneaking()) {

            // Get player chests
            TBankChest singleChest = null;
            TBankChest doubleChest = null;
            singleChest = BankChestManager.get().getChest(event.getPlayer().getUniqueId(),
                    BankChestManager.BankChestType.SINGLE_CHEST);
            doubleChest = BankChestManager.get().getChest(event.getPlayer().getUniqueId(),
                    BankChestManager.BankChestType.DOUBLE_CHEST);

            if(!owner.equals(FREE_TAG)) {
                if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().hasPermission(ADMIN_PERMISSION)) {
                    BankChestManager.get().unregister(getOwnerId(owner));
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Die Bankkiste wurde wieder freigegeben!");
                    return;
                }
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Diese Bankkiste ist bereits vergeben!");
                return;
            }

            // Double chest
            if(chest.getInventory().getHolder() instanceof DoubleChest) {
                bankChestType = BankChestManager.BankChestType.DOUBLE_CHEST;
                if(!event.getPlayer().hasPermission(ADMIN_PERMISSION) &&
                        !event.getPlayer().hasPermission(DOUBLE_PERMISSION)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Bank Doppelkiste besitzen!");
                    return;
                }
                if(doubleChest != null) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Du hast bereits eine Bank Doppelkiste!");
                    return;
                }
            }

            // Single chest
            else {
                bankChestType = BankChestManager.BankChestType.SINGLE_CHEST;
                if(!event.getPlayer().hasPermission(ADMIN_PERMISSION) &&
                        !event.getPlayer().hasPermission(SINGLE_PERMISSION)){
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Bank Einzelkiste besitzen!");
                    return;
                }
                if(singleChest != null) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Du hast bereits eine Bank Einzelkiste!");
                    return;
                }
            }

            TBankChest playerChest = BankChestManager.get().register(sign.getLocation(), bankChestType, event.getPlayer().getUniqueId());
            if(playerChest == null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Es ist ein Fehler beim Zuordnen der Bankkiste aufgetreten!");
                return;
            }

            // Format lines
            String[] formattedLines = formatSign(chest.getInventory(), playerChest);
            for(int i = 0; i < 4; i ++) {
                sign.setLine(i, formattedLines[i]);
            }
            sign.update();

            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Diese Bankkiste gehört nun dir!");
            return;
        }

        // Owner
        if(owner.equals(FREE_TAG)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Diese Bankkiste hat noch keinen Besitzer!");
            return;
        }

        TBankChest playerChest = BankChestManager.get().getChest(getOwnerId(owner));
        if(playerChest == null) {
            // Format lines
            String[] formattedLines = formatSign(chest.getInventory(), null);
            for(int i = 0; i < 4; i ++) {
                sign.setLine(i, formattedLines[i]);
            }
            sign.update();

            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du bist nicht der Eigentümer dieser Bankkiste!");
            return;
        }
        else if(!playerChest.getPlayerId().equals(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du bist nicht der Eigentümer dieser Bankkiste!");
            return;
        }

        // Format lines
        String[] formattedLines = formatSign(chest.getInventory(), playerChest);
        for(int i = 0; i < 4; i ++) {
            sign.setLine(i, formattedLines[i]);
        }
        sign.update();

        // Check delay
        if(!BankChestManager.get().isCooldownOver(playerChest)) {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Date date = BankChestManager.get().getNextPossibleEmptying(playerChest);
            String nextDate = df.format(date);
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Die nächste Leerung ist erst wieder am " + nextDate + " möglich!");
            return;
        }

        Economy economy = RaidCraft.getEconomy();

        // Sell items
        double value = BankChestManager.get().getContentValue(event.getPlayer().getUniqueId(), chest.getInventory(), true);
        if(value > 0) {
            BankChestManager.get().updateEmptyingDate(sign.getLocation());
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Deine Bankkiste wurde geleert (" + economy.getFormattedAmount(value)+ ChatColor.GREEN + ")" + "!");

            // Format lines
            formattedLines = formatSign(chest.getInventory(), playerChest);
            for(int i = 0; i < 4; i ++) {
                sign.setLine(i, formattedLines[i]);
            }
            sign.update();

            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.GREEN + "Der Inhalt deiner Bankkiste ist wertlos!");
        return;
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {

        if (event.getBlock() == null) {
            return;
        }

        // Check if sign
        if (!SignUtil.isSign(event.getBlock())) {
            return;
        }

        Sign sign = SignUtil.getSign(event.getBlock());
        if (sign == null) {
            return;
        }

        // Check sign tag
        if (!SignUtil.strip(sign.getLine(0)).equalsIgnoreCase(BANK_CHEST_TAG)) {
            return;
        }

        TBankChest bankChest = BankChestManager.get().getChest(getOwnerId(SignUtil.strip(sign.getLine(1))));
        if(bankChest == null) {
            return;
        }

        // unregister chest
        BankChestManager.get().unregister(bankChest.getId());
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
        if((sign1 != null && SignUtil.strip(sign1.getLine(0)).equalsIgnoreCase(BANK_CHEST_TAG))) {
            location = sign1.getLocation();
            sign = sign1;
        }
        else if((sign2 != null && SignUtil.strip(sign2.getLine(0)).equalsIgnoreCase(BANK_CHEST_TAG))) {
            location = sign2.getLocation();
            sign = sign2;
        }
        else if(location == null || sign == null) {
            return;
        }

        TBankChest tBankChest = BankChestManager.get().getChest(location);
        if(tBankChest == null) {
            return;
        }

        // Update sign
        String[] formattedLines = formatSign(event.getInventory(), tBankChest);
        for(int i = 0; i < 4; i ++) {
            sign.setLine(i, formattedLines[i]);
        }
        sign.update();
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

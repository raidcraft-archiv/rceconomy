package de.raidcraft.rceconomy.bankchest;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rceconomy.tables.TBankChest;
import de.raidcraft.util.SignUtil;
import de.raidcraft.util.UUIDUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    private String[] formatSign(Chest chest, TBankChest bankChest) {
        String[] lines = new String[4];

        lines[0] = ChatColor.YELLOW + "[" + ChatColor.GREEN + BANK_CHEST_TAG + ChatColor.YELLOW + "]";
        if(bankChest != null) {
            lines[1] = ChatColor.AQUA.toString() + bankChest.getId() + ChatColor.WHITE +
                    "-" + ChatColor.AQUA + UUIDUtil.getNameFromUUID(bankChest.getPlayerId());
        } else {
            lines[1] = ChatColor.AQUA.toString() + FREE_TAG;
        }
        lines[2] = ChatColor.BLACK + "Aktueller Wert:";
        lines[3] = RaidCraft.getEconomy().getFormattedAmount(BankChestManager.get().getContentValue(null, chest, false));

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
        String[] formattedLines = formatSign(chest, null);
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
            if(!event.getPlayer().hasPermission(ADMIN_PERMISSION)) {
                singleChest = BankChestManager.get().getChest(event.getPlayer().getUniqueId(),
                        BankChestManager.BankChestType.SINGLE_CHEST);
                doubleChest = BankChestManager.get().getChest(event.getPlayer().getUniqueId(),
                        BankChestManager.BankChestType.DOUBLE_CHEST);
            }

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
            if(chest instanceof DoubleChest) {
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
            String[] formattedLines = formatSign(chest, playerChest);
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
        if(playerChest == null || !playerChest.getPlayerId().equals(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Du bist nicht der Eigentümer dieser Bankkiste!");
            return;
        }

        // Format lines
        String[] formattedLines = formatSign(chest, playerChest);
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
        double value = BankChestManager.get().getContentValue(event.getPlayer().getUniqueId(), chest, true);
        if(value > 0) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Deine Bankkiste wurde geleert (" + economy.getFormattedAmount(value)+ ChatColor.GREEN + ")" + "!");
            return;
        }
    }
}

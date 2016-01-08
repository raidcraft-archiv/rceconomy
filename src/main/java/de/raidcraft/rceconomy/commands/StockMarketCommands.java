package de.raidcraft.rceconomy.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.bankchest.ui.MaterialPricesMenu;
import de.raidcraft.reference.Colors;
import de.raidcraft.util.UUIDUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Author: Philip
 * Date: 16.10.12 - 19:46
 * Description:
 */
public class StockMarketCommands {

    private RCEconomyPlugin plugin;
    private Economy api;

    public StockMarketCommands(RCEconomyPlugin plugin) {

        this.plugin = plugin;
        api = plugin.getApi();
    }

    @Command(
            aliases = {"börse", "rcbörse", "preise"},
            desc = "Börse"
    )
    public void stockMarket(CommandContext context, CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Spielerkommando");
            return;
        }

        // Open stock market menu
        if(!MaterialPricesMenu.get().open((Player)sender, 1)) {
            sender.sendMessage(ChatColor.RED + "Es ist ein Fehler beim öffnen des Menüs aufgetreten!");
        }
    }
}

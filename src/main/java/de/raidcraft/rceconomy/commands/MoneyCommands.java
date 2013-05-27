package de.raidcraft.rceconomy.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.RaidCraft;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Author: Philip
 * Date: 16.10.12 - 19:46
 * Description:
 */
public class MoneyCommands {

    private final RCEconomyPlugin plugin;

    public MoneyCommands(RCEconomyPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"money", "coins", "geld"},
            desc = "Main money command",
            flags = "p:"
    )
    @NestedCommand(value = NestedLootCommands.class, executeBody = true)
    public void money(CommandContext context, CommandSender sender) throws CommandException {

        String target = sender.getName();
        if(context.hasFlag('p')) {
            target = context.getFlag('p');
            if(!plugin.accountExists(target)) {
                throw new CommandException("Der Bankaccount '" + target + "' existiert nicht!");
            }
        }
        double balance = plugin.getBalance(target);
        sender.sendMessage(ChatColor.GREEN + "Kontostand von '" + ChatColor.YELLOW + target + ChatColor.GREEN + "': " + plugin.getFormattedAmount(balance));
    }

    public static class NestedLootCommands {

        private final RCEconomyPlugin plugin;

        public NestedLootCommands(RCEconomyPlugin plugin) {

            this.plugin = plugin;
        }

        @Command(
                aliases = {"reload"},
                desc = "Reloads plugin"
        )
        @CommandPermissions("rceconomy.reload")
        public void reload(CommandContext context, CommandSender sender) throws CommandException {

            RaidCraft.getComponent(RCEconomyPlugin.class).reload();
            sender.sendMessage(ChatColor.GREEN + "RCEconomy wurde neugeladen!");
        }

        @Command(
                aliases = {"pay"},
                desc = "Pay",
                min = 2
        )
        @CommandPermissions("rceconomy.use")
        public void pay(CommandContext context, CommandSender sender) throws CommandException {

            String target = context.getString(0).toLowerCase();
            double amount = context.getDouble(1);

            if(amount < 0) {
                throw new CommandException("Der Betrag muss positiv sein!");
            }

            if(!plugin.accountExists(target)) {
                throw new CommandException("Der Bankaccount '" + target + "' existiert nicht!");
            }

            if(!plugin.hasEnough(sender.getName(), amount)) {
                throw new CommandException("Du hast nicht genügend Coins auf deinem Konto!");
            }

            if(sender.getName().equalsIgnoreCase(target)) {
                throw new CommandException("Du kannst nicht an dich selbst überweisen!");
            }

            String targetFriendlyName = target;
            Player targetPlayer = Bukkit.getPlayer(target);
            String amountName = plugin.getFormattedAmount(round(amount));

            if(targetPlayer != null) {
                targetFriendlyName = targetPlayer.getName();
                targetPlayer.sendMessage(ChatColor.YELLOW + sender.getName() + ChatColor.GREEN + " hat dir " + amountName + ChatColor.GREEN + " überwiesen!");
            }

            plugin.modify(sender.getName(), -amount);
            plugin.modify(target, amount);
            sender.sendMessage(ChatColor.GREEN + "Du hast '" + ChatColor.YELLOW + targetFriendlyName + ChatColor.GREEN + "' " + amountName + ChatColor.GREEN + " überwiesen!");
        }

        @Command(
                aliases = {"flow"},
                desc = "Flow"
        )
        @CommandPermissions("rceconomy.use")
        public void flow(CommandContext context, CommandSender sender) throws CommandException {

            //TODO implement
        }

        @Command(
                aliases = {"give"},
                desc = "Give",
                min = 2
        )
         @CommandPermissions("rceconomy.admin")
         public void give(CommandContext context, CommandSender sender) throws CommandException {

            String target = context.getString(0).toLowerCase();
            double amount = context.getDouble(1);

            if(amount < 0) {
                throw new CommandException("Der Betrag muss positiv sein!");
            }

            if(!plugin.accountExists(target)) {
                throw new CommandException("Der Bankaccount '" + target + "' existiert nicht!");
            }

            plugin.modify(target, amount);
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target + "' wurde mit " + plugin.getFormattedAmount(round(amount)) + ChatColor.GREEN + " begünstigt!");
        }

        @Command(
                aliases = {"take"},
                desc = "Take",
                min = 2
        )
        @CommandPermissions("rceconomy.admin")
        public void take(CommandContext context, CommandSender sender) throws CommandException {

            String target = context.getString(0).toLowerCase();
            double amount = context.getDouble(1);

            if(amount < 0) {
                throw new CommandException("Der Betrag muss positiv sein!");
            }

            if(!plugin.accountExists(target)) {
                throw new CommandException("Der Bankaccount '" + target + "' existiert nicht!");
            }

            plugin.modify(target, -amount);
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target + "' wurde mit " + plugin.getFormattedAmount(round(amount)) + ChatColor.GREEN + " belastet!");
        }

        @Command(
                aliases = {"set"},
                desc = "Take",
                min = 2
        )
        @CommandPermissions("rceconomy.admin")
        public void set(CommandContext context, CommandSender sender) throws CommandException {

            String target = context.getString(0).toLowerCase();
            double amount = context.getDouble(1);

            if(!plugin.accountExists(target)) {
                throw new CommandException("Der Bankaccount '" + target + "' existiert nicht!");
            }

            plugin.set(target, amount);
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target + "' wurde auf " + plugin.getFormattedAmount(round(amount)) + ChatColor.GREEN + " gesetzt!");
        }
        private double round(double d) {

            return Math.round(d*100)/100.0;
        }
    }

}

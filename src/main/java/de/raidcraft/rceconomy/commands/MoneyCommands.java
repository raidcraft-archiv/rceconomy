package de.raidcraft.rceconomy.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.RaidCraft;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
            flags = "p"
    )
    @NestedCommand(value = NestedLootCommands.class, executeBody = true)
    public void money(CommandContext context, CommandSender sender) throws CommandException {

        double balance = plugin.getBalance(sender.getName());
        sender.sendMessage(ChatColor.GREEN + "Kontostand: " + CustomItemUtil.getSellPriceString(balance));
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
                desc = "Pay"
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

            plugin.modify(sender.getName(), -amount);
            plugin.modify(target, amount);
            sender.sendMessage(ChatColor.GREEN + "Du hast '" + ChatColor.YELLOW + target + ChatColor.GREEN + "' " + CustomItemUtil.getSellPriceString(round(amount)) + ChatColor.GREEN + " überwiesen!");
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
                desc = "Give"
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
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target + "' wurde mit " + CustomItemUtil.getSellPriceString(round(amount)) + ChatColor.GREEN + " begünstigt!");
        }

        @Command(
                aliases = {"take"},
                desc = "Take"
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
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target + "' wurde mit " + CustomItemUtil.getSellPriceString(round(amount)) + ChatColor.GREEN + " belastet!");
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
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target + "' wurde auf " + CustomItemUtil.getSellPriceString(round(amount)) + ChatColor.GREEN + " gesetzt!");
        }
        private double round(double d) {

            return Math.round(d*100)/100.0;
        }
    }

}

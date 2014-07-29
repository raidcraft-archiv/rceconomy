package de.raidcraft.rceconomy.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
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

    private RCEconomyPlugin plugin;
    private Economy api;

    public MoneyCommands(RCEconomyPlugin plugin) {

        this.plugin = plugin;
        api = plugin.getApi();
    }

    @Command(
            aliases = {"money", "coins", "geld"},
            desc = "Main money command"
    )
    @NestedCommand(value = NestedLootCommands.class, executeBody = true)
    public void money(CommandContext context, CommandSender sender) throws CommandException {

        if(!(sender instanceof Player)) {
            sender.sendMessage("Spielerkommando");
            return;
        }
        String target = ((Player) sender).getUniqueId().toString();
        double balance = api.getBalance(AccountType.PLAYER, target);
        sender.sendMessage(ChatColor.GREEN + "Dein Kontostand: " + api.getFormattedAmount(balance));
    }

    public static class NestedLootCommands {

        private RCEconomyPlugin plugin;
        private Economy api;

        public NestedLootCommands(RCEconomyPlugin plugin) {

            this.plugin = plugin;
            this.api = plugin.getApi();
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
                aliases = {"info", "player", "p"},
                desc = "Info"
        )
        @CommandPermissions("rceconomy.use")
        public void info(CommandContext context, CommandSender sender) throws CommandException {

            String target = sender.getName();
            if (context.argsLength() > 0) {
                if (!sender.hasPermission("rceconomy.admin")) {
                    throw new CommandException("Du hast keine Rechte dir Fremde Kontostände anzuzeigen!");
                }
                target = context.getString(0);
                if (!api.accountExists(AccountType.PLAYER, target)) {
                    throw new CommandException("Der Bankaccount '" + target + "' existiert nicht!");
                }
            }

            int number = 10;
            if (context.argsLength() > 1) {
                number = context.getInteger(1);
            }

            double balance = api.getBalance(AccountType.PLAYER, target);
            sender.sendMessage(ChatColor.YELLOW + target + "s" + ChatColor.GREEN + " Kontostand: " + api.getFormattedAmount(balance));
            api.printFlow(sender, AccountType.PLAYER, target, number);
        }

        @Command(
                aliases = {"pay"},
                desc = "Pay",
                min = 2
        )
        @CommandPermissions("rceconomy.use")
        public void pay(CommandContext context, CommandSender sender) throws CommandException {

            String target = context.getString(0).toLowerCase();
            // lets parse the input for an amount
            double amount = api.parseCurrencyInput(context.getJoinedStrings(1));

            if (amount <= 0.0) {
                throw new CommandException("Der Betrag muss positiv sein und mindestens 1 Kupfer betragen.");
            }

            if (!api.accountExists(AccountType.PLAYER, target)) {
                throw new CommandException("Der Bank Account '" + target + "' existiert nicht!");
            }

            if (!api.hasEnough(AccountType.PLAYER, sender.getName(), amount)) {
                throw new CommandException("Du hast nicht genügend Geld auf deinem Konto!");
            }

            if (sender.getName().equalsIgnoreCase(target)) {
                throw new CommandException("Du kannst dir nicht selbst Geld überweisen.");
            }

            if (Bukkit.getPlayer(target) != null) {
                target = Bukkit.getPlayer(target).getName();
            }

            String detail = sender.getName() + " --> " + target;
            api.modify(AccountType.PLAYER, sender.getName(), -amount, BalanceSource.PAY_COMMAND, detail);
            api.modify(AccountType.PLAYER, target, amount, BalanceSource.PAY_COMMAND, detail);

        }

        @Command(
                aliases = {"flow"},
                desc = "Flow"
        )
        @CommandPermissions("rceconomy.use")
        public void flow(CommandContext context, CommandSender sender) throws CommandException {

            int number = 10;
            if (context.argsLength() > 0) {
                number = context.getInteger(0);
            }

            api.printFlow(sender, AccountType.PLAYER, sender.getName(), number);
        }

        @Command(
                aliases = {"give"},
                desc = "Give",
                min = 2
        )
        @CommandPermissions("rceconomy.admin")
        public void give(CommandContext context, CommandSender sender, String target1) throws CommandException {

            String pitarget1 = context.getString(0).toLowerCase();
            // lets parse the input for an amount
            double amount = api.parseCurrencyInput(context.getJoinedStrings(1));

            if (amount <= 0.0) {
                throw new CommandException("Der Betrag muss positiv und mindestens 1 Kuper sein.");
            }

            if (!api.accountExists(AccountType.PLAYER, target1)) {
                throw new CommandException("Der Bank Account '" + target1 + "' existiert nicht!");
            }

            String detail = null;
            if (context.argsLength() > 2) {
                detail = context.getJoinedStrings(2);
            }
            api.modify(AccountType.PLAYER, target1, amount, BalanceSource.ADMIN_COMMAND, detail);
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target1 + "' wurde mit " + api.getFormattedAmount(round(amount)) + ChatColor.GREEN + " begünstigt!");
        }

        @Command(
                aliases = {"take"},
                desc = "Take",
                min = 2
        )
        @CommandPermissions("rceconomy.admin")
        public void take(CommandContext context, CommandSender sender) throws CommandException {

            String target = context.getString(0).toLowerCase();
            // lets parse the input for an amount
            double amount = api.parseCurrencyInput(context.getJoinedStrings(1));

            if (amount <= 0.0) {
                throw new CommandException("Der Betrag muss positiv und mindestens 1 Kuper sein.");
            }

            if (!api.accountExists(AccountType.PLAYER, target)) {
                throw new CommandException("Der Bank Account '" + target + "' existiert nicht!");
            }

            String detail = null;
            if (context.argsLength() > 2) {
                detail = context.getJoinedStrings(2);
            }
            api.modify(AccountType.PLAYER, target, -amount, BalanceSource.ADMIN_COMMAND, detail);
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target + "' wurde mit " + api.getFormattedAmount(round(amount)) + ChatColor.GREEN + " belastet!");
        }

        @Command(
                aliases = {"set"},
                desc = "Take",
                min = 2
        )
        @CommandPermissions("rceconomy.admin")
        public void set(CommandContext context, CommandSender sender) throws CommandException {

            String target = context.getString(0).toLowerCase();
            // lets parse the input for an amount
            double amount = api.parseCurrencyInput(context.getJoinedStrings(1));

            if (!api.accountExists(AccountType.PLAYER, target)) {
                throw new CommandException("Der Bank Account '" + target + "' existiert nicht!");
            }

            String detail = null;
            if (context.argsLength() > 2) {
                detail = context.getJoinedStrings(2);
            }
            api.set(AccountType.PLAYER, target, amount, BalanceSource.ADMIN_COMMAND, detail);
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target + "' wurde auf " + api.getFormattedAmount(round(amount)) + ChatColor.GREEN + " gesetzt!");
        }

        private double round(double d) {

            return Math.round(d * 100) / 100.0;
        }
    }
}

package de.raidcraft.rceconomy.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.rceconomy.BankActivity;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.tables.FlowTable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

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
            desc = "Main money command"
    )
    @NestedCommand(value = NestedLootCommands.class, executeBody = true)
    public void money(CommandContext context, CommandSender sender) throws CommandException {

        String target = sender.getName();
        double balance = plugin.getBalance(target);
        sender.sendMessage(ChatColor.GREEN + "Dein Kontostand: " + plugin.getFormattedAmount(balance));
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
                aliases = {"info", "player", "p"},
                desc = "Info"
        )
        @CommandPermissions("rceconomy.use")
        public void info(CommandContext context, CommandSender sender) throws CommandException {

            String target = sender.getName();
            if(context.argsLength() > 0) {
                if(!sender.hasPermission("rceconomy.admin")) {
                    throw new CommandException("Du hast keine Rechte dir Fremde Kontostände anzuzeigen!");
                }
                target = context.getString(0);
                if(!plugin.accountExists(target)) {
                    throw new CommandException("Der Bankaccount '" + target + "' existiert nicht!");
                }
            }

            int number = 10;
            if(context.argsLength() > 1) {
                number = context.getInteger(1);
            }

            double balance = plugin.getBalance(target);
            sender.sendMessage(ChatColor.YELLOW + target + "s" + ChatColor.GREEN + " Kontostand: " + plugin.getFormattedAmount(balance));
            showFlow(sender, target, number);
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

            if(Bukkit.getPlayer(target) != null) {
                target = Bukkit.getPlayer(target).getName();
            }

            String detail = sender.getName() + " --> " + target;
            plugin.modify(sender.getName(), -amount, BalanceSource.PAY_COMMAND, detail);
            plugin.modify(target, amount, BalanceSource.PAY_COMMAND, detail);

        }

        @Command(
                aliases = {"flow"},
                desc = "Flow"
        )
        @CommandPermissions("rceconomy.use")
        public void flow(CommandContext context, CommandSender sender) throws CommandException {

            int number = 10;
            if(context.argsLength() > 0) {
                number = context.getInteger(0);
            }

            showFlow(sender, sender.getName(), number);
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

            String detail = context.getJoinedStrings(2);
            plugin.modify(target, amount, BalanceSource.ADMIN_COMMAND, detail);
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

            String detail = context.getJoinedStrings(2);
            plugin.modify(target, -amount, BalanceSource.ADMIN_COMMAND, detail);
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

            String detail = context.getJoinedStrings(2);
            plugin.set(target, amount, BalanceSource.ADMIN_COMMAND, detail);
            sender.sendMessage(ChatColor.GREEN + "Der Bankaccount von '" + target + "' wurde auf " + plugin.getFormattedAmount(round(amount)) + ChatColor.GREEN + " gesetzt!");
        }
        private double round(double d) {

            return Math.round(d*100)/100.0;
        }

        private void showFlow(CommandSender sender, String target, int number) {

            List<BankActivity> activities = RaidCraft.getTable(FlowTable.class).getActivity(target, number);

            sender.sendMessage(ChatColor.GREEN + "Die letzten Kontobewegungen von " + ChatColor.YELLOW + target + ChatColor.GREEN + ":");
            ChatColor amountColor;
            for(BankActivity activity : activities) {
                if(activity.getAmount() > 0) {
                    amountColor = ChatColor.GREEN;
                }
                else {
                    amountColor = ChatColor.RED;
                }
                sender.sendMessage(amountColor + String.valueOf(activity.getAmount()) + ChatColor.YELLOW + " "
                        + activity.getSource().getFriendlyName() + ChatColor.YELLOW + " " + activity.getDate());
            }
        }
    }
}

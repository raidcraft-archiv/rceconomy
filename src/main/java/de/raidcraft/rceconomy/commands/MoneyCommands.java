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
import de.raidcraft.reference.Colors;
import de.raidcraft.util.UUIDUtil;
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

        if (!(sender instanceof Player)) {
            sender.sendMessage("Spielerkommando");
            return;
        }
        String target = UUIDUtil.castUUID(sender);
        double balance = api.getBalance(AccountType.PLAYER, target);
        sender.sendMessage(Colors.Chat.SUCCESS + "Dein Kontostand: " + api.getFormattedAmount(balance));
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
            sender.sendMessage(Colors.Chat.SUCCESS + "RCEconomy wurde neugeladen!");
        }

        @Command(
                aliases = {"info", "player", "p", "flow"},
                desc = "Zeigt den Verlauf deines Geldes an",
                usage = "<playername> <entries>"
        )
        @CommandPermissions("rceconomy.use")
        public void info(CommandContext context, CommandSender sender) throws CommandException {

            String target_id = null;
            String target_name  = null;
            if (context.argsLength() > 0) {
                if (!sender.hasPermission("rceconomy.admin")) {
                    throw new CommandException("Du hast keine Rechte dir Fremde Kontostände anzuzeigen!");
                }
                target_name = context.getString(0);
                target_id = UUIDUtil.getUUIDStringFromName(target_name);
                if (!api.accountExists(AccountType.PLAYER, target_id)) {
                    throw new CommandException("Der Bankaccount '" + target_name + "' existiert nicht!");
                }
            }
            if (target_id == null) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Spielerkommando");
                    return;
                }
                target_name = sender.getName();
                target_id = UUIDUtil.castUUID(sender);
            }

            int number = plugin.getEconomyConfig().sizePrintFlowEntries;
            if (context.argsLength() > 1) {
                number = context.getInteger(1);
            }

            double balance = api.getBalance(AccountType.PLAYER, target_id);
            sender.sendMessage(Colors.Chat.INFO + target_name
                    + "s" + Colors.Chat.SUCCESS + " Kontostand: " + api.getFormattedAmount(balance));
            api.printFlow(sender, AccountType.PLAYER, target_id, number);
        }

        @Command(
                aliases = {"pay", "transfer"},
                desc = "Überträgt Geld zu einem anderen Spieler",
                usage = "<Zielspieler> <Betrag>",
                min = 2
        )
        @CommandPermissions("rceconomy.use")
        public void pay(CommandContext context, CommandSender sender) throws CommandException {

            if (!(sender instanceof Player)) {
                sender.sendMessage("Spielerkommando");
                return;
            }
            String sender_id = UUIDUtil.castUUID(sender);
            String target_name = context.getString(0);
            String target_id = UUIDUtil.getUUIDStringFromName(target_name);
            // lets parse the input for an amount
            double amount = api.parseCurrencyInput(context.getJoinedStrings(1));

            if (amount <= 0.0) {
                throw new CommandException("Der Betrag muss positiv sein und mindestens 1 Kupfer betragen.");
            }

            if (!api.accountExists(AccountType.PLAYER, target_id)) {
                throw new CommandException("Der Bank Account '" + target_name + "' existiert nicht!");
            }

            if (!api.hasEnough(AccountType.PLAYER, sender_id, amount)) {
                throw new CommandException("Du hast nicht genügend Geld auf deinem Konto!");
            }

            if (sender.getName().equalsIgnoreCase(target_name)) {
                throw new CommandException("Du kannst dir nicht selbst Geld überweisen.");
            }

            String detail = sender.getName() + " --> " + target_name;
            api.modify(AccountType.PLAYER, sender_id, -amount, BalanceSource.PAY_COMMAND, detail);
            api.modify(AccountType.PLAYER, target_id, amount, BalanceSource.PAY_COMMAND, detail);

        }

        @Command(
                aliases = {"give"},
                desc = "ADMIN: Give a player money",
                usage = "<Spieler> <Betrag> [FlowDetails]",
                min = 2
        )
        @CommandPermissions("rceconomy.admin")
        public void give(CommandContext context, CommandSender sender) throws CommandException {

            String target_name = context.getString(0);
            String target_id = UUIDUtil.getUUIDStringFromName(target_name);
            double amount = context.getDouble(1);

            if (amount <= 0.0) {
                throw new CommandException("Der Betrag muss positiv und mindestens 1 Kuper sein.");
            }

            if (!api.accountExists(AccountType.PLAYER, target_id)) {
                throw new CommandException("Der Spieler Account '" + target_name + "' existiert nicht!");
            }

            String detail = null;
            if (context.argsLength() > 2) {
                detail = context.getJoinedStrings(2);
            }
            api.modify(AccountType.PLAYER, target_id, amount, BalanceSource.ADMIN_COMMAND, detail);
            sender.sendMessage(Colors.Chat.SUCCESS + "Der Spieler Account von '" + target_name + "' wurde mit "
                    + api.getFormattedAmount(round(amount)) + Colors.Chat.SUCCESS + " begünstigt!");
        }

        @Command(
                aliases = {"take"},
                desc = "ADMIN: Take money from a player",
                usage = "<Spieler> <Betrag> [FlowDetails]",
                min = 2
        )
        @CommandPermissions("rceconomy.admin")
        public void take(CommandContext context, CommandSender sender) throws CommandException {

            String target_name = context.getString(0);
            String target_id = UUIDUtil.getUUIDStringFromName(target_name);
            double amount = context.getDouble(1);

            if (amount <= 0.0) {
                throw new CommandException("Der Betrag muss positiv und mindestens 1 Kuper sein.");
            }

            if (!api.accountExists(AccountType.PLAYER, target_id)) {
                throw new CommandException("Der Bank Account '" + target_name + "' existiert nicht!");
            }

            String detail = null;
            if (context.argsLength() > 2) {
                detail = context.getJoinedStrings(2);
            }
            api.modify(AccountType.PLAYER, target_id, -amount, BalanceSource.ADMIN_COMMAND, detail);
            sender.sendMessage(Colors.Chat.SUCCESS + "Der Spieler Account von '" + target_name + "' wurde mit "
                    + api.getFormattedAmount(round(amount)) + Colors.Chat.SUCCESS + " belastet!");
        }

        @Command(
                aliases = {"set"},
                desc = "ADMIN: set money of a player",
                usage = "<Spieler> <Betrag> [FlowDetails]",
                min = 2
        )
        @CommandPermissions("rceconomy.admin")
        public void set(CommandContext context, CommandSender sender) throws CommandException {

            String target_name = context.getString(0);
            String target_id = UUIDUtil.getUUIDStringFromName(target_name);
            double amount = context.getDouble(1);

            if (!api.accountExists(AccountType.PLAYER, target_id)) {
                throw new CommandException("Der Bank Account '" + target_name + "' existiert nicht!");
            }

            String detail = null;
            if (context.argsLength() > 2) {
                detail = context.getJoinedStrings(2);
            }
            api.set(AccountType.PLAYER, target_id, amount, BalanceSource.ADMIN_COMMAND, detail);
            sender.sendMessage(Colors.Chat.SUCCESS + "Der Spieler Account von '" + target_name + "' wurde auf "
                    + api.getFormattedAmount(round(amount)) + Colors.Chat.SUCCESS + " gesetzt!");
        }

        private double round(double d) {

            return Math.round(d * 100) / 100.0;
        }
    }
}

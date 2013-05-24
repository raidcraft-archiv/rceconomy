package de.raidcraft.rceconomy.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.RaidCraft;
import de.raidcraft.rceconomy.RCEconomyPlugin;
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

        //TODO implement
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

            //TODO implement
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

            //TODO implement
        }

        @Command(
                aliases = {"take"},
                desc = "Take"
        )
        @CommandPermissions("rceconomy.admin")
        public void take(CommandContext context, CommandSender sender) throws CommandException {

            //TODO implement
        }

        @Command(
                aliases = {"set"},
                desc = "Take"
        )
        @CommandPermissions("rceconomy.admin")
        public void set(CommandContext context, CommandSender sender) throws CommandException {

            //TODO implement
        }
    }
}

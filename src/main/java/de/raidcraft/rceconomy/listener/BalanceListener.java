package de.raidcraft.rceconomy.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceChangeEvent;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Philip Urban
 */
public class BalanceListener implements Listener {

    @EventHandler
    public void onBalanceChange(BalanceChangeEvent event) {

        Player player = Bukkit.getPlayer(event.getAccountName());
        if(player == null) {
            return;
        }

        RCEconomyPlugin plugin = RaidCraft.getComponent(RCEconomyPlugin.class);

        player.sendMessage(ChatColor.GREEN + "Kontobewegung: " + plugin.getFormattedAmount(event.getAmount())
                + ChatColor.GREEN + " Grund: " + ChatColor.YELLOW + event.getSource().getFriendlyName());
        if(event.getDetail() != null && event.getDetail().length() > 0) {
            player.sendMessage(ChatColor.GREEN + "Details: " + ChatColor.WHITE + event.getDetail());
        }
    }
}

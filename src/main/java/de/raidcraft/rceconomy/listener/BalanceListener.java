package de.raidcraft.rceconomy.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.api.economy.BalanceChangeEvent;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * @author Philip Urban
 */
public class BalanceListener implements Listener {

    private RCEconomyPlugin plugin = RaidCraft.getComponent(RCEconomyPlugin.class);

    @EventHandler
    public void onBalanceChange(BalanceChangeEvent event) {

        if (event.getType() != AccountType.PLAYER) {
            return;
        }
        Player player = Bukkit.getPlayer(UUID.fromString(event.getAccountName()));
        if (player == null || event.getAmount() == 0.0 || (int) (event.getAmount() * 100) == 0) {
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Kontobewegung: " + plugin.getApi().getFormattedAmount(event.getAmount())
                + ChatColor.GREEN + " Grund: " + ChatColor.YELLOW + event.getSource().getFriendlyName());
        if (event.getDetail() != null && event.getDetail().length() > 0) {
            player.sendMessage(ChatColor.GREEN + "Details: " + ChatColor.WHITE + event.getDetail());
        }
    }
}

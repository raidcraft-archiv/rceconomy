package de.raidcraft.rceconomy.listener;

import com.google.common.base.Strings;
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

        if (!plugin.getEconomyConfig().balanceChangeEnabled) {
            return;
        }
        if (event.getType() != AccountType.PLAYER) {
            return;
        }
        Player player = Bukkit.getPlayer(UUID.fromString(event.getAccountName()));
        if (player == null || event.getAmount() == 0.0 || (int) (event.getAmount() * 100) == 0) {
            return;
        }

        String message = plugin.getEconomyConfig().balanceChangeText
                .replace("%amount%", event.getAmount() + "")
                .replace("%formatted-amount%", plugin.getApi().getFormattedAmount(event.getAmount()))
                .replace("%reason%", event.getSource().getFriendlyName())
                .replace("%player%", player.getDisplayName());
        player.sendMessage(message);
        if (event.getDetail() != null && event.getDetail().length() > 0 && !Strings.isNullOrEmpty(plugin.getEconomyConfig().balanceDetailsText)) {
            player.sendMessage(plugin.getEconomyConfig().balanceDetailsText
                    .replace("%details%", event.getDetail())
                    .replace("%amount%", event.getAmount() + "")
                    .replace("%formatted-amount%", plugin.getApi().getFormattedAmount(event.getAmount()))
                    .replace("%reason%", event.getSource().getFriendlyName())
                    .replace("%player%", player.getDisplayName()));
        }
    }
}

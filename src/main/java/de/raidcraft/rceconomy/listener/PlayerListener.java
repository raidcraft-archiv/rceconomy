package de.raidcraft.rceconomy.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * @author Philip Urban
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {

        RCEconomyPlugin plugin = RaidCraft.getComponent(RCEconomyPlugin.class);
        if (!plugin.getApi().accountExists(
                AccountType.PLAYER, event.getPlayer().getUniqueId().toString())) {
            plugin.getApi().createAccount(
                    AccountType.PLAYER, event.getPlayer().getUniqueId().toString());
        }
    }
}

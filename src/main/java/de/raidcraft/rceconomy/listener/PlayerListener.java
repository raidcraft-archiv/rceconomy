package de.raidcraft.rceconomy.listener;

import com.avaje.ebean.EbeanServer;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.tables.TAccount;
import de.raidcraft.util.PlayerExperienceUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

        Bukkit.getScheduler().runTaskLater(RaidCraft.getComponent(RCEconomyPlugin.class), new Runnable() {
            @Override
            public void run() {
                // Check if player has EXP in bank
                Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
                if(player == null) return;

                EbeanServer database = RaidCraft.getComponent(RCEconomyPlugin.class).getDatabase();
                TAccount tAccount = database.find(TAccount.class).where()
                        .eq("name", player.getUniqueId().toString().toLowerCase()).findUnique();
                if(tAccount.getExp() != 0) {
                    PlayerExperienceUtil ownerExpUtil = new PlayerExperienceUtil(player);
                    ownerExpUtil.setExp(ownerExpUtil.getCurrentExp() + tAccount.getExp());
                    player.sendMessage(ChatColor.GREEN + "Dir wurden seit dem letzten Besuch " + tAccount.getExp() + "EXP verkauft!");
                    tAccount.setExp(0);
                    database.update(tAccount);
                }
            }
        }, 10);
    }
}

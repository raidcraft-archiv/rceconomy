package de.raidcraft.rceconomy.actionapi;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.api.economy.BalanceSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author Philip Urban
 */
// TODO: needed?
public class ModifyMoneyAction implements Action<Player> {

    @Override
    @Information(
            value = "playermoney.modify",
            desc = "Modifies the player money.",
            conf = {"delta", "flowdetail"}
    )
    public void accept(Player player, ConfigurationSection config) {
        String details = config.getString("flowdetail", "Conversation");
        double amount = config.getDouble("delta", 0.0);
        RaidCraft.getEconomy().modify(AccountType.PLAYER, player.getUniqueId().toString(),
                amount, BalanceSource.PLUGIN, details);
    }
}

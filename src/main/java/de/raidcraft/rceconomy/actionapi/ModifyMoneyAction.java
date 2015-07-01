package de.raidcraft.rceconomy.actionapi;

import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author Philip Urban
 */
// TODO: needed?
public class ModifyMoneyAction implements Action<Player> {
    private Economy eco;

    public ModifyMoneyAction(Economy eco) {

        this.eco = eco;
    }

    @Override
    @Information(
            value = "playermoney.modify",
            desc = "Modifies the player money.",
            conf = {"delta", "flowdetail"}
    )
    public void accept(Player player, ConfigurationSection config) {
        String details = config.getString("flowdetail", "Conversation");
        double amount = config.getDouble("delta", 0.0);
        eco.modify(AccountType.PLAYER, player.getUniqueId().toString(),
                amount, BalanceSource.PLUGIN, details);
    }
}

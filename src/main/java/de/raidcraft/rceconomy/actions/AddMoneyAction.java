package de.raidcraft.rceconomy.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
public class AddMoneyAction implements Action<Player> {

    @Override
    @Information(
            value = "player.money.add",
            desc = "Adds money the player account.",
            conf = {
                    "amount: <1g2s33k>",
                    "details: [optional transaction details for the logs]"
            },
            aliases = {"money.add", "playermoney.add"}
    )
    public void accept(Player player, ConfigurationSection config) {

        Economy economy = RaidCraft.getEconomy();
        double amount = economy.parseCurrencyInput(config.getString("amount"));
        if (config.isSet("details")) {
            economy.add(player.getUniqueId(), amount, BalanceSource.PLUGIN, config.getString("details"));
        } else {
            economy.add(player.getUniqueId(), amount);
        }
    }
}

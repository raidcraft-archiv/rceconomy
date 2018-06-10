package de.raidcraft.rceconomy.requirements;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.economy.Economy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
public class HasEnoughMoneyRequirement implements Requirement<Player> {

    @Override
    @Information(
            value = "player.money.has",
            desc = "Checks if the player has enough money.",
            conf = {"amount: <1g2s33k>"},
            aliases = {"HAS_ENOUGH_MONEY", "player.has-enough-money", "player.money.has-enough"}
    )
    public boolean test(Player player, ConfigurationSection config) {

        Economy economy = RaidCraft.getEconomy();
        return economy.hasEnough(player.getUniqueId(), economy.parseCurrencyInput(config.getString("amount")));
    }
}

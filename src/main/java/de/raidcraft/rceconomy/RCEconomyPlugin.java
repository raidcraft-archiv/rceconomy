package de.raidcraft.rceconomy;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.rceconomy.commands.MoneyCommands;
import de.raidcraft.rceconomy.tables.BalanceTable;

/**
 * @author Philip Urban
 */
public class RCEconomyPlugin extends BasePlugin {

    @Override
    public void enable() {

        registerTable(BalanceTable.class, new BalanceTable());
        registerCommands(MoneyCommands.class);
    }

    @Override
    public void disable() {

    }
}

package de.raidcraft.rceconomy;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.rceconomy.commands.MoneyCommands;
import de.raidcraft.rceconomy.tables.BalanceTable;

/**
 * @author Philip Urban
 */
public class RCEconomyPlugin extends BasePlugin {

    LocalConfiguration config;

    @Override
    public void enable() {

        registerTable(BalanceTable.class, new BalanceTable());
        registerCommands(MoneyCommands.class);

        reload();
    }

    @Override
    public void disable() {

    }

    @Override
    public void reload() {

        config = configure(new LocalConfiguration(this));
    }

    public class LocalConfiguration extends ConfigurationBase<RCEconomyPlugin> {

        @Setting("initial-amount")
        public double initialAmount = 10.0;

        public LocalConfiguration(RCEconomyPlugin plugin) {

            super(plugin, "config.yml");
        }
    }

    public void createAccount(String accountName) {

        //TODO implement
    }

    private void deleteAccount(String accountName) {

        //TODO implement
    }

    public double getBalance(String accountName) {

        //TODO implement
    }

    public String getFormattedBalance(String accountName) {

        //TODO implement
    }

    public boolean hasEnough(String accountName, double amount) {

        //TODO implement
    }

    public void withdraw(String accountName, double amount) {

        //TODO implement
    }

    public void deposit(String accountName, double amount) {

        //TODO implement
    }
}

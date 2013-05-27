package de.raidcraft.rceconomy;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.rceconomy.commands.MoneyCommands;
import de.raidcraft.rceconomy.listener.PlayerListener;
import de.raidcraft.rceconomy.tables.BalanceTable;
import de.raidcraft.util.CustomItemUtil;

/**
 * @author Philip Urban
 */
public class RCEconomyPlugin extends BasePlugin {

    private LocalConfiguration config;

    @Override
    public void enable() {

        registerTable(BalanceTable.class, new BalanceTable());
        registerCommands(MoneyCommands.class);
        registerEvents(new PlayerListener());

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

        RaidCraft.getTable(BalanceTable.class).set(accountName, config.initialAmount);
    }

    public void deleteAccount(String accountName) {

        RaidCraft.getTable(BalanceTable.class).deleteAccount(accountName);
    }

    public boolean accountExists(String accountName) {

        return RaidCraft.getTable(BalanceTable.class).exists(accountName);
    }

    public double getBalance(String accountName) {

        return RaidCraft.getTable(BalanceTable.class).getBalance(accountName);
    }

    public String getFormattedBalance(String accountName) {

        double balance = RaidCraft.getTable(BalanceTable.class).getBalance(accountName);
        return CustomItemUtil.getSellPriceString(balance);
    }

    public String getFormattedAmount(double amount) {

        return CustomItemUtil.getSellPriceString(amount);
    }

    public boolean hasEnough(String accountName, double amount) {

        double balance = RaidCraft.getTable(BalanceTable.class).getBalance(accountName);
        if(balance >= amount) {
            return true;
        }
        return false;
    }

    public void modify(String accountName, double amount) {

        RaidCraft.getTable(BalanceTable.class).modify(accountName, amount);
    }

    public void set(String accountName, double amount) {

        RaidCraft.getTable(BalanceTable.class).set(accountName, amount);
    }
}

package de.raidcraft.rceconomy;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rceconomy.commands.MoneyCommands;
import de.raidcraft.rceconomy.listener.BalanceListener;
import de.raidcraft.rceconomy.listener.PlayerListener;
import de.raidcraft.rceconomy.tables.BalanceTable;
import de.raidcraft.rceconomy.tables.FlowTable;
import de.raidcraft.util.CustomItemUtil;

/**
 * @author Philip Urban
 */
public class RCEconomyPlugin extends BasePlugin implements Economy {

    private LocalConfiguration config;

    @Override
    public void enable() {

        registerTable(BalanceTable.class, new BalanceTable());
        registerTable(FlowTable.class, new FlowTable());
        registerCommands(MoneyCommands.class);
        registerEvents(new PlayerListener());
        registerEvents(new BalanceListener());
        RaidCraft.setupEconomy(this);
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
        public double initialAmount = 0.0;
        @Setting("currency-name-singular")
        public String currencySingular = "Coin";
        @Setting("currency-name-plural")
        public String currencyPlural = "Coins";

        public LocalConfiguration(RCEconomyPlugin plugin) {

            super(plugin, "config.yml");
        }
    }

    @Override
    public void createAccount(String accountName) {

        RaidCraft.getTable(BalanceTable.class).createAccount(accountName);
        RaidCraft.getTable(BalanceTable.class).set(accountName, config.initialAmount);
    }

    @Override
    public void deleteAccount(String accountName) {

        RaidCraft.getTable(BalanceTable.class).deleteAccount(accountName);
    }

    @Override
    public boolean accountExists(String accountName) {

        return RaidCraft.getTable(BalanceTable.class).exists(accountName);
    }

    @Override
    public double getBalance(String accountName) {

        return RaidCraft.getTable(BalanceTable.class).getBalance(accountName);
    }

    @Override
    public String getFormattedBalance(String accountName) {

        double balance = RaidCraft.getTable(BalanceTable.class).getBalance(accountName);
        return CustomItemUtil.getSellPriceString(balance);
    }

    @Override
    public String getFormattedAmount(double amount) {

        return CustomItemUtil.getSellPriceString(amount);
    }

    @Override
    public boolean hasEnough(String accountName, double amount) {

        double balance = RaidCraft.getTable(BalanceTable.class).getBalance(accountName);
        if(balance >= amount) {
            return true;
        }
        return false;
    }

    @Override
    public void modify(String accountName, double amount) {

        if(amount == 0) return;
        FlowManager.addActivity(accountName, amount, BalanceSource.PLUGIN, null);
        RaidCraft.getTable(BalanceTable.class).modify(accountName, amount);
    }

    @Override
    public void modify(String accountName, double amount, BalanceSource source, String detail) {

        if(amount == 0) return;
        FlowManager.addActivity(accountName, amount, source, detail);
        RaidCraft.getTable(BalanceTable.class).modify(accountName, amount);
    }

    @Override
    public void substract(String accountName, double amount) {

        modify(accountName, -Math.abs(amount));
    }

    @Override
    public void substract(String accountName, double amount, BalanceSource source, String detail) {

        modify(accountName, -Math.abs(amount), source, detail);
    }

    @Override
    public void add(String accountName, double amount) {

        modify(accountName, Math.abs(amount));
    }

    @Override
    public void add(String accountName, double amount, BalanceSource source, String detail) {

        modify(accountName, Math.abs(amount), source, detail);
    }

    @Override
    public void set(String accountName, double amount) {

        if(amount == 0) return;
        FlowManager.addActivity(accountName, amount, BalanceSource.PLUGIN, null);
        RaidCraft.getTable(BalanceTable.class).set(accountName, amount);
    }

    @Override
    public void set(String accountName, double amount, BalanceSource source, String detail) {

        if(amount == 0) return;
        FlowManager.addActivity(accountName, amount, source, detail);
        RaidCraft.getTable(BalanceTable.class).set(accountName, amount);
    }

    public String getCurrencyNameSingular() {

        return config.currencySingular;
    }

    public String getCurrencyNamePlural() {

        return config.currencyPlural;
    }
}

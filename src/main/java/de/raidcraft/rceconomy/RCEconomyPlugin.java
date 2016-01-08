package de.raidcraft.rceconomy;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rcconversations.actions.ActionManager;
import de.raidcraft.rceconomy.actionapi.HasEnoughMoneyAction;
import de.raidcraft.rceconomy.actionapi.ModifyMoneyAction;
import de.raidcraft.rceconomy.actionapi.ParseMoneyInputAction;
import de.raidcraft.rceconomy.actionapi.SubstractMoneyAction;
import de.raidcraft.rceconomy.bankchest.BankChestListener;
import de.raidcraft.rceconomy.commands.MoneyCommands;
import de.raidcraft.rceconomy.listener.BalanceListener;
import de.raidcraft.rceconomy.listener.BankSign;
import de.raidcraft.rceconomy.listener.PlayerListener;
import de.raidcraft.rceconomy.tables.TAccount;
import de.raidcraft.rceconomy.tables.TBankChest;
import de.raidcraft.rceconomy.tables.TBankMaterial;
import de.raidcraft.rceconomy.tables.TFlow;
import lombok.Getter;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Urban
 */
public class RCEconomyPlugin extends BasePlugin {

    LocalConfiguration config;
    @Getter
    private EconomyConfig economyConfig;
    @Getter
    private Economy api;

    @Override
    public void enable() {

        economyConfig = configure(new EconomyConfig(this));
        setupDatabase();

        api = new RcEconomy(this, economyConfig);
        RaidCraft.setupEconomy(api);

        registerCommands(MoneyCommands.class);
        registerEvents(new PlayerListener());
        registerEvents(new BalanceListener());
        registerEvents(new BankSign());
        registerEvents(new BankChestListener());

        ActionAPI.register(this)
                .action(new ModifyMoneyAction());

        ActionManager.registerAction(new HasEnoughMoneyAction());
        ActionManager.registerAction(new ParseMoneyInputAction());
        ActionManager.registerAction(new SubstractMoneyAction());

        // inject Vault
        new VaultEco(this);
    }

    @Override
    public void disable() {
        //nothing
    }

    public TAccount getAccount(AccountType type, String name) {

        return getDatabase().find(TAccount.class)
                .where()
                .eq("type", type)
                .eq("name", name).setMaxRows(1).findUnique();
    }


    @Override
    public List<Class<?>> getDatabaseClasses() {

        List<Class<?>> tables = new ArrayList<>();
        tables.add(TAccount.class);
        tables.add(TFlow.class);
        tables.add(TBankChest.class);
        tables.add(TBankMaterial.class);
        return tables;
    }

    private void setupDatabase() {

        try {
            getDatabase();
        } catch (PersistenceException e) {
            e.printStackTrace();
            getLogger().warning("Installing database for " + getDescription().getName() + " due to first time usage");
            installDDL();
        }
    }

    @Override
    public LocalConfiguration getConfig() {
        return config;
    }

    public class LocalConfiguration extends ConfigurationBase<RCEconomyPlugin> {

        @Setting("bank-chest-delay-hours")
        public int bankChestDelayHours = 168;

        public LocalConfiguration(RCEconomyPlugin plugin) {

            super(plugin, "config.yml");
        }
    }
}
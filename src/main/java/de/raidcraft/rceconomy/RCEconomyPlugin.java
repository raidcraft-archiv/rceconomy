package de.raidcraft.rceconomy;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rcconversations.actions.ActionManager;
import de.raidcraft.rceconomy.commands.MoneyCommands;
import de.raidcraft.rceconomy.conversations.HasEnoughMoneyAction;
import de.raidcraft.rceconomy.conversations.ParseMoneyInputAction;
import de.raidcraft.rceconomy.conversations.SubstractMoneyAction;
import de.raidcraft.rceconomy.listener.BalanceListener;
import de.raidcraft.rceconomy.listener.PlayerListener;
import de.raidcraft.rceconomy.tables.TAccount;
import de.raidcraft.rceconomy.tables.TFlow;
import lombok.Getter;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Urban
 */
public class RCEconomyPlugin extends BasePlugin {

    private EconomyConfig config;
    @Getter
    private Economy api;

    @Override
    public void enable() {

        config = configure(new EconomyConfig(this));
        setupDatabase();

        api = new RcEconomy(this, config);
        RaidCraft.setupEconomy(api);

        registerCommands(MoneyCommands.class, getName());
        registerEvents(new PlayerListener());
        registerEvents(new BalanceListener());

        // TODO: use new system
        ActionManager.registerAction(new HasEnoughMoneyAction());
        ActionManager.registerAction(new SubstractMoneyAction());
        ActionManager.registerAction(new ParseMoneyInputAction());
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
}
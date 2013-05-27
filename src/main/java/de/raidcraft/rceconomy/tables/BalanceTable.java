package de.raidcraft.rceconomy.tables;

import de.raidcraft.api.database.Table;

/**
 * @author Philip Urban
 */
public class BalanceTable extends Table {

    public BalanceTable() {

        super("balance", "rceconomy_");
    }

    @Override
    public void createTable() {

    }

    public void modify(String accountName, double amount) {

        //TODO implement
    }

    public void set(String accountName, double amount) {

        //TODO implement
    }

    public double getBalance(String accountName) {

        //TODO implement
    }
}

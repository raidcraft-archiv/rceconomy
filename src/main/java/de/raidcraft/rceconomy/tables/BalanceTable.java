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
}

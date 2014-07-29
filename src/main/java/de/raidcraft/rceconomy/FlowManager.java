package de.raidcraft.rceconomy;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceChangeEvent;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.rceconomy.tables.FlowTable;

/**
 * @author Philip Urban
 */
public class FlowManager {

    public static void addActivity(String accountName, double amount, BalanceSource source, String detail) {

        RaidCraft.getTable(FlowTable.class).addEntry(accountName, amount, source, detail);
        RaidCraft.callEvent(new BalanceChangeEvent(source, detail, accountName, amount));
    }
}

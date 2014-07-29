package de.raidcraft.rceconomy.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.rceconomy.BankActivity;
import de.raidcraft.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Urban
 */
public class FlowTable extends Table {

    public FlowTable() {

        super("flow", "rceconomy_");
    }

    @Override
    public void createTable() {

        try {
            executeUpdate(
                    "CREATE TABLE `" + getTableName() + "` (" +
                            "`id` INT NOT NULL AUTO_INCREMENT, " +
                            "`name` VARCHAR( 64 ) NOT NULL, " +
                            "`amount` DOUBLE( 64,2 ) NOT NULL, " +
                            "`source` TEXT NOT NULL, " +
                            "`detail` TEXT, " +
                            "`date` VARCHAR( 64 ) NOT NULL, " +
                            "PRIMARY KEY ( `id` )" +
                            ")");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
    }

    public void addEntry(String name, double amount, BalanceSource source, String detail) {

        if(detail == null) detail = "";
        name = name.toLowerCase();

        try {
            executeUpdate("INSERT INTO " + getTableName() + " (name, amount, source, detail, date) " +
                    "VALUES (" +
                    "'" + name + "'" + "," +
                    "'" + amount + "'" + "," +
                    "'" + source.name() + "'" + "," +
                    "'" + detail + "'" + "," +
                    "'" + DateUtil.getCurrentDateString() + "'" +
                    ");");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
    }

    public List<BankActivity> getActivity(String accountName, int number) {

        accountName = accountName.toLowerCase();
        List<BankActivity> activities = new ArrayList<>();

        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName() + " WHERE name = '" + accountName + "' ORDER BY id DESC LIMIT 0," + number + ";");

            while (resultSet.next()) {

                BankActivity activity = new BankActivity(
                        resultSet.getString("name"),
                        resultSet.getDouble("amount"),
                        resultSet.getString("date"),
                        BalanceSource.valueOf(resultSet.getString("source")),
                        resultSet.getString("detail")
                );
                activities.add(activity);
            }
            resultSet.close();
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
        return activities;
    }
}

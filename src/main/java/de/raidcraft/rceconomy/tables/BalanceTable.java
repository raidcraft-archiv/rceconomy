package de.raidcraft.rceconomy.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Philip Urban
 */
public class BalanceTable extends Table {

    public BalanceTable() {

        super("balance", "rceconomy_");
    }

    @Override
    public void createTable() {

        try {
            executeUpdate(
                    "CREATE TABLE `" + getTableName() + "` (" +
                            "`id` INT NOT NULL AUTO_INCREMENT, " +
                            "`name` VARCHAR( 64 ) NOT NULL, " +
                            "`balance` DOUBLE( 64,10 ) NOT NULL, " +
                            "PRIMARY KEY ( `id` )" +
                            ")");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
    }

    public boolean exists(String accountName) {

        accountName = accountName.toLowerCase();
        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName() + " WHERE name = '" + accountName + "';");

            if (resultSet.next()) {
                resultSet.close();
                return true;
            }
            resultSet.close();
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
        return false;
    }

    public void modify(String accountName, double amount) {

        double newBalance = getBalance(accountName) + amount;
        try {
            executeUpdate("UPDATE " + getTableName() + " SET balance = '" + newBalance + "' WHERE name = '" + accountName + "'");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    public void set(String accountName, double amount) {

        try {
            executeUpdate("UPDATE " + getTableName() + " SET balance = '" + amount + "' WHERE name = '" + accountName + "'");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    public double getBalance(String accountName) {

        accountName = accountName.toLowerCase();
        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName() + " WHERE name = '" + accountName + "';");

            if (resultSet.next()) {
                double balance = resultSet.getDouble("balance");
                resultSet.close();
                return balance;
            }
            resultSet.close();
            createAccount(accountName);
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
        return 0;
    }

    public void createAccount(String accountName) {

        try {
            executeUpdate("INSERT INTO " + getTableName() + " (name, balance) " +
                    "VALUES (" +
                    "'" + accountName.toLowerCase() + "'" + "," +
                    "'" + 0 + "'" +
                    ");");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
    }

    public void deleteAccount(String accountName) {

        accountName = accountName.toLowerCase();
        try {
            executeUpdate("DELETE FROM " + getTableName() + " WHERE name = '" + accountName + "'");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }
}

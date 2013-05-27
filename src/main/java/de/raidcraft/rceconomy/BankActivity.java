package de.raidcraft.rceconomy;

import de.raidcraft.api.economy.BalanceSource;

/**
 * @author Philip Urban
 */
public class BankActivity {

    private String accountName;
    private double amount;
    private String date;
    private BalanceSource source;
    private String detail;

    public BankActivity(String accountName, double amount, String date, BalanceSource source, String detail) {

        this.accountName = accountName;
        this.amount = amount;
        this.date = date;
        this.source = source;
        this.detail = detail;
    }

    public String getAccountName() {

        return accountName;
    }

    public double getAmount() {

        return amount;
    }

    public String getDate() {

        return date;
    }

    public BalanceSource getSource() {

        return source;
    }

    public String getDetail() {

        return detail;
    }
}

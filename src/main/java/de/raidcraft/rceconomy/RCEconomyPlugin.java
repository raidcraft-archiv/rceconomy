package de.raidcraft.rceconomy;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rcconversations.actions.ActionManager;
import de.raidcraft.rceconomy.commands.MoneyCommands;
import de.raidcraft.rceconomy.conversations.HasEnoughMoneyAction;
import de.raidcraft.rceconomy.conversations.ParseMoneyInputAction;
import de.raidcraft.rceconomy.conversations.SubstractMoneyAction;
import de.raidcraft.rceconomy.listener.BalanceListener;
import de.raidcraft.rceconomy.listener.PlayerListener;
import de.raidcraft.rceconomy.tables.BalanceTable;
import de.raidcraft.rceconomy.tables.FlowTable;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Philip Urban
 */
public class RCEconomyPlugin extends BasePlugin implements Economy {

    // group 2 = gold, group 4 = silver, group 6 = copper
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^((\\d+)[gG])?\\s?((\\d+)[sS])?\\s?((\\d+)[cCkK]?)?$");
    private LocalConfiguration config;

    @Override
    public void enable() {

        registerTable(BalanceTable.class, new BalanceTable());
        registerTable(FlowTable.class, new FlowTable());
        registerCommands(MoneyCommands.class);
        registerEvents(new PlayerListener());
        registerEvents(new BalanceListener());
        RaidCraft.setupEconomy(this);

        ActionManager.registerAction(new HasEnoughMoneyAction());
        ActionManager.registerAction(new SubstractMoneyAction());
        ActionManager.registerAction(new ParseMoneyInputAction());

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
    public double parseCurrencyInput(String input) {

        // lets parse the string for the different money values
        input = ChatColor.stripColor(input).replace("●", "");
        Matcher matcher = CURRENCY_PATTERN.matcher(input);
        double value = 0.0;
        if (matcher.matches()) {
            // lets grap the different groups and check for input
            // group 2 = gold
            // group 4 = silver
            // group 6 = copper
            if (matcher.group(2) != null) {
                value += 100 * Integer.parseInt(matcher.group(2));
            }
            if (matcher.group(4) != null) {
                value += Integer.parseInt(matcher.group(4));
            }
            if (matcher.group(6) != null) {
                value += Integer.parseInt(matcher.group(6)) / 100.0;
            }
        }
        return value;
    }

    @Override
    public boolean hasEnough(String accountName, double amount) {

        double balance = RaidCraft.getTable(BalanceTable.class).getBalance(accountName);
        return balance >= amount;
    }

    @Override
    public void substract(String accountName, double amount) {

        modify(accountName, -amount);
    }

    @Override
    public void substract(String accountName, double amount, BalanceSource source, String detail) {

        modify(accountName, -amount, source, detail);
    }

    @Override
    public void add(String accountName, double amount) {

        modify(accountName, amount);
    }

    @Override
    public void add(String accountName, double amount, BalanceSource source, String detail) {

        modify(accountName, amount, source, detail);
    }

    @Override
    public void modify(String accountName, double amount) {

        if(amount == 0.0) return;
        FlowManager.addActivity(accountName, amount, BalanceSource.PLUGIN, null);
        RaidCraft.getTable(BalanceTable.class).modify(accountName, amount);
    }

    @Override
    public void modify(String accountName, double amount, BalanceSource source, String detail) {

        if(amount == 0.0) return;
        FlowManager.addActivity(accountName, amount, source, detail);
        RaidCraft.getTable(BalanceTable.class).modify(accountName, amount);
    }

    @Override
    public void set(String accountName, double amount) {

        FlowManager.addActivity(accountName, amount, BalanceSource.PLUGIN, null);
        RaidCraft.getTable(BalanceTable.class).set(accountName, amount);
    }

    @Override
    public void set(String accountName, double amount, BalanceSource source, String detail) {

        FlowManager.addActivity(accountName, amount, source, detail);
        RaidCraft.getTable(BalanceTable.class).set(accountName, amount);
    }

    @Override
    public void printFlow(CommandSender sender, String accountName, int entries) {

        List<BankActivity> activities = RaidCraft.getTable(FlowTable.class).getActivity(accountName, entries);

        sender.sendMessage(ChatColor.GREEN + "Die letzten Kontobewegungen von " + ChatColor.YELLOW + accountName + ChatColor.GREEN + ":");
        String detail;
        for(BankActivity activity : activities) {
            if(activity.getDetail() != null && activity.getDetail().length() > 0) {
                detail = activity.getDetail();
            }
            else {
                detail = "";
            }
            sender.sendMessage(getFormattedAmount(activity.getAmount()) + ChatColor.WHITE + " "
                    + activity.getSource().getFriendlyName() + ChatColor.YELLOW + " " + activity.getDate() + ChatColor.WHITE + " " + detail);
        }
    }

    public String getCurrencyNameSingular() {

        return config.currencySingular;
    }

    public String getCurrencyNamePlural() {

        return config.currencyPlural;
    }
}

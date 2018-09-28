package de.raidcraft.rceconomy;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.AccountType;
import de.raidcraft.api.economy.BalanceChangeEvent;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rceconomy.tables.TAccount;
import de.raidcraft.rceconomy.tables.TFlow;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.UUIDUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dragonfire
 */
public class RcEconomy implements Economy {

    private RCEconomyPlugin plugin;
    // group 2 = gold, group 4 = silver, group 6 = copper
    private static final Pattern CURRENCY_PATTERN =
            Pattern.compile("^((\\d+)[gG])?\\s?((\\d+)[sS])?\\s?((\\d+)[cCkK]?)?$");
    private EconomyConfig config;


    public RcEconomy(RCEconomyPlugin plugin, EconomyConfig config) {

        this.plugin = plugin;
        this.config = config;
    }


    public String getCurrencyNameSingular() {

        return config.currencySingular;
    }

    public String getCurrencyNamePlural() {

        return config.currencyPlural;
    }

    @Override
    public void createAccount(UUID player) {

        createAccount(AccountType.PLAYER, player.toString());
    }

    @Override
    public void createAccount(AccountType type, String accountName) {

        TAccount account = new TAccount();
        account.setName(accountName);
        account.setType(type);
        account.setBalance(config.initialAmount);
        plugin.getRcDatabase().save(account);
    }

    @Override
    public void deleteAccount(UUID player) {

        deleteAccount(AccountType.PLAYER, player.toString());
    }

    @Override
    public void deleteAccount(AccountType type, String accountName) {

        plugin.getRcDatabase().delete(plugin.getAccount(type, accountName));
    }

    @Override
    public boolean accountExists(UUID player) {

        return accountExists(AccountType.PLAYER, player.toString());
    }

    @Override
    public boolean accountExists(AccountType type, String accountName) {

        return plugin.getAccount(type, accountName) != null;
    }

    @Override
    public double getBalance(UUID player) {

        return getBalance(AccountType.PLAYER, player.toString());
    }

    @Override
    public double getBalance(AccountType type, String accountName) {

        TAccount acc = plugin.getAccount(type, accountName);
        return acc.getBalance();
    }

    @Override
    public String getFormattedBalance(UUID player) {

        return getFormattedBalance(AccountType.PLAYER, player.toString());
    }

    @Override
    public String getFormattedBalance(AccountType type, String accountName) {

        return CustomItemUtil.getSellPriceString(getBalance(type, accountName));
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
                value += 100. * Integer.parseInt(matcher.group(2));
            }
            if (matcher.group(4) != null) {
                value += Integer.parseInt(matcher.group(4));
            }
            if (matcher.group(6) != null) {
                value += Integer.parseInt(matcher.group(6)) / 100.;
            }
        }
        return value;
    }

    @Override
    public boolean hasEnough(UUID player, double amount) {

        return hasEnough(AccountType.PLAYER, player.toString(), amount);
    }

    @Override
    public boolean hasEnough(AccountType type, String accountName, double amount) {

        double balance = getBalance(type, accountName);
        return amount <= 0 || balance >= amount;
    }

    @Override
    public void substract(UUID player, double amount) {

        substract(AccountType.PLAYER, player.toString(), amount);
    }

    @Override
    public void substract(AccountType type, String accountName, double amount) {

        modify(type, accountName, -amount);
    }

    @Override
    public void substract(UUID player, double amount, BalanceSource source, String detail) {

        substract(AccountType.PLAYER, player.toString(), amount, source, detail);
    }

    @Override
    public void substract(AccountType type, String accountName,
                          double amount, BalanceSource source, String detail) {

        modify(type, accountName, -amount, source, detail);
    }

    @Override
    public void add(UUID player, double amount) {

        add(AccountType.PLAYER, player.toString(), amount);
    }

    @Override
    public void add(AccountType type, String accountName, double amount) {

        modify(type, accountName, amount);
    }

    @Override
    public void add(UUID player, double amount, BalanceSource source, String detail) {

        add(AccountType.PLAYER, player.toString(), amount, source, detail);
    }

    @Override
    public void add(AccountType type, String accountName,
                    double amount, BalanceSource source, String detail) {

        modify(type, accountName, amount, source, detail);
    }

    @Override
    public void modify(UUID player, double amount) {

        modify(AccountType.PLAYER, player.toString(), amount);
    }

    @Override
    public void modify(AccountType type, String accountName, double amount) {

        modify(type, accountName, amount, BalanceSource.PLUGIN, null);
    }

    @Override
    public void modify(UUID player, double amount, BalanceSource source, String detail) {

        modify(AccountType.PLAYER, player.toString(), amount, source, detail);
    }

    @Override
    public void modify(AccountType type, String accountName,
                       double amount, BalanceSource source, String detail) {

        if (amount == 0.0) return;

        plugin.getRcDatabase().beginTransaction();
        try {
            TAccount account = plugin.getAccount(type, accountName);
            account.setBalance(account.getBalance() + amount);
            plugin.getRcDatabase().save(account);

            plugin.getRcDatabase().commitTransaction();
            addActivity(account, amount, source, detail);

        } finally {
            plugin.getRcDatabase().endTransaction();
        }
    }

    @Override
    public void set(UUID player, double amount) {

        set(AccountType.PLAYER, player.toString(), amount);
    }

    @Override
    public void set(AccountType type, String accountName, double amount) {

        set(type, accountName, amount, BalanceSource.PLUGIN, null);
    }

    @Override
    public void set(UUID player, double amount, BalanceSource source, String detail) {

        set(AccountType.PLAYER, player.toString(), amount, source, detail);
    }

    @Override
    public void set(AccountType type, String accountName,
                    double amount, BalanceSource source, String detail) {

        plugin.getRcDatabase().beginTransaction();
        try {
            TAccount account = plugin.getAccount(type, accountName);
            account.setBalance(amount);
            plugin.getRcDatabase().save(account);

            plugin.getRcDatabase().commitTransaction();
            addActivity(account, amount, source, detail);

        } finally {
            plugin.getRcDatabase().endTransaction();
        }
    }

    @Override
    public void printFlow(CommandSender sender, UUID playerId, int entries) {

        printFlow(sender, AccountType.PLAYER, playerId.toString(), entries);
    }


    @Override
    public void printFlow(CommandSender sender, AccountType type, String accountName, int entries) {

        TAccount account = plugin.getAccount(type, accountName);
        List<TFlow> activities = plugin.getRcDatabase().find(TFlow.class)
                .where()
                .eq("account", account).orderBy("id DESC").setMaxRows(entries).findList();
        if (type == AccountType.PLAYER) {
            accountName = UUIDUtil.getNameFromUUID(accountName);
        }
        sender.sendMessage(ChatColor.GREEN + "Die letzten Kontobewegungen von "
                + ChatColor.YELLOW + accountName + ChatColor.GREEN + ":");
        String detail;
        for (TFlow activity : activities) {
            if (activity.getDetail() != null && activity.getDetail().length() > 0) {
                detail = activity.getDetail();
            } else {
                detail = "";
            }
            sender.sendMessage(getFormattedAmount(activity.getAmount()) + ChatColor.WHITE + " "
                    + activity.getSource().getFriendlyName() + ChatColor.YELLOW + " "
                    + activity.getDate() + ChatColor.WHITE + " " + detail);
        }
    }

    public void addActivity(TAccount account, double amount,
                            BalanceSource source, String detail) {

        TFlow flow = new TFlow();
        flow.setAmount(amount);
        flow.setDate(new Date());
        flow.setSource(source);
        flow.setDetail(detail);
        flow.setAccount(account);
        plugin.getRcDatabase().save(flow);
        RaidCraft.callEvent(new BalanceChangeEvent(source, detail,
                account.getType(), account.getName(), amount));
    }

    // depracted
    @Override
    @Deprecated
    public boolean hasEnough(String playerName, double amount) {

        return hasEnough(UUIDUtil.convertPlayer(playerName), amount);
    }

    @Override
    @Deprecated
    public void add(String playerName, double amount) {

        add(UUIDUtil.convertPlayer(playerName), amount);
    }

    @Override
    @Deprecated
    public void add(String playerName, double amount, BalanceSource source, String detail) {

        add(UUIDUtil.convertPlayer(playerName), amount, source, detail);
    }

    @Override
    @Deprecated
    public void modify(String playerName, double amount) {

        modify(UUIDUtil.convertPlayer(playerName), amount);
    }

    @Override
    @Deprecated
    public void modify(String playerName, double amount, BalanceSource source, String detail) {

        modify(UUIDUtil.convertPlayer(playerName), amount, source, detail);
    }

    @Override
    @Deprecated
    public void set(String playerName, double amount) {

        set(UUIDUtil.convertPlayer(playerName), amount);
    }

    @Override
    @Deprecated
    public void set(String playerName, double amount, BalanceSource source, String detail) {

        set(UUIDUtil.convertPlayer(playerName), amount, source, detail);
    }

    @Override
    @Deprecated
    public void printFlow(CommandSender sender, String playerName, int entries) {

        printFlow(sender, UUIDUtil.convertPlayer(playerName), entries);
    }

    @Override
    @Deprecated
    public void substract(String playerName, double amount) {

        substract(UUIDUtil.convertPlayer(playerName), amount);
    }

    @Override
    @Deprecated
    public void substract(String playerName, double amount, BalanceSource source, String detail) {

        substract(UUIDUtil.convertPlayer(playerName), amount, source, detail);
    }

    @Override
    @Deprecated
    public void createAccount(String playerName) {

        createAccount(UUIDUtil.convertPlayer(playerName));
    }

    @Override
    @Deprecated
    public void deleteAccount(String playerName) {

        deleteAccount(UUIDUtil.convertPlayer(playerName));
    }

    @Override
    @Deprecated
    public boolean accountExists(String playerName) {

        return accountExists(UUIDUtil.convertPlayer(playerName));
    }

    @Override
    @Deprecated
    public double getBalance(String playerName) {

        return getBalance(UUIDUtil.convertPlayer(playerName));
    }

    @Override
    @Deprecated
    public String getFormattedBalance(String playerName) {

        return getFormattedBalance(UUIDUtil.convertPlayer(playerName));
    }
}

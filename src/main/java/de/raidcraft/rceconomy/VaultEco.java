package de.raidcraft.rceconomy;

import de.raidcraft.api.economy.AccountType;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.util.UUIDUtil;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.List;

/**
 * @author Dragonfire
 */
public class VaultEco implements net.milkbowl.vault.economy.Economy {

    private Economy eco;
    private RCEconomyPlugin plugin;

    public VaultEco(RCEconomyPlugin plugin) {

        this.plugin = plugin;
        this.eco = plugin.getApi();
        Plugin vPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vPlugin == null) {
            plugin.getLogger().severe("Vault not found - cannot inject");
            return;
        }
        hookIntoVault((Vault) vPlugin);
    }

    private void hookIntoVault(Vault vault) {

        try {
            Bukkit.getServicesManager().register(net.milkbowl.vault.economy.Economy.class,
                    this, vault, ServicePriority.Normal);
            net.milkbowl.vault.economy.Economy testEco = Bukkit.getServer().getServicesManager()
                    .getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
            plugin.getLogger().info(plugin.getName() + " hooked into Vault, enabled: "
                    + testEco.isEnabled());
        } catch (Exception e) {
            plugin.getLogger().warning("cannot inject Vault - incompatible version?");
            e.printStackTrace();
        }

    }

    @Override
    public boolean isEnabled() {

        return true;
    }

    @Override
    public String getName() {

        return plugin.getName();
    }

    @Override
    public boolean hasBankSupport() {

        return true;
    }

    @Override
    public int fractionalDigits() {

        return 2;
    }

    @Override
    public String format(double v) {

        return eco.getFormattedAmount(v);
    }

    @Override
    public String currencyNamePlural() {

        return eco.getCurrencyNamePlural();
    }

    @Override
    public String currencyNameSingular() {

        return eco.getCurrencyNameSingular();
    }

    @Override
    @Deprecated
    public boolean hasAccount(String playerName) {

        return hasAccount(playerName, null);
    }

    @Override
    public boolean hasAccount(org.bukkit.OfflinePlayer player) {

        return hasAccount(player, null);
    }

    @Override
    @Deprecated
    public boolean hasAccount(String playerName, String worlName) {

        return eco.accountExists(UUIDUtil.convertPlayer(playerName));
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String world) {

        return eco.accountExists(player.getUniqueId());
    }

    @Override
    @Deprecated
    public double getBalance(String playerName) {

        return getBalance(playerName, null);
    }

    @Override
    public double getBalance(OfflinePlayer player) {

        return getBalance(player, null);
    }

    @Override
    @Deprecated
    public double getBalance(String playerName, String worlName) {

        return eco.getBalance(AccountType.PLAYER, UUIDUtil.getUUIDStringFromName(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {

        return eco.getBalance(player.getUniqueId());
    }

    @Override
    @Deprecated
    public boolean has(String playerName, double v) {

        return has(playerName, null, v);
    }

    @Override
    public boolean has(OfflinePlayer player, double v) {

        return has(player, null, v);
    }

    @Override
    @Deprecated
    public boolean has(String playerName, String worlName, double v) {

        return eco.hasEnough(AccountType.PLAYER, UUIDUtil.getUUIDStringFromName(playerName), v);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double v) {

        return eco.hasEnough(player.getUniqueId(), v);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, double v) {

        return withdrawPlayer(playerName, null, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double v) {

        return withdrawPlayer(player, null, v);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, String worlName, double v) {

        eco.substract(UUIDUtil.convertPlayer(playerName), v);
        return new EconomyResponse(v, getBalance(playerName, worlName), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double v) {

        eco.substract(player.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(player, worldName), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, double v) {

        return depositPlayer(playerName, null, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double v) {

        return depositPlayer(player, null, v);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, String worlName, double v) {

        eco.add(UUIDUtil.convertPlayer(playerName), v);
        return new EconomyResponse(v, getBalance(playerName, worlName), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double v) {

        eco.add(player.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(player, worldName), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    @Deprecated
    public EconomyResponse createBank(String accountName, String playerName) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    public EconomyResponse createBank(String playerName, OfflinePlayer player) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    public EconomyResponse deleteBank(String s) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    public EconomyResponse bankBalance(String s) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    @Deprecated
    public EconomyResponse bankHas(String playerName, double v) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    @Deprecated
    public EconomyResponse bankWithdraw(String playerName, double v) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    public EconomyResponse bankDeposit(String playerName, double v) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    @Deprecated
    public EconomyResponse isBankOwner(String playerName, String worlName) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    public EconomyResponse isBankOwner(String playerName, OfflinePlayer player) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    @Deprecated
    public EconomyResponse isBankMember(String playerName, String worlName) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    public EconomyResponse isBankMember(String playerName, OfflinePlayer player) {

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "player bank accounts not supported!");
    }

    @Override
    public List<String> getBanks() {
        // TODO: implement city, plugin list
        return null;
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName) {

        return createPlayerAccount(playerName, null);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {

        return false;
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName, String worlName) {

        if (hasAccount(playerName, worlName)) {
            return false;
        }
        eco.createAccount(UUIDUtil.convertPlayer(playerName));
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {

        if (hasAccount(player, worldName)) {
            return false;
        }
        eco.createAccount(player.getUniqueId());
        return true;
    }

}

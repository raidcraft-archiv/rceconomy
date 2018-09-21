package de.raidcraft.rceconomy.bankchest;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.tables.TBankChest;
import de.raidcraft.rceconomy.tables.TBankMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Philip on 08.01.2016.
 */
public class BankChestManager {

    private RCEconomyPlugin plugin;
    private static BankChestManager instance;

    public enum BankChestType {
        SINGLE_CHEST,
        DOUBLE_CHEST
    }

    public static BankChestManager get() {

        if(instance == null) {
            instance = new BankChestManager(RaidCraft.getComponent(RCEconomyPlugin.class));
        }

        return instance;
    }

    public BankChestManager(RCEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public TBankChest getChest(UUID uuid, BankChestType type) {

        List<TBankChest> chests = plugin.getDatabase().find(TBankChest.class).where().eq("player_id", uuid).eq("type", type.name()).findList();
        if(chests == null || chests.isEmpty()) return null;
        return chests.get(0);
    }

    public TBankChest getChest(int id) {

        TBankChest chests = plugin.getDatabase().find(TBankChest.class, id);
        return chests;
    }

    public TBankChest getChest(Location location) {

        TBankChest bankChest = plugin.getDatabase().find(TBankChest.class).where()
                .eq("x", location.getBlockX()).eq("y", location.getBlockY()).eq("z", location.getBlockZ()).findOne();
        return bankChest;
    }

    public void unregister(int id) {

        TBankChest bankChest = plugin.getDatabase().find(TBankChest.class, id);
        if(bankChest == null) {
            return;
        }
        plugin.getDatabase().delete(bankChest);
    }

    public TBankChest register(Location signLocation, BankChestType type, UUID uuid) {

        // Delete old chest
        TBankChest bankChest = getChest(uuid, type);
        if(bankChest != null) {
            plugin.getDatabase().delete(bankChest);
        }

        bankChest = new TBankChest();
        bankChest.setPlayerId(uuid);
        bankChest.setLastEmptying(new Date(0));
        bankChest.setType(type.name());
        bankChest.setX(signLocation.getBlockX());
        bankChest.setY(signLocation.getBlockY());
        bankChest.setZ(signLocation.getBlockZ());
        plugin.getDatabase().save(bankChest);

        return getChest(signLocation);
    }

    public boolean isCooldownOver(TBankChest bankChest) {

        if(bankChest == null) {
            return false;
        }

        long last = bankChest.getLastEmptying().getTime();
        long next = last + (plugin.getConfig().bankChestDelayHours * 60 * 60 * 1000);

        if(System.currentTimeMillis() < next) {
            return false;
        }

        return true;
    }

    public Date getNextPossibleEmptying(TBankChest bankChest) {

        if(bankChest == null) {
            return null;
        }

        long next = bankChest.getLastEmptying().getTime() + (plugin.getConfig().bankChestDelayHours * 60 * 60 * 1000);
        return new Date(next);
    }

    public double getContentValue(UUID uuid, Inventory inventory, boolean buy) {

        double value = 0;

        Economy economy = RaidCraft.getEconomy();

        ItemStack[] content = inventory.getContents().clone();
        for(ItemStack itemStack : content) {

            // Empty slot
            if(itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }

            // Get material
            TBankMaterial bankMaterial = BankMaterialManager.get().getMaterial(itemStack.getType());
            if(bankMaterial == null) {
                continue;
            }

            if(!bankMaterial.isBuy()) {
                continue;
            }

            double thisValue = bankMaterial.getPriceBuy() * itemStack.getAmount();
            value += thisValue;

            if(buy) {
                inventory.remove(itemStack);
                economy.add(uuid, thisValue, BalanceSource.SELL_ITEM, itemStack.getAmount() + "x " + itemStack.getType().name() + " verkauft.");
            }
        }
        return value;
    }

    public void updateEmptyingDate(Location location) {

        TBankChest bankChest = getChest(location);
        if(bankChest == null) return;
        bankChest.setLastEmptying(new Date(System.currentTimeMillis()));
        plugin.getDatabase().update(bankChest);
    }
}

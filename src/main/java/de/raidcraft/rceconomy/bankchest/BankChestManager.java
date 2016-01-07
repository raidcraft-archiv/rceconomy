package de.raidcraft.rceconomy.bankchest;

import de.raidcraft.RaidCraft;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.tables.TBankChest;
import org.bukkit.block.Chest;

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

        TBankChest chests = plugin.getDatabase().find(TBankChest.class).where().eq("player_id", uuid).eq("type", type.name()).findUnique();
        return chests;
    }

    public TBankChest getChest(int id) {

        TBankChest chests = plugin.getDatabase().find(TBankChest.class, id);
        return chests;
    }

    public void unregister(int id) {

        TBankChest bankChest = plugin.getDatabase().find(TBankChest.class, id);
        if(bankChest == null) {
            return;
        }
        plugin.getDatabase().delete(bankChest);
    }

    public TBankChest register(Chest chest, UUID uuid) {

        return null;
    }
}

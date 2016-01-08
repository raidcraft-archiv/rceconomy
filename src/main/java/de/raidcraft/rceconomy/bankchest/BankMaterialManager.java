package de.raidcraft.rceconomy.bankchest;

import de.raidcraft.RaidCraft;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.tables.TBankMaterial;
import org.bukkit.Material;

import java.util.List;

/**
 * Created by Philip on 08.01.2016.
 */
public class BankMaterialManager {

    private RCEconomyPlugin plugin;
    private static BankMaterialManager instance;

    public static BankMaterialManager get() {

        if(instance == null) {
            instance = new BankMaterialManager(RaidCraft.getComponent(RCEconomyPlugin.class));
        }

        return instance;
    }

    public BankMaterialManager(RCEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public TBankMaterial getMaterial(Material material) {

        TBankMaterial bankMaterial = plugin.getDatabase().find(TBankMaterial.class).where().ieq("material", material.name()).findUnique();
        return bankMaterial;
    }

    public List<TBankMaterial> getAllMaterials() {

        List<TBankMaterial> bankMaterials = plugin.getDatabase().find(TBankMaterial.class).findList();
        return bankMaterials;
    }
}

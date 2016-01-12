package de.raidcraft.rceconomy.shopsign.ui;

import de.raidcraft.RaidCraft;
import de.raidcraft.rceconomy.RCEconomyPlugin;
import de.raidcraft.rceconomy.shopsign.ShopSign;
import org.bukkit.entity.Player;

/**
 * Created by Philip on 12.01.2016.
 */
public class SellMenu {

    private RCEconomyPlugin plugin;
    private static SellMenu instance;

    public static SellMenu get() {

        if(instance == null) {
            instance = new SellMenu(RaidCraft.getComponent(RCEconomyPlugin.class));
        }

        return instance;
    }

    public SellMenu(RCEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean open(Player player, ShopSign shopSign) {

        return true;
    }
}

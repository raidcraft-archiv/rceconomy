package de.raidcraft.rceconomy.chestshop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.event.block.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Philip on 31.01.2016.
 */
@AllArgsConstructor
public class ShopUseConformer {

    @Getter
    private String className;
    @Getter
    private Location location;
    @Getter
    private String shopType;
    @Getter
    private Action action;

    private static Map<UUID, ShopUseConformer> registry = new HashMap<>();

    public static void unregister(String className, UUID uuid) {

        if(registry.containsKey(uuid) &&
                registry.get(uuid).getClassName().equals(className)) {
            registry.remove(uuid);
        }
    }

    public static boolean checkOrRegister(String className, UUID uuid, Location location, String shopType, Action action) {

        if(registry.containsKey(uuid) &&
                registry.get(uuid).getClassName().equals(className) &&
                registry.get(uuid).getLocation().equals(location) &&
                registry.get(uuid).getShopType().equals(shopType) &&
                registry.get(uuid).getAction() == action) {
            unregister(className, uuid);
            return true;
        } else {
            registry.put(uuid, new ShopUseConformer(className, location, shopType, action));
            return false;
        }
    }
}

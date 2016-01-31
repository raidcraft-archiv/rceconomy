package de.raidcraft.rceconomy.chestshop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Philip on 31.01.2016.
 */
@AllArgsConstructor
public class ShopUseConformer {

    @Getter
    private Location location;
    @Getter
    private ShopType shopType;

    private static Map<UUID, ShopUseConformer> registry = new HashMap<>();

    public static void unregister(UUID uuid) {

        registry.remove(uuid);
    }

    public static boolean checkOrRegister(UUID uuid, Location location, ShopType shopType) {

        if(registry.containsKey(uuid) &&
                registry.get(uuid).getLocation().equals(location) &&
                registry.get(uuid).getShopType() ==shopType) {
            unregister(uuid);
            return true;
        } else {
            registry.put(uuid, new ShopUseConformer(location, shopType));
            return false;
        }
    }
}

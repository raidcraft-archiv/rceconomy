package de.raidcraft.rceconomy.chestshop;

/**
 * Created by Philip on 31.01.2016.
 */
public enum ShopType {

    SELL("Verkauf", "chestshop.create"),
    BUY("Ankauf", "chestshop.create"),
    ADMIN_SELL("Server-Verkauf", "chesthop.admin"),
    ADMIN_BUY("Server-Ankauf", "chestshop.admin");

    private String displayText;
    private String permission;

    private ShopType(String displayText, String permission) {
        this.displayText = displayText;
        this.permission = permission;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getPermission() {
        return permission;
    }

    public static ShopType getByDisplayText(String displayText) {
        for(ShopType shopType : ShopType.values()) {
            if(shopType.getDisplayText().equalsIgnoreCase(displayText)) {
                return shopType;
            }
        }
        return null;
    }
}

package de.raidcraft.rceconomy.expsign;

/**
 * Created by Philip on 31.01.2016.
 */
public enum ExpShopType {

    SELL("Verkauf", "expshop.sell.create"),
    BUY("Ankauf", "expshop.buy.create"),
    ADMIN_SELL("Server-Verkauf", "expshop.admin"),
    ADMIN_BUY("Server-Ankauf", "expshop.admin");

    private String displayText;
    private String permission;

    ExpShopType(String displayText, String permission) {
        this.displayText = displayText;
        this.permission = permission;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getPermission() {
        return permission;
    }

    public static ExpShopType getByDisplayText(String displayText) {
        for(ExpShopType shopType : ExpShopType.values()) {
            if(shopType.getDisplayText().equalsIgnoreCase(displayText)) {
                return shopType;
            }
        }
        return null;
    }
}

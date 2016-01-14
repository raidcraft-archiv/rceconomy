package de.raidcraft.rceconomy.shopsign.ui;

import de.raidcraft.rceconomy.shopsign.ShopSign;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

/**
 * Created by Philip on 14.01.2016.
 */

@Getter
@Setter
public class EditedSign {

    public EditedSign(ShopSign shopSign) {

        price = shopSign.getPrice();
        itemNumber = shopSign.getMaxNumber();
        material = shopSign.getMaterial();
    }

    private double price;
    private int itemNumber;
    private Material material;
}

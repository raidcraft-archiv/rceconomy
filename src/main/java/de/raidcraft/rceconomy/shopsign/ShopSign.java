package de.raidcraft.rceconomy.shopsign;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.rceconomy.shopsign.ui.EditedSign;
import de.raidcraft.rceconomy.shopsign.ui.SellMenu;
import de.raidcraft.util.ItemUtils;
import de.raidcraft.util.SignUtil;
import de.raidcraft.util.UUIDUtil;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Philip on 12.01.2016.
 */

public class ShopSign {

    public enum ShopSignType{
        SELL("Verkauf"),
        BUY("Ankauf");

        private String tag;

        private ShopSignType(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }

    private Sign sign;
    private UUID playerId;
    @Getter
    @Setter
    private Material material;
    @Getter
    @Setter
    private int maxNumber;
    private int boughtNumber;
    private boolean blocked;
    private ShopSignType type;
    @Getter
    @Setter
    private double price;
    @Getter
    private boolean isValid;

    public ShopSign(String[] lines) {

        // first line (tag)
        boolean found = false;
        for(ShopSignType type : ShopSignType.values()) {
            if(SignUtil.strip(lines[0]).equalsIgnoreCase(type.getTag())) {
                found = true;
                this.type = type;
                break;
            }
        }
        if(!found) {
            isValid = false;
            return;
        }

        // second line (material, player ID)
        String[] secondLineParts = SignUtil.strip(lines[1]).split(":");
        if(lines[1].isEmpty() || secondLineParts.length != 2) {
            material = null;
            playerId = null;
        } else {
            material = ItemUtils.getItem(secondLineParts[0]);
            try {
                playerId = UUIDUtil.getUuidFromPlayerId(Integer.parseInt(secondLineParts[1]));
            } catch(NumberFormatException e) {
                playerId = null;
            }
        }
        if(material == null) {
            material = Material.AIR;
        }

        // third line (number)
        String numberParts[] = SignUtil.strip(lines[2]).split(" ");
        if (numberParts.length != 2) {
            maxNumber = 0;
            boughtNumber = 0;
        }
        else if(type == ShopSignType.SELL) {
            try {
                maxNumber = Integer.parseInt(numberParts[0]);
            } catch (NumberFormatException e) {
                maxNumber = 0;
            }
        } else if(type == ShopSignType.BUY) {
            String[] boughtParts = numberParts[0].split("/");
            if(boughtParts.length != 2) {
                maxNumber = 0;
                boughtNumber = 0;
            }
            else {
                try {
                    boughtNumber = Integer.parseInt(boughtParts[0]);
                    maxNumber = Integer.parseInt(boughtParts[1]);
                } catch(NumberFormatException e) {
                    maxNumber = 0;
                    boughtNumber = 0;
                }
            }
        }

        // fourth line (price)
        price = RaidCraft.getEconomy().parseCurrencyInput(SignUtil.strip(lines[3]));

        isValid = true;
    }

    public ShopSign(Sign sign) {

        this(sign.getLines());
        this.sign = sign;
    }

    public void setPlayerId(UUID playerId) {

        this.playerId = playerId;
    }

    public String[] getFormattedLines() {

        String[] lines = new String[4];
        lines[0] = ChatColor.YELLOW + "[" + ChatColor.GREEN + type.getTag() + ChatColor.YELLOW + "]";
        lines[1] = ChatColor.BLACK + String.valueOf(material.getId()) + ":" + UUIDUtil.getPlayerId(playerId);
        if(type == ShopSignType.SELL) {
            lines[2] = ((maxNumber > 0) ? ChatColor.DARK_GREEN : ChatColor.DARK_RED) + String.valueOf(maxNumber) + ChatColor.AQUA + " Stück";
        } else {
            lines[2] = ((boughtNumber < maxNumber) ? ChatColor.DARK_GREEN : ChatColor.DARK_RED) + String.valueOf(boughtNumber) + ChatColor.BLACK + "/" + maxNumber + " " + ChatColor.AQUA + " Stück";
        }
        lines[3] = RaidCraft.getEconomy().getFormattedAmount(price);

        return lines;
    }

    public void updateSign() {

        if(sign == null) return;

        String[] lines = getFormattedLines();
        for(int i = 0; i < lines.length; i++) {
            sign.setLine(i, lines[i]);
        }
        sign.update();
    }

    public boolean isBlocked() {

        if(!isValid) {
            return true;
        }

        if(material == null || material == Material.AIR) {
            return true;
        }

        if(price == 0) {
            return true;
        }

        return false;
    }

    public void updateValues(EditedSign changeValues) {
        price = changeValues.getPrice();
        material = changeValues.getMaterial();
        maxNumber = changeValues.getItemNumber();
    }

    public void interact(Player player, Action action) {

        // Open config menu
        if(player.getUniqueId().equals(playerId)) {

            // block sign
            double backupPrice = price;
            price = 0;
            updateSign();
            price = backupPrice;

            if(type == ShopSignType.SELL) {
                SellMenu.get().open(player, this, new EditedSign(this));
            } else if(type == ShopSignType.BUY) {
                //TODO
            }

            return;
        }

        if(isBlocked()) {
            player.sendMessage(ChatColor.RED + "Dieser Shop ist derzeit geschlossen!");
            return;
        }

        // Sell or buy items

        if(type == ShopSignType.BUY) {
            // Check if player has items in inventory (and how much)
            int itemInventoryCount = 0;
            for (ItemStack itemStack : player.getInventory().all(material).values()) {
                itemInventoryCount += itemStack.getAmount();
            }
            if (itemInventoryCount == 0) {
                player.sendMessage(ChatColor.RED + "Du hast keine Items zu verkaufen!");
                return;
            }

            // Subtract one items
            int itemExchangeNum = 1;
            if (action == Action.RIGHT_CLICK_BLOCK) {
                itemExchangeNum = Math.min(itemInventoryCount, (maxNumber - boughtNumber));
            }

            // Check money
            if(!RaidCraft.getEconomy().hasEnough(playerId, price * itemExchangeNum)) {
                player.sendMessage(ChatColor.RED + "Der Händler kann sich diesen Ankauf nicht leisten!");
                return;
            }

            boughtNumber += itemExchangeNum;
            player.getInventory().removeItem(new ItemStack[]{new ItemStack(material, itemExchangeNum)});
            player.updateInventory();
            updateSign();

            // Subtract money
            RaidCraft.getEconomy().substract(playerId,
                    price * itemExchangeNum, BalanceSource.TRADE,
                    itemExchangeNum + " " + ItemUtils.getFriendlyName(material) + " von " + player.getName() + " angekauft");

            // Give money
            RaidCraft.getEconomy().add(player.getUniqueId(),
                    price * itemExchangeNum, BalanceSource.TRADE,
                    itemExchangeNum + " " + ItemUtils.getFriendlyName(material) + " an " + UUIDUtil.getNameFromUUID(playerId) + " verkauft");

        } else if(type == ShopSignType.SELL) {

            int itemExchangeNum = 1;
            if (action == Action.RIGHT_CLICK_BLOCK) {
                itemExchangeNum = Math.min(64, maxNumber);
            }

            // Check money
            if(!RaidCraft.getEconomy().hasEnough(player.getUniqueId(), price * itemExchangeNum)) {
                player.sendMessage(ChatColor.RED + "Du kannst dir den Kauf nicht leisten!");
                return;
            }

            maxNumber -= itemExchangeNum;
            player.getInventory().removeItem(new ItemStack[]{new ItemStack(material, itemExchangeNum)});
            player.updateInventory();
            updateSign();

            // Subtract money
            RaidCraft.getEconomy().substract(player.getUniqueId(),
                    price * itemExchangeNum, BalanceSource.TRADE,
                    itemExchangeNum + " " + ItemUtils.getFriendlyName(material) + " von " + player.getName() + " gekauft");

            // Give money
            RaidCraft.getEconomy().add(playerId,
                    price * itemExchangeNum, BalanceSource.TRADE,
                    itemExchangeNum + " " + ItemUtils.getFriendlyName(material) + " an " + UUIDUtil.getNameFromUUID(playerId) + " verkauft");
        }
    }
}

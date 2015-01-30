package de.raidcraft.rceconomy.actionapi;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rcconversations.api.action.AbstractAction;
import de.raidcraft.rcconversations.api.action.ActionArgumentException;
import de.raidcraft.rcconversations.api.action.ActionArgumentList;
import de.raidcraft.rcconversations.api.action.ActionInformation;
import de.raidcraft.rcconversations.api.action.WrongArgumentValueException;
import de.raidcraft.rcconversations.api.conversation.Conversation;
import de.raidcraft.rcconversations.util.ParseString;

/**
 * @author Philip Urban
 */
@Deprecated
@ActionInformation(name = "SUBSTRACT_MONEY")
public class SubstractMoneyAction extends AbstractAction {

    @Override
    public void run(Conversation conversation, ActionArgumentList args) throws ActionArgumentException {

        String stringAmount = args.getString("amount");
        String reason = args.getString("reason", "Conversation");
        stringAmount = ParseString.INST.parse(conversation, stringAmount);

        double amount;
        try {
            amount = Double.parseDouble(stringAmount);
        } catch (NumberFormatException e) {
            throw new WrongArgumentValueException("Wrong argument value in action '" + getName() + "': Amount must be a number!");
        }

        Economy economy = RaidCraft.getEconomy();
        economy.substract(conversation.getPlayer().getUniqueId(),
                amount, BalanceSource.PLUGIN, reason);
    }
}
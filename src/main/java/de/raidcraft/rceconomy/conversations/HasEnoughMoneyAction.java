package de.raidcraft.rceconomy.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.economy.Economy;
import de.raidcraft.rcconversations.api.action.*;
import de.raidcraft.rcconversations.api.conversation.Conversation;
import de.raidcraft.rcconversations.util.MathHelper;
import de.raidcraft.rcconversations.util.ParseString;
import de.raidcraft.rcconversations.util.WrongFormulaException;

/**
 * @author Philip Urban
 */
@ActionInformation(name = "HAS_ENOUGH_MONEY")
public class HasEnoughMoneyAction extends AbstractAction {

    @Override
    public void run(Conversation conversation, ActionArgumentList args) throws ActionArgumentException {

        String success = args.getString("onsuccess", null);
        String failure = args.getString("onfailure", null);
        String account = args.getString("account", conversation.getPlayer().getName());
        account = ParseString.INST.parse(conversation, account);
        String stringAmount = args.getString("amount");
        stringAmount = ParseString.INST.parse(conversation, stringAmount);
        double amount;

        try {
            amount = MathHelper.solveDoubleFormula(stringAmount);
        }
        catch(WrongFormulaException e) {
            conversation.abortActionExecution(true);
            throw new WrongArgumentValueException("Wrong argument value in action '" + getName() + "': Amount '" + stringAmount + "' is not a double!");
        }

        Economy economy = RaidCraft.getEconomy();

        conversation.set("money", amount);
        conversation.set("money_formatted", economy.getFormattedAmount(amount));

        if(economy.hasEnough(account, amount)) {
            if(success != null) {
                conversation.abortActionExecution(true);
                conversation.setCurrentStage(success);
                conversation.triggerCurrentStage();
            }
        }
        else {
            if(failure != null) {
                conversation.abortActionExecution(true);
                conversation.setCurrentStage(failure);
                conversation.triggerCurrentStage();
            }
        }
    }
}
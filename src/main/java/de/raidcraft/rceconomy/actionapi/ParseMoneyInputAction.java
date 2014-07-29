package de.raidcraft.rceconomy.actionapi;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.api.action.AbstractAction;
import de.raidcraft.rcconversations.api.action.ActionArgumentException;
import de.raidcraft.rcconversations.api.action.ActionArgumentList;
import de.raidcraft.rcconversations.api.action.ActionInformation;
import de.raidcraft.rcconversations.api.conversation.Conversation;

/**
 * @author Philip Urban
 */
// TODO: needed?
@ActionInformation(name = "PARSE_MONEY_INPUT")
public class ParseMoneyInputAction extends AbstractAction {

    @Override
    public void run(Conversation conversation, ActionArgumentList args) throws ActionArgumentException {

        String input = conversation.getString("input");
        String varName = args.getString("var", "money");
        String success = args.getString("onsuccess", null);
        String failure = args.getString("onfailure", null);

        double amount = RaidCraft.getEconomy().parseCurrencyInput(input);
        if(amount == 0) {
            changeStage(conversation, failure);
            return;
        }
        conversation.set(varName, amount);
        changeStage(conversation, success);
    }

    private void changeStage(Conversation conversation, String stage) {

        if (stage != null) {
            conversation.setCurrentStage(stage);
            conversation.triggerCurrentStage();
        }
    }
}

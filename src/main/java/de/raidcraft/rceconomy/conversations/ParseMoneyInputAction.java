package de.raidcraft.rceconomy.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.api.action.AbstractAction;
import de.raidcraft.rcconversations.api.action.ActionArgumentException;
import de.raidcraft.rcconversations.api.action.ActionArgumentList;
import de.raidcraft.rcconversations.api.action.ActionInformation;
import de.raidcraft.rcconversations.api.conversation.Conversation;
import de.raidcraft.rceconomy.RCEconomyPlugin;

/**
 * @author Philip Urban
 */
@ActionInformation(name = "PARSE_MONEY_INPUT")
public class ParseMoneyInputAction extends AbstractAction {

    @Override
    public void run(Conversation conversation, ActionArgumentList args) throws ActionArgumentException {

        String input = conversation.getString("input");
        String varName = args.getString("var", "money");
        String success = args.getString("onsuccess", null);
        String failure = args.getString("onfailure", null);

        double amount = RaidCraft.getComponent(RCEconomyPlugin.class).parseCurrencyInput(input);
        if(amount == 0) {
            changeStage(conversation, failure);
            return;
        }

        conversation.set(varName, amount);
        changeStage(conversation, success);
    }

    private void changeStage(Conversation conversation, String stage) {

        if (stage != null) {
            conversation.abortActionExecution(true);
            conversation.setCurrentStage(stage);
            conversation.triggerCurrentStage();
        }
    }
}

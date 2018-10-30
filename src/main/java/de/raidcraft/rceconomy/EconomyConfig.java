package de.raidcraft.rceconomy;

import de.raidcraft.api.config.Comment;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;

/**
 * @author Dragonfire
 */
public class EconomyConfig extends ConfigurationBase<RCEconomyPlugin> {

    @Setting("initial-amount")
    public double initialAmount = 0.0;
    @Setting("size-print-flow-entries")
    public int sizePrintFlowEntries = 10;
    @Setting("currency.name-singular")
    public String currencySingular = "Coin";
    @Setting("currency.name-plural")
    public String currencyPlural = "Coins";

    @Setting("currency.first-letters")
    @Comment("The first letter that is used when parsing the eceonomy string. Can be multiple.")
    public String currencyFirstLetters = "gG";
    @Setting("currency.second-letters")
    @Comment("The second letter that is used when parsing the eceonomy string. Can be multiple.")
    public String currencySecondLetters = "sS";
    @Setting("currency.third-letters")
    @Comment("The third letter that is used when parsing the eceonomy string. Can be multiple.")
    public String currencyThirdLetters = "cCkK";

    @Setting("messages.balance-change.enabled")
    public boolean balanceChangeEnabled = true;
    @Setting("messages.balance-change.text")
    @Comment("The text that is displayed to the player when a balance change happens.")
    public String balanceChangeText = "&2Kontobewegung: %formatted-amount% Grund: &e%reason%";
    @Setting("messages.balance-change.details")
    @Comment("Optional details text that is shown to the player.")
    public String balanceDetailsText = "&2Details: &7%details%";

    public EconomyConfig(RCEconomyPlugin plugin) {

        super(plugin, "config.yml");
    }
}

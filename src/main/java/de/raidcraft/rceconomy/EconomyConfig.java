package de.raidcraft.rceconomy;

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
    @Setting("currency-displayName-singular")
    public String currencySingular = "Coin";
    @Setting("currency-displayName-plural")
    public String currencyPlural = "Coins";

    public EconomyConfig(RCEconomyPlugin plugin) {

        super(plugin, "config.yml");
    }
}

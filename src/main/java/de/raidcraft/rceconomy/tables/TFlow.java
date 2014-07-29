package de.raidcraft.rceconomy.tables;

import de.raidcraft.api.economy.BalanceSource;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Dragonfire
 */

@Entity
@Table(name = "rceconomy_flow")
public class TFlow {
    @Getter
    @Setter
    @Id
    private int id;
    @Getter
    @Setter
    private double amount;
    @Getter
    @Setter
    private BalanceSource source;
    @Getter
    @Setter
    public String detail;
    @Getter
    @Setter
    public Date date;
    @Getter
    @Setter
    @ManyToOne
    public TAccount account;
}

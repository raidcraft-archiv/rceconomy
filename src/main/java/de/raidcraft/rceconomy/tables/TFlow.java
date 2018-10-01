package de.raidcraft.rceconomy.tables;

import de.raidcraft.api.economy.BalanceSource;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Dragonfire
 */

@Data
@Entity
@Table(name = "rc_economy_flow")
public class TFlow {

    @Id
    private int id;

    private double amount;
    private BalanceSource source;
    public String detail;
    public Date date;
    @ManyToOne
    public TAccount account;
}

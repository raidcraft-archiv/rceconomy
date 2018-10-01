package de.raidcraft.rceconomy.tables;

import de.raidcraft.api.economy.AccountType;
import lombok.Data;

import javax.persistence.*;

/**
 * @author Dragonfire
 */
@Data
@Entity
@Table(name = "rc_economy_balance")
public class TAccount {

    @Id
    private int id;
    private String name;
    private double balance;
    private int exp;
    @Enumerated(EnumType.STRING)
    private AccountType type;
}

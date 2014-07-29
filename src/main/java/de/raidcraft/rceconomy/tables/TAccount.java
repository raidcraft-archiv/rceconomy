package de.raidcraft.rceconomy.tables;

import de.raidcraft.api.economy.AccountType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Dragonfire
 */
@Entity
@Table(name = "rceconomy_balance")
public class TAccount {
    @Getter
    @Setter
    @Id
    private int id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private double balance;
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private AccountType type;
}

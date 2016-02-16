package de.raidcraft.rceconomy.tables;

import de.raidcraft.api.economy.AccountType;
import lombok.Data;
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
@Data
@Entity
@Table(name = "rceconomy_balance")
public class TAccount {

    @Id
    private int id;
    private String name;
    private double balance;
    private int exp;
    @Enumerated(EnumType.STRING)
    private AccountType type;
}

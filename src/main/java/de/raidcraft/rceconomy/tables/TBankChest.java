package de.raidcraft.rceconomy.tables;

import de.raidcraft.rceconomy.bankchest.BankChestManager;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Philip on 08.01.2016.
 */

@Data
@Entity
@Table(name = "rceconomy_bankchests")
public class TBankChest {

    @Id
    private int id;

    public UUID playerId;
    public String type;
    public Date lastEmptying;
    public int x;
    public int y;
    public int z;

    public BankChestManager.BankChestType getBankChestType() {

        return BankChestManager.BankChestType.valueOf(type);
    }
}

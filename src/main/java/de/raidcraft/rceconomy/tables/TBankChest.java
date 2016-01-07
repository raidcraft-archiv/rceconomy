package de.raidcraft.rceconomy.tables;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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

    private UUID playerId;
    private String type;
}

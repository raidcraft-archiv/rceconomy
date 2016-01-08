package de.raidcraft.rceconomy.tables;

import lombok.Data;

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
@Table(name = "rceconomy_bank_materials")
public class TBankMaterial {

    @Id
    private int id;

    public String material;
    public double priceBuy;
    public double priceSell;
    public boolean buy;
    public boolean sell;
}

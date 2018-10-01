package de.raidcraft.rceconomy.tables;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Philip on 08.01.2016.
 */

@Data
@Entity
@Table(name = "rc_economy_bank_materials")
public class TBankMaterial {

    @Id
    private int id;

    public String material;
    public double priceBuy;
    public double priceSell;
    public boolean buy;
    public boolean sell;
}

package Game;

import fileio.CardInput;

import java.util.ArrayList;
import java.util.List;

public class Minion extends Card{
    private int attackDamage;

    public Minion(){
        super();
        this.attackDamage = 0;
    }

    public Minion(CardInput NewMinion){
        super(NewMinion);
        this.attackDamage = NewMinion.getAttackDamage();
    }

    public void setAttackDamage(int attackDamage){
        this.attackDamage = attackDamage;
    }

    public int getAttackDamage(){
        return this.attackDamage;
    }


}

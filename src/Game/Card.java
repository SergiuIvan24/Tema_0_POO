package Game;

import fileio.CardInput;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Card {

    private int mana;
    private int health;
    private String description;
    private ArrayList<String> colors;
    private String name;
    private int attackDamage;
    private boolean isFrozen;
    private boolean hasAttacked;

    public Card(){
        this.mana = 0;
        this.health = 0;
        this.description = null;
        this.colors = null;
        this.name = null;
    }

    public Card(CardInput card){
        this.mana = card.getMana();
        this.health = card.getHealth();
        this.description = card.getDescription();
        this.colors = card.getColors();
        this.name = card.getName();
        this.attackDamage = card.getAttackDamage();
    }


    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getColors() {
        return colors;
    }

    public void setColors(ArrayList<String> colors) {
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public void setFrozen(boolean val) {
        this.isFrozen = val;
    }

    public boolean getFrozen(){
        return this.isFrozen;
    }

    public boolean isHasAttacked(){
        return this.hasAttacked;
    }

    public void setHasAttacked(boolean val) {
        this.hasAttacked = val;
    }


    public int getAttackDamage() {
        return this.attackDamage;
    }

    public void setAttackDamage(int val){
        this.attackDamage = val;
    }
}

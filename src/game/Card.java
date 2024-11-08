package game;

import fileio.CardInput;

import java.util.ArrayList;

public final class Card {

    private int mana;
    private int health;
    private String description;
    private ArrayList<String> colors;
    private String name;
    private int attackDamage;
    private boolean isFrozen;
    private boolean hasAttacked;

    public Card() {
        this.mana = 0;
        this.health = 0;
        this.description = null;
        this.colors = null;
        this.name = null;
    }

    public Card(final CardInput card) {
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

    public void setHealth(final int health) {
        this.health = health;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public ArrayList<String> getColors() {
        return colors;
    }

    public void setColors(final ArrayList<String> colors) {
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getMana() {
        return mana;
    }

    public void setMana(final int mana) {
        this.mana = mana;
    }

    public void setFrozen(final boolean val) {
        this.isFrozen = val;
    }

    public boolean getFrozen() {
        return this.isFrozen;
    }

    public boolean isHasAttacked() {
        return this.hasAttacked;
    }

    public void setHasAttacked(final boolean val) {
        this.hasAttacked = val;
    }

    public int getAttackDamage() {
        return this.attackDamage;
    }

    public void setAttackDamage(final int val) {
        this.attackDamage = val;
    }
}

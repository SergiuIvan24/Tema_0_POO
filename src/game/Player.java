package game;

import fileio.CardInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public final class Player {

    private static final int HERO_INITIAL_HEALTH = 30;

    private int mana;

    private final Card playerHero;

    private final ArrayList<CardInput> playerDeck;

    private ArrayList<CardInput> playerHand;

    private boolean activeInTurn;

    private int nrWins = 0;

    /**
     * Constructor pentru clasa Player.
     * Inițializează un jucător cu eroul, pachetul de cărți și mana inițială.
     *
     * @param playerIdx   indexul jucătorului
     * @param playerHero  eroul jucătorului
     * @param playerDeck  pachetul de cărți al jucătorului
     * @param mana        mana inițială a jucătorului
     */
    public Player(final int playerIdx, final Card playerHero,
                  final ArrayList<CardInput> playerDeck, final int mana) {
        this.playerHero = playerHero;
        this.playerDeck = playerDeck;
        this.playerHand = new ArrayList<>();
        this.mana = mana;
    }

    /**
     * Resetează starea jucătorului pentru un joc nou.
     * Reinitializează mana, golește mâna și resetează viața și starea eroului.
     */
    public void reset() {
        this.mana = 0;
        this.playerHand.clear();
        this.playerHero.setHealth(HERO_INITIAL_HEALTH);
        this.playerHero.setHasAttacked(false);
    }

    /**
     * Amestecă pachetul de cărți al jucătorului.
     *
     * @param seed sămânța pentru generarea aleatorie
     */
    public void shuffleDeck(final int seed) {
        Random random = new Random(seed);
        Collections.shuffle(this.playerDeck, random);
    }

    /**
     * Returnează pachetul de cărți al jucătorului.
     *
     * @return pachetul de cărți
     */
    public ArrayList<CardInput> getDeck() {
        return this.playerDeck;
    }

    /**
     * Returnează cartea de tip erou a jucătorului.
     *
     * @return cartea erou
     */
    public Card getHero() {
        return this.playerHero;
    }

    /**
     * Returnează dacă jucătorul este activ în tura curentă.
     *
     * @return true dacă jucătorul este activ, false altfel
     */
    public boolean getActiveInTurn() {
        return this.activeInTurn;
    }

    /**
     * Setează dacă jucătorul este activ în tura curentă.
     *
     * @param stat starea de activitate
     */
    public void setActiveInTurn(final boolean stat) {
        this.activeInTurn = stat;
    }

    /**
     * Setează mana jucătorului.
     *
     * @param mana noua valoare a manei
     */
    public void setMana(final int mana) {
        this.mana = mana;
    }

    /**
     * Returnează mana curentă a jucătorului.
     *
     * @return valoarea manei
     */
    public int getMana() {
        return this.mana;
    }

    /**
     * Setează mâna jucătorului.
     *
     * @param playerHand noua mână a jucătorului
     */
    public void setPlayerHand(final ArrayList<CardInput> playerHand) {
        this.playerHand = playerHand;
    }

    /**
     * Returnează mâna jucătorului.
     *
     * @return mâna jucătorului
     */
    public ArrayList<CardInput> getPlayerHand() {
        return this.playerHand;
    }

    /**
     * Returnează o carte din mâna jucătorului pe baza indexului.
     *
     * @param idx indexul cărții
     * @return cartea de la indexul specificat
     */
    public CardInput getCardHand(final int idx) {
        return this.playerHand.get(idx);
    }

    /**
     * Returnează numărul de victorii ale jucătorului.
     *
     * @return numărul de victorii
     */
    public int getNrWins() {
        return this.nrWins;
    }

    /**
     * Setează numărul de victorii ale jucătorului.
     *
     * @param val noua valoare pentru numărul de victorii
     */
    public void setNrWins(final int val) {
        this.nrWins = val;
    }

}

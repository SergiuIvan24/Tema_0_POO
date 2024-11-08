package game;

import fileio.CardInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public final class Player {

    private static final int HERO_INITIAL_HEALTH = 30;

    private int mana;

    private int playerDeckIdx;

    private Card playerHero;

    private ArrayList<CardInput> playerDeck;

    private ArrayList<CardInput> playerHand;

    private boolean activeInTurn;

    private int nrWins = 0;

    /**
     * Constructs a Player object.
     *
     * @param playerIdx   the index of the player's deck
     * @param playerHero  the player's hero card
     * @param playerDeck  the player's deck of cards
     * @param mana        the initial mana of the player
     */
    public Player(final int playerIdx, final Card playerHero,
                  final ArrayList<CardInput> playerDeck, final int mana) {
        this.playerDeckIdx = playerIdx;
        this.playerHero = playerHero;
        this.playerDeck = playerDeck;
        this.playerHand = new ArrayList<>();
        this.mana = mana;
    }

    /**
     * Resets the player's state for a new game.
     */
    public void reset() {
        this.mana = 0;
        this.playerHand.clear();
        this.playerHero.setHealth(HERO_INITIAL_HEALTH);
        this.playerHero.setHasAttacked(false);
    }

    /**
     * Shuffles the player's deck.
     *
     * @param seed the seed for the random shuffle
     */
    public void shuffleDeck(final int seed) {
        Random random = new Random(seed);
        Collections.shuffle(this.playerDeck, random);
    }

    /**
     * Gets the player's deck.
     *
     * @return the player's deck of cards
     */
    public ArrayList<CardInput> getDeck() {
        return this.playerDeck;
    }

    /**
     * Gets the player's hero card.
     *
     * @return the player's hero card
     */
    public Card getHero() {
        return this.playerHero;
    }

    /**
     * Gets whether the player is active in the current turn.
     *
     * @return true if the player is active in the current turn, false otherwise
     */
    public boolean getActiveInTurn() {
        return this.activeInTurn;
    }

    /**
     * Sets whether the player is active in the current turn.
     *
     * @param stat the new active status
     */
    public void setActiveInTurn(final boolean stat) {
        this.activeInTurn = stat;
    }

    /**
     * Sets the player's mana.
     *
     * @param mana the new mana value
     */
    public void setMana(final int mana) {
        this.mana = mana;
    }

    /**
     * Gets the player's mana.
     *
     * @return the player's mana
     */
    public int getMana() {
        return this.mana;
    }

    /**
     * Sets the player's hand.
     *
     * @param playerHand the new player hand
     */
    public void setPlayerHand(final ArrayList<CardInput> playerHand) {
        this.playerHand = playerHand;
    }

    /**
     * Gets the player's hand.
     *
     * @return the player's hand
     */
    public ArrayList<CardInput> getPlayerHand() {
        return this.playerHand;
    }

    /**
     * Gets a card from the player's hand.
     *
     * @param idx the index of the card in the player's hand
     * @return the card at the specified index
     */
    public CardInput getCardHand(final int idx) {
        return this.playerHand.get(idx);
    }

    public int getNrWins() {
        return this.nrWins;
    }

    public void setNrWins(final int val) {
        this.nrWins = val;
    }

}

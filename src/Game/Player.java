package Game;

import fileio.CardInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Player {

    private int mana;

    private int playerDeckIdx;

    private Card playerHero;

    private ArrayList<CardInput> playerDeck;

    public ArrayList<CardInput> playerHand;

    private boolean activeInTurn;

    public Player(int playerIdx, Card playerHero, ArrayList<CardInput> playerDeck, int mana){
        this.playerDeckIdx = playerIdx;
        this.playerHero = playerHero;
        this.playerDeck = playerDeck;
        this.playerHand = new ArrayList<>();
        this.mana = mana;
    }

    public void shuffleDeck(int seed){
        Random random = new Random(seed);
        Collections.shuffle(this.playerDeck, random);
    }
    public ArrayList<CardInput> getDeck(){
        return this.playerDeck;
    }
    public Card getHero(){
        return this.playerHero;
    }
    public boolean getActiveInTurn(){
        return this.activeInTurn;
    }
    public void setActiveInTurn(boolean stat){
        this.activeInTurn = stat;
    }

    public void setMana(int mana){
        this.mana = mana;
    }

    public int getMana(){
        return this.mana;
    }

    public void setPlayerHand(ArrayList<CardInput> playerHand) {
        this.playerHand = playerHand;
    }

    public ArrayList<CardInput> getPlayerHand(){
        return this.playerHand;
    }

    public CardInput getCardHand(int idx){
        return this.playerHand.get(idx);
    }

}

package Game;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import fileio.ActionsInput;
import fileio.DecksInput;
import fileio.StartGameInput;
import java.util.ArrayList;


public class GameSession {
    private int roundNumber;
    private ArrayList<CardInput> playerOneDeck;
    private ArrayList<CardInput> playerTwoDeck;
    private GameBoard gameBoard;
    private Player playerOne;
    private Player playerTwo;
    private Player currentPlayer;
    private int turnCompleted;
    private int startingPlayer;
    private boolean gameEnded = false;

    public GameSession(ArrayList<CardInput> playerOneDeck, ArrayList<CardInput> playerTwoDeck, StartGameInput startGame) {
        this.roundNumber = 0;
        this.playerOneDeck = playerOneDeck;
        this.playerTwoDeck = playerTwoDeck;
        this.gameBoard = new GameBoard();
        this.turnCompleted = 0;
        initializeGame(startGame);
    }

    public void initializeGame(StartGameInput startGame) {

        Random random = new Random(startGame.getShuffleSeed());
        Collections.shuffle(playerOneDeck, random);
        Random random2 = new Random(startGame.getShuffleSeed());
        Collections.shuffle(playerTwoDeck, random2);
        Card playerOneHero = new Card(startGame.getPlayerOneHero());
        Card playerTwoHero = new Card(startGame.getPlayerTwoHero());
        this.playerOne = new Player(startGame.getPlayerOneDeckIdx(), playerOneHero, playerOneDeck, 0);
        this.playerTwo = new Player(startGame.getPlayerTwoDeckIdx(), playerTwoHero, playerTwoDeck, 0);
        this.playerOne.getHero().setHealth(30);
        this.playerTwo.getHero().setHealth(30);
        this.startingPlayer = startGame.getStartingPlayer();

        if (startingPlayer == 1) {
            this.currentPlayer = playerOne;
        } else {
            this.currentPlayer = playerTwo;
        }
        startRound();
    }

    public void startRound() {

        this.roundNumber++;
        updateMana(playerOne);
        updateMana(playerTwo);
        drawCard(playerOne);
        drawCard(playerTwo);
        this.turnCompleted = 0;
    }

    public void endTurn() {

        if (getCurrentPlayerIndex() == 1) {
            currentPlayer = playerTwo;
        } else {
            currentPlayer = playerOne;
        }

        this.getGameBoard().resetAttack();
        this.getGameBoard().resetHeroAttack(playerOne);
        this.getGameBoard().resetHeroAttack(playerTwo);

        if (this.turnCompleted == 1) {
            startRound();
        } else {
            resetFrozenCards();
            this.turnCompleted++;
        }
    }

    public void resetFrozenCards() {

        if (this.getCurrentPlayerIndex() == 1) {
            for (int row = 0; row <= 1; row++) {
                ArrayList<Card> cardsInRow = this.getGameBoard().getRow(row);
                for (Card card : cardsInRow) {
                    if (card.getFrozen()) {
                        card.setFrozen(false);
                    }
                }
            }
        } else {
            for (int row = 2; row <= 3; row++) {
                ArrayList<Card> cardsInRow = this.getGameBoard().getRow(row);
                for (Card card : cardsInRow) {
                    if (card.getFrozen()) {
                        card.setFrozen(false);
                    }
                }
            }
        }


    }

    private void updateMana(Player player) {
        int newMana = player.getMana() + roundNumber;
        if (player.getMana() <= 10) {
            player.setMana(newMana);
        }
    }

    private void drawCard(Player player) {
        if (!player.getDeck().isEmpty()) {
            CardInput newCard = player.getDeck().remove(0);
            player.getPlayerHand().add(newCard);
        }
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void incrementRoundNumber() {
        this.roundNumber++;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayer == playerOne ? 1 : 2;
    }
    public boolean isGameEnded(){
        return this.gameEnded;
    }

    public void setGameEnded(boolean val){
        this.gameEnded = val;
    }

}

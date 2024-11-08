package game;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import fileio.CardInput;
import fileio.StartGameInput;


public class GameSession {
    private static final int INITIAL_HEALTH = 30;
    private static final int MAX_MANA = 10;
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
    private ArrayList<CardInput> originalPlayerOneDeck;
    private ArrayList<CardInput> originalPlayerTwoDeck;

    /**
     *
     * @param playerOneDeck
     * @param playerTwoDeck
     * @param startGame
     */
    public GameSession(final ArrayList<CardInput> playerOneDeck,
                       final ArrayList<CardInput> playerTwoDeck, final StartGameInput startGame) {
        this.roundNumber = 0;
        this.originalPlayerOneDeck = new ArrayList<>(playerOneDeck);
        this.originalPlayerTwoDeck = new ArrayList<>(playerTwoDeck);
        this.playerOneDeck = playerOneDeck;
        this.playerTwoDeck = playerTwoDeck;
        this.gameBoard = new GameBoard();
        this.turnCompleted = 0;
        initializeGame(startGame);
    }

    /**
     *
     */
    public void reset() {
        playerOne.setMana(0);
        playerTwo.setMana(0);
        playerOne.getHero().setHealth(30);
        playerTwo.getHero().setHealth(30);
        playerOne.getPlayerHand().clear();
        playerTwo.getPlayerHand().clear();
        gameBoard.clear();
        this.roundNumber = 0;
        this.turnCompleted = 0;
        this.gameEnded = false;
    }

    /**
     *
     * @param startGame
     */
    public void initializeGame(final StartGameInput startGame) {
        Random random = new Random(startGame.getShuffleSeed());
        Collections.shuffle(playerOneDeck, random);
        Random random2 = new Random(startGame.getShuffleSeed());
        Collections.shuffle(playerTwoDeck, random2);
        Card playerOneHero = new Card(startGame.getPlayerOneHero());
        Card playerTwoHero = new Card(startGame.getPlayerTwoHero());
        this.playerOne = new Player(startGame.getPlayerOneDeckIdx(),
                playerOneHero, playerOneDeck, 0);
        this.playerTwo = new Player(startGame.getPlayerTwoDeckIdx(),
                playerTwoHero, playerTwoDeck, 0);
        this.playerOne.getHero().setHealth(INITIAL_HEALTH);
        this.playerTwo.getHero().setHealth(INITIAL_HEALTH);
        this.startingPlayer = startGame.getStartingPlayer();

        if (startingPlayer == 1) {
            this.currentPlayer = playerOne;
        } else {
            this.currentPlayer = playerTwo;
        }
        startRound();
    }

    /**
     *
     */
    public void startRound() {
        if (!this.gameEnded) {
            this.roundNumber++;
            updateMana(playerOne);
            updateMana(playerTwo);
            drawCard(playerOne);
            drawCard(playerTwo);
            this.turnCompleted = 0;
        }
    }

    /**
     *
     */
    public void endTurn() {
        if (!this.gameEnded) {
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
    }

    /**
     *
     */
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

    /**
     *
     * @param player
     */
    private void updateMana(final Player player) {
        int newMana = player.getMana() + roundNumber;
        if (player.getMana() <= MAX_MANA) {
            player.setMana(newMana);
        }
    }

    /**
     *
     * @param player
     */
    private void drawCard(final Player player) {
        if (!player.getDeck().isEmpty()) {
            CardInput newCard = player.getDeck().remove(0);
            player.getPlayerHand().add(newCard);
        }
    }

    /**
     *
     */
    public void resetDecks() {
        playerOneDeck = new ArrayList<>(originalPlayerOneDeck);
        playerTwoDeck = new ArrayList<>(originalPlayerTwoDeck);
    }

    /**
     *
     * @return
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     *
     */
    public void incrementRoundNumber() {
        this.roundNumber++;
    }

    /**
     *
     * @return
     */
    public GameBoard getGameBoard() {
        return gameBoard;
    }

    /**
     *
     * @return
     */
    public Player getPlayerOne() {
        return playerOne;
    }

    /**
     *
     * @return
     */
    public Player getPlayerTwo() {
        return playerTwo;
    }

    /**
     *
     * @return
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     *
     * @return
     */
    public int getCurrentPlayerIndex() {
        return currentPlayer == playerOne ? 1 : 2;
    }

    /**
     *
     * @return
     */
    public boolean isGameEnded() {
        return this.gameEnded;
    }

    /**
     *
     * @param val
     */
    public void setGameEnded(final boolean val) {
        this.gameEnded = val;
    }
}

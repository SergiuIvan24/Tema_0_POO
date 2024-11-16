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
    private final GameBoard gameBoard;
    private Player playerOne;
    private Player playerTwo;
    private Player currentPlayer;
    private int turnCompleted;
    private boolean gameEnded = false;
    private final ArrayList<CardInput> originalPlayerOneDeck;
    private final ArrayList<CardInput> originalPlayerTwoDeck;

    /**
     * Constructorul clasei GameSession.
     * Inițializează sesiunea de joc cu pachetele de cărți ale jucătorilor și datele de start.
     *
     * @param playerOneDeck pachetul de cărți al jucătorului 1
     * @param playerTwoDeck pachetul de cărți al jucătorului 2
     * @param startGame     datele pentru inițializarea jocului
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
     * Resetează sesiunea de joc la starea inițială.
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
     * Inițializează sesiunea de joc cu parametrii specificați.
     *
     * @param startGame datele pentru inițializarea jocului
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
        int startingPlayer = startGame.getStartingPlayer();

        if (startingPlayer == 1) {
            this.currentPlayer = playerOne;
        } else {
            this.currentPlayer = playerTwo;
        }
        startRound();
    }

    /**
     * Începe o nouă rundă de joc.
     * Actualizează mana și extrage câte o carte pentru fiecare jucător.
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
     * Marchează sfârșitul turei curente a jucătorului.
     * Schimbă jucătorul curent și începe o rundă nouă dacă este necesar.
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
     * Resetează starea de îngheț a cărților jucătorului curent.
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
     * Actualizează mana unui jucător, crescând-o cu numărul rundei curente.
     *
     * @param player jucătorul căruia i se actualizează mana
     */
    private void updateMana(final Player player) {
        int newMana = player.getMana() + roundNumber;
        if (player.getMana() <= MAX_MANA) {
            player.setMana(newMana);
        }
    }

    /**
     * Permite unui jucător să extragă o carte din propriul pachet.
     *
     * @param player jucătorul care extrage cartea
     */
    private void drawCard(final Player player) {
        if (!player.getDeck().isEmpty()) {
            CardInput newCard = player.getDeck().remove(0);
            player.getPlayerHand().add(newCard);
        }
    }

    /**
     * Resetează pachetele de cărți ale jucătorilor la starea inițială.
     */
    public void resetDecks() {
        playerOneDeck = new ArrayList<>(originalPlayerOneDeck);
        playerTwoDeck = new ArrayList<>(originalPlayerTwoDeck);
    }

    /**
     * Returnează numărul rundei curente.
     *
     * @return numărul rundei curente
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * Incrementează numărul rundei curente.
     */
    public void incrementRoundNumber() {
        this.roundNumber++;
    }

    /**
     * Returnează tabla de joc utilizată în sesiunea curentă.
     *
     * @return tabla de joc
     */
    public GameBoard getGameBoard() {
        return gameBoard;
    }

    /**
     * Returnează jucătorul 1.
     *
     * @return jucătorul 1
     */
    public Player getPlayerOne() {
        return playerOne;
    }

    /**
     * Returnează jucătorul 2.
     *
     * @return jucătorul 2
     */
    public Player getPlayerTwo() {
        return playerTwo;
    }

    /**
     * Returnează jucătorul curent.
     *
     * @return jucătorul curent
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Returnează indexul jucătorului curent (1 sau 2).
     *
     * @return indexul jucătorului curent
     */
    public int getCurrentPlayerIndex() {
        return currentPlayer == playerOne ? 1 : 2;
    }

    /**
     * Verifică dacă jocul s-a încheiat.
     *
     * @return true dacă jocul s-a încheiat, altfel false
     */
    public boolean isGameEnded() {
        return this.gameEnded;
    }

    /**
     * Setează starea jocului ca fiind încheiată sau nu.
     *
     * @param val true dacă jocul este încheiat, altfel false
     */
    public void setGameEnded(final boolean val) {
        this.gameEnded = val;
    }
}

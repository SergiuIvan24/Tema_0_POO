package game;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import fileio.ActionsInput;
import fileio.StartGameInput;

public final class GameMode {

    private static final int ROW_3 = 3;
    private static GameMode instance;
    private GameSession currentSession;
    private static final ObjectMapper ObjectMapper = new ObjectMapper();
    private ArrayList<GameSession> gameSessions = new ArrayList<>();
    private int nrGamesPlayed = 0;

    private GameMode() { }

    /**
     *
     * @return
     */
    public static GameMode getInstance() {
        if (instance == null) {
            instance = new GameMode();
        }
        return instance;
    }

    /**
     *
     * @return
     */
    public GameSession getCurrentSession() {
        return this.currentSession;
    }

    /**
     *
     * @return
     */
    public int getNrGamesPlayed() {
        return this.nrGamesPlayed;
    }

    /**
     *
     */
    public static void clearInstance() {
        instance = null;
    }

    /**
     *
     */
    public static void resetGame() {
        if (instance != null && instance.currentSession != null) {
            instance.currentSession.reset();
            instance.currentSession = null;
        }
        instance.nrGamesPlayed = 0;
        instance.gameSessions.clear();
    }


    /**
     *
     * @param playerOneDeck
     * @param playerTwoDeck
     * @param startGame
     */
    public void startNewSession(final ArrayList<CardInput> playerOneDeck,
                                final ArrayList<CardInput> playerTwoDeck,
                                final StartGameInput startGame) {
        System.out.println("startNewSession called");

        if (this.currentSession != null) {
            resetGame();
        }

        this.currentSession = new GameSession(playerOneDeck, playerTwoDeck, startGame);
        gameSessions.add(currentSession);
        nrGamesPlayed++;
    }

    public static void resetForTesting() {
        if (instance != null) {
            instance.nrGamesPlayed = 0;
            instance.gameSessions.clear();
            instance.currentSession = null;
        }
    }

    /**
     *
     * @param startGame
     */
    public void startNewGameWithSameDecks(final StartGameInput startGame) {
        currentSession.resetDecks();
        currentSession.initializeGame(startGame);
    }

    /**
     *
     * @param action
     * @param output
     */
    public void processAction(final ActionsInput action, final ArrayNode output) {
        switch (action.getCommand()) {
            case "endPlayerTurn":
                currentSession.endTurn();
                return;

            case "getPlayerDeck":
                ObjectNode actionResult = output.addObject();
                actionResult.put("command", action.getCommand());
                Player player = action.getPlayerIdx() == 1
                        ? currentSession.getPlayerOne()
                        : currentSession.getPlayerTwo();
                actionResult.put("playerIdx", action.getPlayerIdx());
                actionResult.set("output", convertDeckToJson(player.getDeck()));
                break;

            case "getPlayerHero":
                actionResult = output.addObject();
                actionResult.put("command", action.getCommand());
                Card hero = action.getPlayerIdx() == 1
                        ? currentSession.getPlayerOne().getHero()
                        : currentSession.getPlayerTwo().getHero();
                actionResult.put("playerIdx", action.getPlayerIdx());
                actionResult.set("output", convertHeroToJson(hero));
                break;

            case "getCardsInHand":
                actionResult = output.addObject();
                actionResult.put("command", action.getCommand());
                Player handPlayer = action.getPlayerIdx() == 1
                        ? currentSession.getPlayerOne()
                        : currentSession.getPlayerTwo();
                actionResult.put("playerIdx", action.getPlayerIdx());
                actionResult.set("output", convertCardsToJson(handPlayer.getPlayerHand()));
                break;

            case "getPlayerTurn":
                actionResult = output.addObject();
                actionResult.put("command", action.getCommand());
                actionResult.put("output", currentSession.getCurrentPlayerIndex());
                break;

            case "placeCard":
                Player currentPlayer = currentSession.getCurrentPlayer();
                if (action.getHandIdx() < 0
                        || action.getHandIdx() >= currentPlayer.getPlayerHand().size()) {
                    ObjectNode placeCardError = output.addObject();
                    placeCardError.put("command", action.getCommand());
                    placeCardError.put("handIdx", action.getHandIdx());
                    placeCardError.put("error", "Invalid hand index.");
                } else {
                    CardInput cardToPlace = currentPlayer.getPlayerHand().get(action.getHandIdx());
                    if (currentPlayer.getMana() < cardToPlace.getMana()) {
                        ObjectNode placeCardError = output.addObject();
                        placeCardError.put("command", action.getCommand());
                        placeCardError.put("handIdx", action.getHandIdx());
                        placeCardError.put("error", "Not enough mana to place card on table.");
                    } else {
                        int result = currentSession.getGameBoard().placeCard(cardToPlace,
                                     currentSession.getCurrentPlayerIndex(), currentPlayer);
                        if (result != 1) {
                            ObjectNode placeCardError = output.addObject();
                            placeCardError.put("command", action.getCommand());
                            placeCardError.put("handIdx", action.getHandIdx());
                            placeCardError.put("error",
                                    "Cannot place card on table since row is full.");
                        } else {
                            currentPlayer.setMana(currentPlayer.getMana() - cardToPlace.getMana());
                            currentPlayer.getPlayerHand().remove(action.getHandIdx());
                        }
                    }
                }
                break;

            case "getPlayerMana":
                actionResult = output.addObject();
                actionResult.put("command", action.getCommand());
                Player manaPlayer = action.getPlayerIdx() == 1
                        ? currentSession.getPlayerOne()
                        : currentSession.getPlayerTwo();
                actionResult.put("playerIdx", action.getPlayerIdx());
                actionResult.put("output", manaPlayer.getMana());
                break;

            case "getCardsOnTable":
                actionResult = output.addObject();
                actionResult.put("command", action.getCommand());
                ArrayNode cardsOnTable = currentSession.getGameBoard().getCardsOnTable();
                actionResult.set("output", cardsOnTable);
                break;

            case "cardUsesAttack":
                int xAttacker = action.getCardAttacker().getX();
                int yAttacker = action.getCardAttacker().getY();
                int xAttacked = action.getCardAttacked().getX();
                int yAttacked = action.getCardAttacked().getY();

                Card attacker = currentSession.getGameBoard().getCard(xAttacker, yAttacker);
                Card cardAttacked = currentSession.getGameBoard().getCard(xAttacked, yAttacked);


                if (attacker == null) {
                     actionResult = output.addObject();
                    actionResult.put("command", "cardUsesAttack");

                    ObjectNode attackerNode = ObjectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);

                    actionResult.put("error", "No card available at the specified position.");
                    return;
                }

                if ((currentSession.getCurrentPlayer() == currentSession.getPlayerOne()
                        && (xAttacked == 2 || xAttacked == ROW_3))
                        || (currentSession.getCurrentPlayer() == currentSession.getPlayerTwo()
                        && (xAttacked == 0 || xAttacked == 1))) {

                    actionResult = output.addObject();
                    actionResult.put("command", "cardUsesAttack");

                    ObjectNode attackerNode = ObjectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);

                    ObjectNode attackedNode = ObjectMapper.createObjectNode();
                    attackedNode.put("x", xAttacked);
                    attackedNode.put("y", yAttacked);
                    actionResult.set("cardAttacked", attackedNode);

                    actionResult.put("error", "Attacked card does not belong to the enemy.");
                    return;
                }

                if (currentSession.getGameBoard().
                        hasTankOnEnemyRows(currentSession.getCurrentPlayerIndex())) {
                    boolean validTankAttack = false;
                    List<Card> tanks =
                            currentSession.
                                    getGameBoard().
                                    getAllTanksOnEnemyRows(currentSession.getCurrentPlayerIndex());
                    for (Card tank : tanks) {
                        if (cardAttacked == tank) {
                            validTankAttack = true;
                            break;
                        }
                    }
                    if (!validTankAttack) {
                        actionResult = output.addObject();
                        actionResult.put("command", "cardUsesAttack");

                        ObjectNode attackerNode = ObjectMapper.createObjectNode();
                        attackerNode.put("x", xAttacker);
                        attackerNode.put("y", yAttacker);
                        actionResult.set("cardAttacker", attackerNode);

                        ObjectNode attackedNode = ObjectMapper.createObjectNode();
                        attackedNode.put("x", xAttacked);
                        attackedNode.put("y", yAttacked);
                        actionResult.set("cardAttacked", attackedNode);

                        actionResult.put("error", "Attacked card is not of type 'Tank'.");
                        return;
                    }
                }

                if (attacker.isHasAttacked()) {
                    actionResult = output.addObject();
                    actionResult.put("command", "cardUsesAttack");

                    ObjectNode attackerNode = ObjectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);

                    ObjectNode attackedNode = ObjectMapper.createObjectNode();
                    attackedNode.put("x", xAttacked);
                    attackedNode.put("y", yAttacked);
                    actionResult.set("cardAttacked", attackedNode);

                    actionResult.put("error", "Attacker card has already attacked this turn.");
                    return;
                }

                if (attacker.getFrozen()) {
                    actionResult = output.addObject();
                    actionResult.put("command", "cardUsesAttack");

                    ObjectNode attackerNode = ObjectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);

                    actionResult.put("error", "Attacker card is frozen.");
                    return;
                }

                currentSession.getGameBoard().attackCard(attacker, cardAttacked);

                attacker.setHasAttacked(true);

                break;

            case "getCardAtPosition":
                int x = action.getX();
                int y = action.getY();
                Card card = this.currentSession.getGameBoard().getCard(x, y);
                actionResult = output.addObject();
                actionResult.put("command", "getCardAtPosition");
                actionResult.put("x", x);
                actionResult.put("y", y);

                if (card == null) {
                    actionResult.put("output", "No card available at that position.");
                } else {
                    ObjectNode cardNode = convertCardToJson2(card);
                    actionResult.set("output", cardNode);
                }
                break;

            case "cardUsesAbility":
                xAttacker = action.getCardAttacker().getX();
                yAttacker = action.getCardAttacker().getY();
                xAttacked = action.getCardAttacked().getX();
                yAttacked = action.getCardAttacked().getY();

                attacker = currentSession.getGameBoard().getCard(xAttacker, yAttacker);
                cardAttacked = currentSession.getGameBoard().getCard(xAttacked, yAttacked);

                actionResult = output.addObject();
                actionResult.put("command", "cardUsesAbility");

                ObjectNode attackerNode = ObjectMapper.createObjectNode();
                attackerNode.put("x", xAttacker);
                attackerNode.put("y", yAttacker);
                actionResult.set("cardAttacker", attackerNode);

                ObjectNode attackedNode = ObjectMapper.createObjectNode();
                attackedNode.put("x", xAttacked);
                attackedNode.put("y", yAttacked);
                actionResult.set("cardAttacked", attackedNode);



                if (attacker.getFrozen()) {
                    actionResult.put("error", "Attacker card is frozen.");
                    return;
                }

                if (attacker.isHasAttacked()) {
                    actionResult.put("error", "Attacker card has already attacked this turn.");
                    return;
                }

                if (attacker.getName().equals("Disciple")) {
                    if ((currentSession.getCurrentPlayer() == currentSession.getPlayerOne()
                            && (xAttacked == 0 || xAttacked == 1))
                            || (currentSession.getCurrentPlayer() == currentSession.getPlayerTwo()
                                    && (xAttacked == 2 || xAttacked == ROW_3))) {
                        actionResult.put("error",
                                "Attacked card does not belong to the current player.");
                        return;
                    }
                }

                if (attacker.getName().equals("The Ripper")
                        || attacker.getName().equals("Miraj")
                        || attacker.getName().equals("The Cursed One")) {
                    if ((currentSession.getCurrentPlayer() == currentSession.getPlayerOne()
                            && (xAttacked == 2 || xAttacked == ROW_3))
                            || (currentSession.getCurrentPlayer() == currentSession.getPlayerTwo()
                                    && (xAttacked == 0 || xAttacked == 1))) {
                        actionResult.put("error",
                                "Attacked card does not belong to the enemy.");
                        return;
                    }
                }

                if (currentSession.getGameBoard().
                        hasTankOnEnemyRows(currentSession.getCurrentPlayerIndex())
                        && !attacker.getName().equals("Disciple")) {
                    List<Card> tanks = currentSession.getGameBoard().
                            getAllTanksOnEnemyRows(currentSession.getCurrentPlayerIndex());

                    boolean isTargetTank = false;

                    for (Card tank : tanks) {
                        if (cardAttacked == tank) {
                            isTargetTank = true;
                            break;
                        }
                    }

                    if (!isTargetTank) {
                        actionResult.put("error", "Attacked card is not of type 'Tank'.");
                        return;
                    }
                }

                output.remove(output.size() - 1);

                currentSession.getGameBoard().cardUsesAbility(attacker, cardAttacked);
                attacker.setHasAttacked(true);

                break;
            case "useAttackHero":
                if (this.currentSession.isGameEnded()) {
                    return;
                }

                xAttacker = action.getCardAttacker().getX();
                yAttacker = action.getCardAttacker().getY();

                attacker = currentSession.getGameBoard().getCard(xAttacker, yAttacker);

                if (attacker == null) {
                    actionResult = output.addObject();
                    actionResult.put("command", "useAttackHero");
                    attackerNode = ObjectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);
                    actionResult.put("error", "No card available at the specified position.");
                    return;
                }

                if (attacker.getFrozen()) {
                    actionResult = output.addObject();
                    actionResult.put("command", "useAttackHero");
                    attackerNode = ObjectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);
                    actionResult.put("error", "Attacker card is frozen.");
                    return;
                }

                if (attacker.isHasAttacked()) {
                    actionResult = output.addObject();
                    actionResult.put("command", "useAttackHero");
                    attackerNode = ObjectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);
                    actionResult.put("error", "Attacker card has already attacked this turn.");
                    return;
                }

                if (this.currentSession.getGameBoard().
                        hasTankOnEnemyRows(this.currentSession.getCurrentPlayerIndex())) {
                    actionResult = output.addObject();
                    actionResult.put("command", "useAttackHero");
                    attackerNode = ObjectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);
                    actionResult.put("error", "Attacked card is not of type 'Tank'.");
                    return;
                }

                Player attackedPlayer = (currentSession.getCurrentPlayerIndex() == 2)
                        ? this.currentSession.getPlayerOne()
                        : this.currentSession.getPlayerTwo();

                currentSession.getGameBoard().attackHero(attackedPlayer, attacker);


                attacker.setHasAttacked(true);

                if (this.currentSession.getGameBoard().isHeroDead(attackedPlayer)) {
                    actionResult = output.addObject();
                    if (this.currentSession.getCurrentPlayerIndex() == 1) {
                        actionResult.put("gameEnded", "Player one killed the enemy hero.");
                    } else {
                        actionResult.put("gameEnded", "Player two killed the enemy hero.");
                    }
                    this.currentSession.getCurrentPlayer().setNrWins(this.currentSession.getCurrentPlayer().getNrWins() + 1);
                    this.currentSession.setGameEnded(true);
                    return;
                }

                break;

            case "useHeroAbility":
                int affectedRow = action.getAffectedRow();
                if (this.currentSession.getCurrentPlayer().getMana()
                        < this.currentSession.getCurrentPlayer().getHero().getMana()) {
                    actionResult = output.addObject();
                    actionResult.put("command", "useHeroAbility");
                    actionResult.put("affectedRow", affectedRow);
                    actionResult.put("error", "Not enough mana to use hero's ability.");
                    return;
                }
                if (this.currentSession.getCurrentPlayer().getHero().isHasAttacked()) {
                    actionResult = output.addObject();
                    actionResult.put("command", "useHeroAbility");
                    actionResult.put("affectedRow", affectedRow);
                    actionResult.put("error", "Hero has already attacked this turn.");
                    return;
                }
                if ((this.currentSession.getCurrentPlayer().getHero().getName().equals("Lord Royce")
                        || this.currentSession.getCurrentPlayer().getHero()
                        .getName().equals("Empress Thorina"))
                        && this.currentSession.getCurrentPlayerIndex() == 1) {
                    if (affectedRow != 0 && affectedRow != 1) {
                        actionResult = output.addObject();
                        actionResult.put("command", "useHeroAbility");
                        actionResult.put("affectedRow", affectedRow);
                        actionResult.put("error", "Selected row does not belong to the enemy.");
                        return;
                    }
                }
                if ((this.currentSession.getCurrentPlayer().getHero().getName().equals("Lord Royce")
                        || this.currentSession.getCurrentPlayer().getHero()
                        .getName().equals("Empress Thorina"))
                        && this.currentSession.getCurrentPlayerIndex() == 2) {
                    if (affectedRow != 2 && affectedRow != ROW_3) {
                        actionResult = output.addObject();
                        actionResult.put("command", "useHeroAbility");
                        actionResult.put("affectedRow", affectedRow);
                        actionResult.put("error", "Selected row does not belong to the enemy.");
                        return;
                    }
                }

                if ((this.currentSession.getCurrentPlayer().getHero()
                        .getName().equals("General Kocioraw")
                || this.currentSession.getCurrentPlayer().getHero()
                        .getName().equals("King Mudface"))
                        && this.currentSession.getCurrentPlayerIndex() == 1) {
                    if (affectedRow != 2 && affectedRow != ROW_3) {
                        actionResult = output.addObject();
                        actionResult.put("command", "useHeroAbility");
                        actionResult.put("affectedRow", affectedRow);
                        actionResult.put("error", "Selected row does not belong to the current player.");
                        return;
                    }
                }
                if ((this.currentSession.getCurrentPlayer().getHero().getName().equals("General Kocioraw")
                        || this.currentSession.getCurrentPlayer().getHero().getName().equals("King Mudface"))
                        && this.currentSession.getCurrentPlayerIndex() == 2) {
                    if(affectedRow != 0 && affectedRow != 1){
                        actionResult = output.addObject();
                        actionResult.put("command", "useHeroAbility");
                        actionResult.put("affectedRow", affectedRow);
                        actionResult.put("error", "Selected row does not belong to the current player.");
                        return;
                    }
                }

                this.currentSession.getGameBoard().useHeroAbility(this.currentSession.getCurrentPlayer(), affectedRow);
                this.currentSession.getCurrentPlayer().getHero().setHasAttacked(true);

                break;

            case "getFrozenCardsOnTable":
                this.printFrozenCards(output);
                break;

            case "getTotalGamesPlayed":
                int games = this.getNrGamesPlayed();
                actionResult = output.addObject();
                actionResult.put("command", "getTotalGamesPlayed");
                actionResult.put("output", games);
                break;

            case "getPlayerOneWins":
                int wins = this.currentSession.getPlayerOne().getNrWins();
                actionResult = output.addObject();
                actionResult.put("command", "getPlayerOneWins");
                actionResult.put("output", wins);
                break;

            case "getPlayerTwoWins":
                int wins2 = this.currentSession.getPlayerTwo().getNrWins();
                actionResult = output.addObject();
                actionResult.put("command", "getPlayerTwoWins");
                actionResult.put("output", wins2);
                break;

            default:

                break;
        }
    }

    /**
     *
     * @param action
     * @param actionResult
     */
    private void placeCard(final ActionsInput action, final ObjectNode actionResult) {
        Player currentPlayer = currentSession.getCurrentPlayer();

        if (action.getHandIdx() < 0 || action.getHandIdx() >= currentPlayer.getPlayerHand().size()) {
            actionResult.put("handIdx", action.getHandIdx());
            actionResult.put("error", "Invalid hand index.");
            return;
        }

        CardInput cardToPlace = currentPlayer.getPlayerHand().get(action.getHandIdx());

        if (currentPlayer.getMana() < cardToPlace.getMana()) {
            actionResult.put("handIdx", action.getHandIdx());
            actionResult.put("error", "Not enough mana to place card on table.");
            return;
        }

        int result = currentSession.getGameBoard().placeCard(cardToPlace,
                currentSession.getCurrentPlayerIndex(), currentPlayer);

        if (result == 1) {
            currentPlayer.setMana(currentPlayer.getMana() - cardToPlace.getMana());
            currentPlayer.getPlayerHand().remove(action.getHandIdx());
        } else {
            actionResult.put("handIdx", action.getHandIdx());
            actionResult.put("error", "Cannot place card on table since row is full.");
        }
    }

    /**
     *
     * @param output
     */
    public void printFrozenCards(final ArrayNode output) {
        ArrayNode frozenCardsArray = ObjectMapper.createArrayNode();

        for (int row = 0; row < 4; row++) {
            ArrayList<Card> cardsInRow = currentSession.getGameBoard().getRow(row);
            for (Card card : cardsInRow) {
                if (card.getFrozen()) {
                    frozenCardsArray.add(convertCardToJson2(card));
                }
            }
        }

        ObjectNode actionResult = output.addObject();
        actionResult.put("command", "getFrozenCardsOnTable");
        actionResult.set("output", frozenCardsArray);
    }


    /**
     *
     * @param deck
     * @return
     */

    private ArrayNode convertDeckToJson(final ArrayList<CardInput> deck) {
        ArrayNode deckArray = ObjectMapper.createArrayNode();
        for (CardInput card : deck) {
            deckArray.add(convertCardToJson(card));
        }
        return deckArray;
    }

    /**
     *
     * @param card
     * @return
     */
    private ObjectNode convertCardToJson(final CardInput card) {
        ObjectNode cardNode = ObjectMapper.createObjectNode();
        cardNode.put("mana", card.getMana());
        cardNode.put("attackDamage", card.getAttackDamage());
        cardNode.put("health", card.getHealth());
        cardNode.put("description", card.getDescription());
        ArrayNode colors = ObjectMapper.createArrayNode();
        for (String color : card.getColors()) {
            colors.add(color);
        }
        cardNode.set("colors", colors);
        cardNode.put("name", card.getName());
        return cardNode;
    }

    /**
     *
     * @param card
     * @return
     */
    private ObjectNode convertCardToJson2(final Card card) {
        ObjectNode cardNode = ObjectMapper.createObjectNode();
        cardNode.put("mana", card.getMana());
        cardNode.put("attackDamage", card.getAttackDamage());
        cardNode.put("health", card.getHealth());
        cardNode.put("description", card.getDescription());
        ArrayNode colors = ObjectMapper.createArrayNode();
        for (String color : card.getColors()) {
            colors.add(color);
        }
        cardNode.set("colors", colors);
        cardNode.put("name", card.getName());
        return cardNode;
    }

    /**
     *
     * @param cards
     * @return
     */
    private ArrayNode convertCardsToJson(final ArrayList<CardInput> cards) {
        ArrayNode cardsArray = ObjectMapper.createArrayNode();
        for (CardInput card : cards) {
            cardsArray.add(convertCardToJson(card));
        }
        return cardsArray;
    }

    /**
     *
     * @param hero
     * @return
     */
    private ObjectNode convertHeroToJson(final Card hero) {
        ObjectNode heroNode = ObjectMapper.createObjectNode();
        heroNode.put("mana", hero.getMana());
        heroNode.put("description", hero.getDescription());
        ArrayNode colors = ObjectMapper.createArrayNode();
        for (String color : hero.getColors()) {
            colors.add(color);
        }
        heroNode.set("colors", colors);
        heroNode.put("name", hero.getName());
        heroNode.put("health", hero.getHealth());
        return heroNode;
    }

}

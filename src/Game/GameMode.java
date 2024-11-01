package Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import fileio.ActionsInput;
import fileio.DecksInput;
import fileio.StartGameInput;

public class GameMode {
    private static GameMode instance;
    private GameSession currentSession;
    private static final ObjectMapper objectMapper = new ObjectMapper();


    private GameMode() {
    }

    public static GameMode getInstance() {
        if (instance == null) {
            instance = new GameMode();
        }
        return instance;
    }

    public static void resetGame() {
        System.out.println("resetGame called");
        if (instance != null) {
            instance.currentSession = null;
        }
    }

    public void startNewSession(ArrayList<CardInput> playerOneDeck, ArrayList<CardInput> playerTwoDeck, StartGameInput startGame) {
        System.out.println("startNewSession called");
        this.currentSession = new GameSession(playerOneDeck, playerTwoDeck, startGame);
    }


    public void processAction(ActionsInput action, ArrayNode output) {
        switch (action.getCommand()) {
            case "endPlayerTurn":
                currentSession.endTurn();
                return;

            case "getPlayerDeck":
                ObjectNode actionResult = output.addObject();
                actionResult.put("command", action.getCommand());
                Player player = action.getPlayerIdx() == 1 ? currentSession.getPlayerOne() : currentSession.getPlayerTwo();
                actionResult.put("playerIdx", action.getPlayerIdx());
                actionResult.set("output", convertDeckToJson(player.getDeck()));
                break;

            case "getPlayerHero":
                actionResult = output.addObject();
                actionResult.put("command", action.getCommand());
                CardInput hero = action.getPlayerIdx() == 1 ? currentSession.getPlayerOne().getHero() : currentSession.getPlayerTwo().getHero();
                actionResult.put("playerIdx", action.getPlayerIdx());
                actionResult.set("output", convertHeroToJson(hero));
                break;

            case "getCardsInHand":
                actionResult = output.addObject();
                actionResult.put("command", action.getCommand());
                Player handPlayer = action.getPlayerIdx() == 1 ? currentSession.getPlayerOne() : currentSession.getPlayerTwo();
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
                if (action.getHandIdx() < 0 || action.getHandIdx() >= currentPlayer.getPlayerHand().size()) {
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
                        int result = currentSession.getGameBoard().placeCard(cardToPlace, currentSession.getCurrentPlayerIndex(), currentPlayer);
                        if (result != 1) {
                            ObjectNode placeCardError = output.addObject();
                            placeCardError.put("command", action.getCommand());
                            placeCardError.put("handIdx", action.getHandIdx());
                            placeCardError.put("error", "Cannot place card on table since row is full.");
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
                Player manaPlayer = action.getPlayerIdx() == 1 ? currentSession.getPlayerOne() : currentSession.getPlayerTwo();
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

                // Verificări pentru validarea atacului
                if ((currentSession.getCurrentPlayer() == currentSession.getPlayerOne() && (xAttacked == 2 || xAttacked == 3)) ||
                        (currentSession.getCurrentPlayer() == currentSession.getPlayerTwo() && (xAttacked == 0 || xAttacked == 1))) {

                    actionResult = output.addObject();
                    actionResult.put("command", "cardUsesAttack");

                    ObjectNode attackerNode = objectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);

                    ObjectNode attackedNode = objectMapper.createObjectNode();
                    attackedNode.put("x", xAttacked);
                    attackedNode.put("y", yAttacked);
                    actionResult.set("cardAttacked", attackedNode);

                    actionResult.put("error", "Attacked card does not belong to the enemy.");
                    return;
                }

                if (currentSession.getGameBoard().hasTankOnEnemyRows(currentSession.getCurrentPlayerIndex())) {
                    boolean validTankAttack = false;
                    List<Card> tanks = currentSession.getGameBoard().getAllTanksOnEnemyRows(currentSession.getCurrentPlayerIndex());
                    for (Card tank : tanks) {
                        if (cardAttacked == tank) {
                            validTankAttack = true;
                            break;
                        }
                    }
                    if (!validTankAttack) {
                        actionResult = output.addObject();
                        actionResult.put("command", "cardUsesAttack");

                        ObjectNode attackerNode = objectMapper.createObjectNode();
                        attackerNode.put("x", xAttacker);
                        attackerNode.put("y", yAttacker);
                        actionResult.set("cardAttacker", attackerNode);

                        ObjectNode attackedNode = objectMapper.createObjectNode();
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

                    ObjectNode attackerNode = objectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);

                    ObjectNode attackedNode = objectMapper.createObjectNode();
                    attackedNode.put("x", xAttacked);
                    attackedNode.put("y", yAttacked);
                    actionResult.set("cardAttacked", attackedNode);

                    actionResult.put("error", "Attacker card has already attacked this turn.");
                    return;
                }

                if (attacker.getFrozen()) {
                    actionResult = output.addObject();
                    actionResult.put("command", "cardUsesAttack");

                    ObjectNode attackerNode = objectMapper.createObjectNode();
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

                ObjectNode attackerNode = objectMapper.createObjectNode();
                attackerNode.put("x", xAttacker);
                attackerNode.put("y", yAttacker);
                actionResult.set("cardAttacker", attackerNode);

                ObjectNode attackedNode = objectMapper.createObjectNode();
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
                    if ((currentSession.getCurrentPlayer() == currentSession.getPlayerOne() && (xAttacked == 0 || xAttacked == 1)) ||
                            (currentSession.getCurrentPlayer() == currentSession.getPlayerTwo() && (xAttacked == 2 || xAttacked == 3))) {
                        actionResult.put("error", "Attacked card does not belong to the current player.");
                        return;
                    }
                }

                if (attacker.getName().equals("The Ripper") || attacker.getName().equals("Miraj") || attacker.getName().equals("The Cursed One")) {
                    if ((currentSession.getCurrentPlayer() == currentSession.getPlayerOne() && (xAttacked == 2 || xAttacked == 3)) ||
                            (currentSession.getCurrentPlayer() == currentSession.getPlayerTwo() && (xAttacked == 0 || xAttacked == 1))) {
                        actionResult.put("error", "Attacked card does not belong to the enemy.");
                        return;
                    }
                }

                if (currentSession.getGameBoard().hasTankOnEnemyRows(currentSession.getCurrentPlayerIndex()) && !attacker.getName().equals("Disciple")) {
                    List<Card> tanks = currentSession.getGameBoard().getAllTanksOnEnemyRows(currentSession.getCurrentPlayerIndex());

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
                    attackerNode = objectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);
                    actionResult.put("error", "No card available at the specified position.");
                    return;
                }

                if (attacker.getFrozen()) {
                    actionResult = output.addObject();
                    actionResult.put("command", "useAttackHero");
                    attackerNode = objectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);
                    actionResult.put("error", "Attacker card is frozen.");
                    return;
                }

                if (attacker.isHasAttacked()) {
                    actionResult = output.addObject();
                    actionResult.put("command", "useAttackHero");
                    attackerNode = objectMapper.createObjectNode();
                    attackerNode.put("x", xAttacker);
                    attackerNode.put("y", yAttacker);
                    actionResult.set("cardAttacker", attackerNode);
                    actionResult.put("error", "Attacker card has already attacked this turn.");
                    return;
                }

                if (this.currentSession.getGameBoard().hasTankOnEnemyRows(this.currentSession.getCurrentPlayerIndex())) {
                    actionResult = output.addObject();
                    actionResult.put("command", "useAttackHero");
                    attackerNode = objectMapper.createObjectNode();
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
                    this.currentSession.setGameEnded(true);
                    return;
                }


                break;
            default:

                break;
        }
    }

    private void placeCard(ActionsInput action, ObjectNode actionResult) {
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

        int result = currentSession.getGameBoard().placeCard(cardToPlace, currentSession.getCurrentPlayerIndex(), currentPlayer);

        if (result == 1) {
            currentPlayer.setMana(currentPlayer.getMana() - cardToPlace.getMana());
            currentPlayer.getPlayerHand().remove(action.getHandIdx());
        } else {
            actionResult.put("handIdx", action.getHandIdx());
            actionResult.put("error", "Cannot place card on table since row is full.");
        }
    }

    private ArrayNode convertDeckToJson(ArrayList<CardInput> deck) {
        ArrayNode deckArray = objectMapper.createArrayNode();
        for (CardInput card : deck) {
            deckArray.add(convertCardToJson(card));
        }
        return deckArray;
    }

    private ObjectNode convertCardToJson(CardInput card) {
        ObjectNode cardNode = objectMapper.createObjectNode();
        cardNode.put("mana", card.getMana());
        cardNode.put("attackDamage", card.getAttackDamage());
        cardNode.put("health", card.getHealth());
        cardNode.put("description", card.getDescription());
        ArrayNode colors = objectMapper.createArrayNode();
        for (String color : card.getColors()) {
            colors.add(color);
        }
        cardNode.set("colors", colors);
        cardNode.put("name", card.getName());
        return cardNode;
    }

    private ObjectNode convertCardToJson2(Card card) {
        ObjectNode cardNode = objectMapper.createObjectNode();
        cardNode.put("mana", card.getMana());
        cardNode.put("attackDamage", card.getAttackDamage());
        cardNode.put("health", card.getHealth());
        cardNode.put("description", card.getDescription());
        ArrayNode colors = objectMapper.createArrayNode();
        for (String color : card.getColors()) {
            colors.add(color);
        }
        cardNode.set("colors", colors);
        cardNode.put("name", card.getName());
        return cardNode;
    }

    private ArrayNode convertCardsToJson(ArrayList<CardInput> cards) {
        ArrayNode cardsArray = objectMapper.createArrayNode();
        for (CardInput card : cards) {
            cardsArray.add(convertCardToJson(card));
        }
        return cardsArray;
    }

    private ObjectNode convertHeroToJson(CardInput hero) {
        ObjectNode heroNode = objectMapper.createObjectNode();
        heroNode.put("mana", hero.getMana());
        heroNode.put("description", hero.getDescription());
        ArrayNode colors = objectMapper.createArrayNode();
        for (String color : hero.getColors()) {
            colors.add(color);
        }
        heroNode.set("colors", colors);
        heroNode.put("name", hero.getName());
        heroNode.put("health", hero.getHealth());
        return heroNode;
    }

}

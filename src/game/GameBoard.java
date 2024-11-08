package game;

import java.util.ArrayList;
import  fileio.CardInput;
import fileio.Coordinates;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class GameBoard {
    private static final int BOARD_ROWS = 4;
    private static final int MAX_CARDS_PER_ROW = 5;
    private static final int TANK_ROWS_START_PLAYER_ONE = 0;
    private static final int TANK_ROWS_END_PLAYER_ONE = 1;
    private static final int TANK_ROWS_START_PLAYER_TWO = 2;
    private static final int TANK_ROWS_END_PLAYER_TWO = 3;
    private static final int NOT_ENOUGH_MANA_ERR_NR = -2;
    private static final int MAX_ROW_PL_ONE = 3;

    private ArrayList<ArrayList<Card>> board;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     *
     */
    public GameBoard() {
        this.board = new ArrayList<>(BOARD_ROWS);
        for (int i = 0; i < BOARD_ROWS; i++) {
            board.add(new ArrayList<>());
        }
    }

    /**
     *
     */
    public void clear() {
        for (ArrayList<Card> row : board) {
            row.clear();
        }
    }

    /**
     *
     * @param card
     * @param playerIndex
     * @param currentPlayer
     * @return
     */
    public int placeCard(final CardInput card, final int playerIndex, final Player currentPlayer) {
        if (currentPlayer.getMana() < card.getMana()) {
            return NOT_ENOUGH_MANA_ERR_NR;
        }

        Coordinates coords = getPlayerRowColumn(playerIndex, card);
        if (coords.getX() == -1) {
            return -1;
        }

        int row = coords.getX();
        Card card2 = new Card(card);
        board.get(row).add(card2);
        return 1;
    }

    /**
     *
     * @param playerIndex
     * @param card
     * @return
     */
    private Coordinates getPlayerRowColumn(final int playerIndex, final CardInput card) {
        Coordinates coords = new Coordinates();

        if (playerIndex == 1) {
            if (card.getName().equals("Sentinel") || card.getName().equals("Berserker")
                    || card.getName().equals("Disciple")
                    || card.getName().equals("The Cursed One")) {
                if (board.get(MAX_ROW_PL_ONE).size() < MAX_CARDS_PER_ROW) {
                    coords.setX(MAX_ROW_PL_ONE);
                    coords.setY(board.get(MAX_ROW_PL_ONE).size());
                    return coords;
                }
            } else if (card.getName().equals("Goliath") || card.getName().equals("Warden")
                    || card.getName().equals("Miraj") || card.getName().equals("The Ripper")) {
                if (board.get(2).size() < MAX_CARDS_PER_ROW) {
                    coords.setX(2);
                    coords.setY(board.get(2).size());
                    return coords;
                }
            }
        } else if (playerIndex == 2) {
            if (card.getName().equals("Sentinel") || card.getName().equals("Berserker")
                    || card.getName().equals("Disciple")
                    || card.getName().equals("The Cursed One")) {
                if (board.get(0).size() < MAX_CARDS_PER_ROW) {
                    coords.setX(0);
                    coords.setY(board.get(0).size());
                    return coords;
                }
            } else if (card.getName().equals("Goliath") || card.getName().equals("Warden")
                    || card.getName().equals("Miraj") || card.getName().equals("The Ripper")) {
                if (board.get(1).size() < MAX_CARDS_PER_ROW) {
                    coords.setX(1);
                    coords.setY(board.get(1).size());
                    return coords;
                }
            }
        }

        coords.setX(-1);
        coords.setY(-1);
        return coords;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    public Card getCard(final int row, final int col) {
        if (row >= 0 && row < board.size() && col >= 0 && col < board.get(row).size()) {
            return board.get(row).get(col);
        }
        return null;
    }

    /**
     *
     * @param attacker
     * @param cardAttacked
     */
    public void attackCard(final Card attacker, final Card cardAttacked) {
        int newHealth = cardAttacked.getHealth() - attacker.getAttackDamage();
        if (newHealth <= 0) {
            removeCard(cardAttacked);
        } else {
            cardAttacked.setHealth(newHealth);
        }
    }

    /**
     *
     * @param cardToRemove
     */
    public void removeCard(final Card cardToRemove) {
        for (ArrayList<Card> row : board) {
            if (row.remove(cardToRemove)) {
                break;
            }
        }
    }

    /**
     *
     * @return
     */
    public int getRows() {
        return board.size();
    }

    /**
     *
     * @param row
     * @return
     */
    public int getCols(final int row) {
        return row >= 0 && row < board.size() ? board.get(row).size() : 0;
    }

    /**
     *
     * @return
     */
    public ArrayNode getCardsOnTable() {
        ArrayNode cardsOnTable = OBJECT_MAPPER.createArrayNode();

        for (int row = 0; row < board.size(); row++) {
            ArrayNode rowArray = OBJECT_MAPPER.createArrayNode();
            for (int col = 0; col < board.get(row).size(); col++) {
                Card card = board.get(row).get(col);
                if (card != null) {
                    rowArray.add(convertCardToJson(card));
                }
            }
            cardsOnTable.add(rowArray);
        }

        return cardsOnTable;
    }

    /**
     *
     */
    public void resetAttack() {
        for (int row = 0; row < board.size(); row++) {
            for (int col = 0; col < board.get(row).size(); col++) {
                Card card = board.get(row).get(col);
                card.setHasAttacked(false);
            }
        }
    }

    /**
     *
     * @param player
     */
    public void resetHeroAttack(final Player player) {
        player.getHero().setHasAttacked(false);
    }

    /**
     *
     * @param attacker
     * @param cardAttacked
     */
    public void cardUsesAbility(final Card attacker, final Card cardAttacked) {
        if (attacker.getName().equals("The Ripper")) {
            int newAttackDamage = Math.max(cardAttacked.getAttackDamage() - 2, 0);
            cardAttacked.setAttackDamage(newAttackDamage);
            return;
        }
        if (attacker.getName().equals("Miraj")) {
            int auxHealth = cardAttacked.getHealth();
            cardAttacked.setHealth(attacker.getHealth());
            attacker.setHealth(auxHealth);
            return;
        }
        if (attacker.getName().equals("The Cursed One")) {
            int auxHealth = cardAttacked.getHealth();
            int auxAttack = cardAttacked.getAttackDamage();
            cardAttacked.setAttackDamage(auxHealth);
            cardAttacked.setHealth(auxAttack);
            if (cardAttacked.getAttackDamage() == 0) {
                cardAttacked.setHealth(0);
                removeCard(cardAttacked);
            }
            return;
        }
        if (attacker.getName().equals("Disciple")) {
            cardAttacked.setHealth(cardAttacked.getHealth() + 2);
        }
    }

    private ObjectNode convertCardToJson(final Card card) {
        ObjectNode cardNode = OBJECT_MAPPER.createObjectNode();
        cardNode.put("mana", card.getMana());
        cardNode.put("attackDamage", card.getAttackDamage());
        cardNode.put("health", card.getHealth());
        cardNode.put("description", card.getDescription());
        ArrayNode colors = OBJECT_MAPPER.createArrayNode();
        for (String color : card.getColors()) {
            colors.add(color);
        }
        cardNode.set("colors", colors);
        cardNode.put("name", card.getName());
        return cardNode;
    }

    /**
     *
     * @param playerIndex
     * @return
     */
    public boolean hasTankOnEnemyRows(final int playerIndex) {
        int startRow;
        int endRow;

        if (playerIndex == 1) {
            startRow = TANK_ROWS_START_PLAYER_ONE;
            endRow = TANK_ROWS_END_PLAYER_ONE;
        } else {
            startRow = TANK_ROWS_START_PLAYER_TWO;
            endRow = TANK_ROWS_END_PLAYER_TWO;
        }

        for (int row = startRow; row <= endRow; row++) {
            for (Card card : board.get(row)) {
                if (card.getName().equals("Goliath") || card.getName().equals("Warden")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param playerIndex
     * @return
     */
    public Card getTankOnEnemyRows(final int playerIndex) {
        int startRow;
        int endRow;

        if (playerIndex == 1) {
            startRow = TANK_ROWS_START_PLAYER_ONE;
            endRow = TANK_ROWS_END_PLAYER_ONE;
        } else {
            startRow = TANK_ROWS_START_PLAYER_TWO;
            endRow = TANK_ROWS_END_PLAYER_TWO;
        }

        for (int row = startRow; row <= endRow; row++) {
            for (Card card : board.get(row)) {
                if (card.getName().equals("Goliath") || card.getName().equals("Warden")) {
                    return card;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param playerIndex
     * @return
     */
    public ArrayList<Card> getAllTanksOnEnemyRows(final int playerIndex) {
        int startRow;
        int endRow;

        if (playerIndex == 1) {
            startRow = TANK_ROWS_START_PLAYER_ONE;
            endRow = TANK_ROWS_END_PLAYER_ONE;
        } else {
            startRow = TANK_ROWS_START_PLAYER_TWO;
            endRow = TANK_ROWS_END_PLAYER_TWO;
        }

        ArrayList<Card> tanks = new ArrayList<>();
        for (int row = startRow; row <= endRow; row++) {
            for (Card card : board.get(row)) {
                if (card.getName().equals("Goliath") || card.getName().equals("Warden")) {
                    tanks.add(card);
                }
            }
        }

        return tanks;
    }

    /**
     *
     * @param attackedPlayer
     * @param attackerCard
     */
    public void attackHero(final Player attackedPlayer,
                           final Card attackerCard) {
        attackedPlayer.getHero()
                .setHealth(attackedPlayer.getHero().getHealth() - attackerCard.getAttackDamage());
    }

    /**
     *
     * @param player
     * @return
     */
    public boolean isHeroDead(final Player player) {
        return player.getHero().getHealth() <= 0;
    }

    /**
     *
     * @param attacker
     * @param attackedRow
     */
    public void useHeroAbility(final Player attacker, final int attackedRow) {
        attacker.setMana(attacker.getMana() - attacker.getHero().getMana());

        if (attacker.getHero().getName().equals("Lord Royce")) {
            for (Card card : board.get(attackedRow)) {
                card.setFrozen(true);
            }
            return;
        }

        if (attacker.getHero().getName().equals("Empress Thorina")) {
            int maxi = -1;
            Card maxCard = null;
            for (Card card : board.get(attackedRow)) {
                if (card.getHealth() > maxi) {
                    maxi = card.getHealth();
                    maxCard = card;
                }
            }
            if (maxCard != null) {
                maxCard.setHealth(0);
            }
            removeCard(maxCard);
            return;
        }
        if (attacker.getHero().getName().equals("King Mudface")) {
            for (Card card : board.get(attackedRow)) {
                card.setHealth(card.getHealth() + 1);
            }
            return;
        }
        if (attacker.getHero().getName().equals("General Kocioraw")) {
            for (Card card : board.get(attackedRow)) {
                card.setAttackDamage(card.getAttackDamage() + 1);
            }
        }
    }

    /**
     *
     * @param row
     * @return
     */
    public ArrayList<Card> getRow(final int row) {
        if (row >= 0 && row < BOARD_ROWS) {
            return board.get(row);
        }
        return new ArrayList<>();
    }
}

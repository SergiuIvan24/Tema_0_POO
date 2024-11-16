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

    private final ArrayList<ArrayList<Card>> board;
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
     * Plasează o carte pe tabla de joc dacă jucătorul are suficientă mană.
     *
     * @param card         cartea care trebuie plasată
     * @param playerIndex  indicele jucătorului curent
     * @param currentPlayer jucătorul care efectuează acțiunea
     * @return -2 dacă mana este insuficientă, -1 dacă locul este invalid, 1 dacă plasarea reușește
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
     * Determină rândul și coloana unde poate fi plasată o carte pe tabla de joc.
     *
     * @param playerIndex indicele jucătorului curent
     * @param card        cartea care trebuie plasată
     * @return coordonatele rândului și coloanei, sau coordonate invalide (-1, -1) dacă nu există spațiu
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
     * Returnează cartea de pe o anumită poziție de pe tablă.
     *
     * @param row rândul căutat
     * @param col coloana căutată
     * @return cartea de pe poziția specificată sau null dacă nu există
     */
    public Card getCard(final int row, final int col) {
        if (row >= 0 && row < board.size() && col >= 0 && col < board.get(row).size()) {
            return board.get(row).get(col);
        }
        return null;
    }

    /**
     * Realizează un atac asupra unei cărți, scăzând viața acesteia.
     * Elimină cartea de pe tablă dacă viața ajunge la 0 sau mai puțin.
     *
     * @param attacker     cartea care atacă
     * @param cardAttacked cartea atacată
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
     * Elimină o carte specificată de pe tablă.
     *
     * @param cardToRemove cartea care trebuie eliminată
     */
    public void removeCard(final Card cardToRemove) {
        for (ArrayList<Card> row : board) {
            if (row.remove(cardToRemove)) {
                break;
            }
        }
    }

    /**
     * Returnează numărul de rânduri de pe tablă.
     *
     * @return numărul total de rânduri
     */
    public int getRows() {
        return board.size();
    }

    /**
     * Returnează numărul de coloane ocupate pe un rând specificat.
     *
     * @param row rândul dorit
     * @return numărul de cărți de pe rândul respectiv sau 0 dacă rândul este invalid
     */
    public int getCols(final int row) {
        return row >= 0 && row < board.size() ? board.get(row).size() : 0;
    }

    /**
     * Creează o reprezentare JSON a cărților de pe tablă.
     *
     * @return un nod JSON care conține cărțile de pe fiecare rând al tablei
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
     * Resetează starea de atac pentru toate cărțile de pe tablă.
     * Setează `hasAttacked` la `false` pentru fiecare carte.
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
     * Resetează starea de atac a eroului unui jucător.
     *
     * @param player jucătorul al cărui erou trebuie resetat
     */
    public void resetHeroAttack(final Player player) {
        player.getHero().setHasAttacked(false);
    }

    /**
     * Aplică abilitatea unei cărți asupra unei alte cărți, în funcție de tipul acesteia.
     *
     * @param attacker     cartea care folosește abilitatea
     * @param cardAttacked cartea asupra căreia se aplică abilitatea
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
     * Verifică dacă există un "tank" pe rândurile inamicului.
     *
     * @param playerIndex indicele jucătorului curent
     * @return true dacă există un "tank", altfel false
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
     * Returnează primul "tank" de pe rândurile inamicului, dacă există.
     *
     * @param playerIndex indicele jucătorului curent
     * @return cartea de tip "tank" sau null dacă nu există
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
     * Returnează toate cărțile de tip "tank" de pe rândurile inamicului.
     *
     * @param playerIndex indicele jucătorului curent
     * @return lista cărților de tip "tank"
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
     * Realizează un atac asupra eroului unui jucător.
     *
     * @param attackedPlayer jucătorul al cărui erou este atacat
     * @param attackerCard   cartea care efectuează atacul
     */
    public void attackHero(final Player attackedPlayer,
                           final Card attackerCard) {
        attackedPlayer.getHero()
                .setHealth(attackedPlayer.getHero().getHealth() - attackerCard.getAttackDamage());
    }

    /**
     * Verifică dacă eroul unui jucător a murit.
     *
     * @param player jucătorul al cărui erou este verificat
     * @return true dacă eroul este mort, altfel false
     */
    public boolean isHeroDead(final Player player) {
        return player.getHero().getHealth() <= 0;
    }

    /**
     * Aplică abilitatea unui erou asupra unui rând specificat de pe tablă.
     *
     * @param attacker   jucătorul care folosește abilitatea
     * @param attackedRow rândul asupra căruia se aplică abilitatea
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
     * Returnează toate cărțile de pe un rând specificat al tablei.
     *
     * @param row rândul dorit
     * @return lista cărților de pe rândul specificat sau o listă goală dacă rândul este invalid
     */
    public ArrayList<Card> getRow(final int row) {
        if (row >= 0 && row < BOARD_ROWS) {
            return board.get(row);
        }
        return new ArrayList<>();
    }
}

package Game;

import java.util.ArrayList;
import  fileio.CardInput;
import fileio.Coordinates;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GameBoard {
    private ArrayList<ArrayList<Card>> board;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public GameBoard() {
        this.board = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            board.add(new ArrayList<>());
        }
    }

    public int placeCard(CardInput card, int playerIndex, Player currentPlayer) {

        if (currentPlayer.getMana() < card.getMana()) {
            return -2;
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

    private Coordinates getPlayerRowColumn(int playerIndex, CardInput card) {
        Coordinates coords = new Coordinates();

        if (playerIndex == 1) {
            if (card.getName().equals("Sentinel") || card.getName().equals("Berserker") ||
                    card.getName().equals("Disciple") || card.getName().equals("The Cursed One")) {
                if (board.get(3).size() < 5) {
                    coords.setX(3);
                    coords.setY(board.get(3).size());
                    return coords;
                }
            } else if (card.getName().equals("Goliath") || card.getName().equals("Warden") ||
                    card.getName().equals("Miraj") || card.getName().equals("The Ripper")) {
                if (board.get(2).size() < 5) {
                    coords.setX(2);
                    coords.setY(board.get(2).size());
                    return coords;
                }
            }
        } else if (playerIndex == 2) {
            if (card.getName().equals("Sentinel") || card.getName().equals("Berserker") ||
                    card.getName().equals("Disciple") || card.getName().equals("The Cursed One")) {
                if (board.get(0).size() < 5) {
                    coords.setX(0);
                    coords.setY(board.get(0).size());
                    return coords;
                }
            } else if (card.getName().equals("Goliath") || card.getName().equals("Warden") ||
                    card.getName().equals("Miraj") || card.getName().equals("The Ripper")) {
                if (board.get(1).size() < 5) {
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

    public Card getCard(int row, int col) {
        if (row >= 0 && row < board.size() && col >= 0 && col < board.get(row).size()) {
            return board.get(row).get(col);
        }
        return null;
    }

    public void attackCard(Card attacker, Card cardAttacked) {
        int newHealth = cardAttacked.getHealth() - attacker.getAttackDamage();
        if (newHealth <= 0) {
            removeCard(cardAttacked);
        } else {
            cardAttacked.setHealth(newHealth);
        }
    }

    public void removeCard(Card cardToRemove) {
        for (ArrayList<Card> row : board) {
            if (row.remove(cardToRemove)) {
                break;
            }
        }
    }

    public int getRows() {
        return board.size();
    }

    public int getCols(int row) {
        return row >= 0 && row < board.size() ? board.get(row).size() : 0;
    }

    public ArrayNode getCardsOnTable() {
        ArrayNode cardsOnTable = objectMapper.createArrayNode();

        for (int row = 0; row < board.size(); row++) {
            ArrayNode rowArray = objectMapper.createArrayNode();
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

    public void resetAttack(){
        for (int row = 0; row < board.size(); row++) {
            for (int col = 0; col < board.get(row).size(); col++) {
                Card card = board.get(row).get(col);
                card.setHasAttacked(false);
            }
        }
    }

    public void resetHeroAttack(Player player) {
        player.getHero().setHasAttacked(false);
    }

    public void cardUsesAbility(Card attacker, Card cardAttacked) {
        if(attacker.getName().equals("The Ripper")){
            int newAttackDamage = Math.max(cardAttacked.getAttackDamage() - 2, 0);
            cardAttacked.setAttackDamage(newAttackDamage);
            return;
        }
        if(attacker.getName().equals("Miraj")){
            int auxHealth = cardAttacked.getHealth();
            cardAttacked.setHealth(attacker.getHealth());
            attacker.setHealth(auxHealth);
            return;
        }
        if(attacker.getName().equals("The Cursed One")){
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
        if(attacker.getName().equals("Disciple")){
            cardAttacked.setHealth(cardAttacked.getHealth() + 2);
        }
    }

    private ObjectNode convertCardToJson(Card card) {
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

    public boolean hasTankOnEnemyRows(int playerIndex) {
        int startRow, endRow;

        if (playerIndex == 1) {
            startRow = 0;
            endRow = 1;
        } else {
            startRow = 2;
            endRow = 3;
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

    public Card getTankOnEnemyRows(int playerIndex){
        int startRow, endRow;

        if (playerIndex == 1) {
            startRow = 0;
            endRow = 1;
        } else {
            startRow = 2;
            endRow = 3;
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

    public ArrayList<Card> getAllTanksOnEnemyRows(int playerIndex) {
        int startRow, endRow;

        if (playerIndex == 1) {
            startRow = 0;
            endRow = 1;
        } else {
            startRow = 2;
            endRow = 3;
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

    public void attackHero(Player attackedPlayer, Card attackerCard) {
        attackedPlayer.getHero().setHealth(attackedPlayer.getHero().getHealth() - attackerCard.getAttackDamage());
    }

    public boolean isHeroDead(Player player){
        if(player.getHero().getHealth() <= 0){
            return true;
        }
        return false;
    }

    public void useHeroAbility(Player attacker, int attackedRow) {

        attacker.setMana(attacker.getMana() - attacker.getHero().getMana());

        if(attacker.getHero().getName().equals("Lord Royce")){
            for(Card card : board.get(attackedRow)){
                card.setFrozen(true);
            }
            return;
        }

        if(attacker.getHero().getName().equals("Empress Thorina")){
            int maxi = -1;
            Card maxCard = null;
            for(Card card : board.get(attackedRow)){
                if(card.getHealth() > maxi){
                    maxi = card.getHealth();
                    maxCard = card;
                }
            }
            if(maxCard != null) {
                maxCard.setHealth(0);
            }
            removeCard(maxCard);
            return;
        }
        if(attacker.getHero().getName().equals("King Mudface")){
            for(Card card : board.get(attackedRow)){
                card.setHealth(card.getHealth() + 1);
            }
            return;
        }
        if(attacker.getHero().getName().equals("General Kocioraw")){
            for(Card card : board.get(attackedRow)){
                card.setAttackDamage(card.getAttackDamage() + 1);
            }
            return;
        }
    }
    public ArrayList<Card> getRow(int row) {
        if (row >= 0 && row < 4) {
            return board.get(row);
        }
        return new ArrayList<>();
    }

}

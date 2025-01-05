package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.example.server.JSONDataHandler.getJSON;
import static org.example.server.Message.sendEndGame;

@Getter
@Setter
public class Board {
    public static List<Board> boards = new ArrayList<>();


    private static int boardIdCounter = 0;

    private int boardId;
    private int playersAmount;

    private int cardAmount = 5;
    private List<Player> players = new ArrayList<>();
    private Deck deck;
    private int firstPlayerIndex;
    private Player currentPlayer;
    private int currentPlayerIndex;

    private long pot;
    private long currentBet;

    private int round;

    public Board(int amount) {
        this.playersAmount = amount;
        this.boardId = boardIdCounter++;
    }

    public JSONObject start() {
        this.deck = Deck.sortedDeck(); // create new sorted deck
        this.deck.shuffle(); // shuffle deck

        for (Player player : players) {
            player.setState(State.WAITING);
        }

        dealCardsToPlayers();

        firstPlayerIndex = randInt(players.size());
        currentPlayerIndex = firstPlayerIndex;
        currentPlayer = players.get(currentPlayerIndex);
        currentPlayer.setState(State.TURN);

        this.pot = 0;
        this.currentBet = 0;
        this.round = 0;

        return getJSON(boardId,"dealCards","success","player "+currentPlayer.getName()+" starts");
    }

    public JSONObject addPlayer(Player player) {
        players.add(player);
        player.setState(State.WAITING_FOR_GAME);

        if (players.size() == playersAmount) {
            return start();
        }

        else{
            return getJSON(boardId,"addPlayer","Joined the game", "player "+player.getName()+" joined");
        }
    }
    public void removePlayer(Player player) {
        players.remove(player);
        player.setState(State.LOBBY);
    }

    public void dealCardsToPlayers() {
        for (Player player : players) { // draw cards to players
            player.drawCards(this.deck.dealCards(cardAmount));
        }
    }

    public List<Card> dealCards(int numberOfCards) {
        return deck.dealCards(numberOfCards);
    }

    public JSONObject placeBet(Player player, long amount) throws IOException {
        if (amount >= currentBet && amount <= player.getMoney()) {
            pot += amount - player.getBet();
            player.setBet(amount);
            currentBet = amount;
            player.setState(State.BET);
            if(amount > currentBet) {
                restartPlayersState();
            }
            nextPlayer();
            return getJSON(boardId, "bet","bet placed","bet placed");

        } else if (amount < currentBet){
            return getJSON("Bet must be at least " + currentBet + " or higher!");
        } else {
            return getJSON("Not enough money");
        }
    }

    public JSONObject call(Player player) throws IOException {
        if (currentBet == player.getBet()) {
            player.setState(State.CALL);
            nextPlayer();
            return getJSON(boardId, "call","call","Player "+player.getName()+" called");
        } else {
            return getJSON("You cannot call; there is a bet of " + currentBet);
        }
    }

    public JSONObject fold(Player player) throws IOException {
        player.setState(State.FOLD);
        nextPlayer();
        return getJSON(boardId, "fold","folds","Player "+player.getName()+" folded");
    }

    public void nextPlayer() throws IOException {
        if (checkIfAllPlayersAreCalling() && round == 0){
            for (Player player : players) {
                if (player.getState() != State.FOLD) {
                    player.setState(State.BEFORE_EXCHANGE);
                }
            }
            exchangeRound();
        }
        else if (checkIfAllPlayersAreCalling() && round == 1){
            evaluate();
            List<Player> winners = comparePlayersHands();
            for (Player player : winners) {
                player.addWinnings(getPot()/winners.size());
            }
            sendEndGame(boardId, winners);
        }
        else if (getActivePlayerCount()>1){
            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                currentPlayer = players.get(currentPlayerIndex);
                currentPlayer.setState(State.TURN);
            } while (currentPlayer.getState() == State.FOLD || currentPlayer.getState() == State.CALL);
        }
    }

    private void exchangeRound(){
        round = 1;
        for (Player player : players) {
            if (player.getState() != State.FOLD) {
                player.setState(State.BEFORE_EXCHANGE);
            }
        }
    }

    public JSONObject exchange(Player player, int[] cards) {
        if(cards.length > 4){
            return getJSON("Too many cards for exchange");
        }
        for (int i = 0; i < cards.length; i++) {
            if (cards[i]>4 || cards[i] < 0) return getJSON("wrong card index");
        }
        player.exchangeCards(cards, dealCards(cards.length));
        if(isExchangeFinished()){
            lastRound();
        }
        return getJSON(boardId,"exchange","exchanged","exchange done");
    }


    public boolean isExchangeFinished() {
        for (Player player : players) {
            if (player.getState() != State.EXCHANGE && player.getState() != State.FOLD){
                return false;
            }
        }
        return true;
    }

    private void lastRound() {
        for (Player player : players) {
            if (player.getState() != State.FOLD) {
                player.setState(State.WAITING);
            }
        }
        currentPlayerIndex = firstPlayerIndex;
        currentPlayer = players.get(currentPlayerIndex);
        currentPlayer.setState(State.TURN);
    }

    public boolean isRoundFinished() {
        for (Player player : players) {
            if (!player.getState().equals(State.FOLD) && !player.getState().equals(State.CALL)) {
                return false;
            }
        }
        return true;
    }

    public void evaluate(){
        for (Player player : players) {
            System.out.println(player.getHand().evaluateHand());
        }
    }

    public List<Player> comparePlayersHands() {
        List<Player> playersHands = new ArrayList<>();
        int bestHand = -1;

        // Find the players with the best hand values
        for (Player player : players) {
            if (!player.getState().equals(State.FOLD)){
                int value = player.getHandValues()[0];
                if (value > bestHand) {
                    playersHands.clear();
                    playersHands.add(player);
                    bestHand = value;
                } else if (value == bestHand) {
                    playersHands.add(player);
                }
            }
        }

        // If there's a tie, resolve it using rank values
        if (playersHands.size() > 1) {
            List<Player> winners = new ArrayList<>();
            int bestRank = -1;

            for (Player player : playersHands) {
                int rankValue = player.getHandValues()[1];
                if (rankValue > bestRank) {
                    winners.clear();
                    winners.add(player);
                    bestRank = rankValue;
                } else if (rankValue == bestRank) {
                    winners.add(player);
                }
            }

            return winners;
        }

        // If no ties, return the single winner
        return playersHands;
    }

    public boolean isBettingEqual() {
        long currentBet = -1;

        for (Player player : players) {
            if (!player.getState().equals(State.FOLD)) {
                if (currentBet == -1) {
                    currentBet = player.getBet();
                } else if (player.getBet() != currentBet) {
                    return false;
                }
            }
        }

        return true;
    }

    public void restartPlayersState(){
        for (Player player : players) {
            if (!player.getState().equals(State.FOLD)){
                player.setState(State.BET);
            }
        }
    }

    public boolean checkIfAllPlayersAreCalling(){
        for (Player player : players) {
            if (player.getState() != State.CALL && player.getState() != State.FOLD){
                return false;
            }
        }
        System.out.println("All players are checking!");
        return true;
    }


    public int getActivePlayerCount() {
        return (int) players.stream().filter(player -> !player.getState().equals(State.FOLD)).count();
    }


//    @Override
//    public String toString() {
//        StringBuilder out = new StringBuilder();
//        for (Player player : players) {
//            out.append(player.toString()).append("\n");
//        }
//        return out.toString();
//    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.boardId);
        jsonObject.put("players", this.players.size() + '/' + this.playersAmount);
        return jsonObject;
    }

    public static int randInt(int max) {
        Random random = new Random();
        return random.nextInt(max);
    }


    public static Board getBoardById(int id){
        for (Board board : boards) {
            if (board.getBoardId() == id) {
                return board;
            }
        }
        return null;
    }

    public static Board findPlayersBoard(Player player) {
        for (Board b : boards){
            if (b.getPlayers().contains(player)){
                return b;
            }
        }
        return null;
    }
}

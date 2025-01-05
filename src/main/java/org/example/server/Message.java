package org.example.server;

import org.example.game.Board;
import org.example.game.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.example.Server.players;
import static org.example.game.Board.boards;
import static org.example.game.Board.getBoardById;

public class Message {
    public static void sendMessage(SocketChannel socketChannel, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
        socketChannel.write(buffer);
    }

    public static void broadcastMessage(String message) throws IOException {
        // Send the message to all connected clients
        for (SocketChannel client : players.keySet()) {
            sendMessage(client, message);
        }
    }
    public static void send(SocketChannel socketChannel, JSONObject jsonObject) throws IOException {


        // Create a response JSON object to send
        JSONObject response = new JSONObject();

        // Add "status" to the response
        if (jsonObject.has("status")) {
            response.put("status", jsonObject.getString("status"));
        }
        // Add "boards" to the response if it exists
        if (jsonObject.has("boards")) {
            response.put("boards", jsonObject.getJSONArray("boards"));
        }
        // Add "action" to the response if it exists
        if (jsonObject.has("board")) {
            sendGameInfo(jsonObject.getJSONObject("board"));
        }
        // Send the response as a JSON string
        sendMessage(socketChannel, response.toString());

        // Check if "players" and "message" are present, then send to specific players
        if (jsonObject.has("players") && jsonObject.has("message")) {

            JSONArray playerIds = jsonObject.getJSONArray("players");
            String message = jsonObject.getString("message");

            // Send the message to the specified players
            for (int i = 0; i < playerIds.length(); i++) {
                int playerId = playerIds.getInt(i);
                for (Map.Entry<SocketChannel, Player> entry : players.entrySet()) {
                    if (entry.getValue().getId() == playerId && entry.getKey()!=socketChannel) { // Match player ID
                        sendMessage(entry.getKey(), message.toString());
                    }
                }
            }
        }
    }

    private static void sendGameInfo(JSONObject jsonObject) throws IOException {

        // Get board ID and retrieve the corresponding Board object
        int boardId = jsonObject.getInt("id");
        Board board = Board.getBoardById(boardId);

        if (board == null) {
            System.out.println("No board found with ID: " + boardId);
            return;
        }

        // Get the action from the JSON
        String action = jsonObject.getString("action");


        for (Player player : board.getPlayers()) {
            SocketChannel sc = getKeyByValue(players, player);
            if (sc != null) {
                JSONObject response = new JSONObject();
                JSONArray playersArray = new JSONArray();

                // Build the JSON array of players
                for (Player p : board.getPlayers()) {
                    JSONObject playerJson = new JSONObject();
                    playerJson.put("id", p.getId());
                    playerJson.put("name", p.getName());
                    playerJson.put("state", p.getState());
                    playerJson.put("bet", p.getBet());
                    if (p == player) {
                        // Include full player details (with cards) for the current player
                        playerJson.put("cards", p.getHand()); // Assuming getCards() returns a list of cards
                    } else {
                        // Exclude cards for other players
                        playerJson.put("cards", "hidden"); // Indicate that cards are hidden
                    }
                    playersArray.put(playerJson);
                }

                // Add the players array and status to the response
                response.put("players", playersArray);
                if (action.equals("dealCards")) {
                    response.put("status", "Player " + jsonObject.getString("currentPlayer") + " starts");
                } else if (action.equals("bet")) {
                    response.put("status", "Actual bet: " + jsonObject.getInt("currentBet"));
                } else if (action.equals("addPlayer")) {
                    response.put("status", "Player added to board");
                }
                else if (action.equals("fold")) {
                    response.put("status", "Player folded");
                }
                // Send the response to the current player
                sendMessage(sc, response.toString());
            }
        }

    }

    public static void sendEndGame(int boardId, List<Player> winners) throws IOException {
        Board board = Board.getBoardById(boardId);

        for (Player player : board.getPlayers()) {
            SocketChannel sc = getKeyByValue(players, player);

            JSONObject response = new JSONObject();
            JSONArray playersArray = new JSONArray();

            // Build the JSON array of players
            for (Player p : board.getPlayers()) {
                JSONObject playerJson = new JSONObject();
                playerJson.put("id", p.getId());
                playerJson.put("name", p.getName());
                playerJson.put("state", p.getState());
                playerJson.put("bet", p.getBet());
                playerJson.put("cards", p.getHand());
                playerJson.put("hand", p.getHand().evaluateHand());
                playersArray.put(playerJson);
            }

            // Add the players array and status to the response
            response.put("players", playersArray);

            StringBuilder sb = new StringBuilder();
            sb.append("Winner: ");
            for (Player p : winners) {
                sb.append(p.getName()).append(" ");
            }
            response.put("message", sb.toString());
            sendMessage(sc, response.toString());
        }
    }

    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null; // Return null if no matching key is found
    }



}

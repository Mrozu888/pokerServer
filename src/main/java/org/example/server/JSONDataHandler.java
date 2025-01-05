package org.example.server;

import org.example.game.Board;
import org.json.JSONObject;

import static org.example.game.Board.boards;

public class JSONDataHandler {
    // keys:
    // "status" - success or error
    // "board" - game data
    //      "currentPlayer" - current players turn
    //      "currentBet" - current bet
    //      "id" - id of board to handle
    //      "message" - sending message to players e.g. player joined board/quitted
    //      "action" - current action
    //          ("dealcards" - send cards to players, "bet"



    public static void handleJSON(JSONObject jsonObject) {
        try {
            // Check if "board" key exists
            if (jsonObject.has("board")) {
                JSONObject board = jsonObject.getJSONObject("board");

                // Check for "currentPlayer"
                if (board.has("currentPlayer")) {
                    int currentPlayer = board.getInt("currentPlayer");
                    System.out.println("Current Player's Turn: " + currentPlayer);
                }

                // Check for "id"
                if (board.has("id")) {
                    int boardId = board.getInt("id");
                    System.out.println("Board ID: " + boardId);
                }

                // Check for "currentbet"
                if (board.has("currentBet")) {
                    int boardId = board.getInt("id");
                    System.out.println("Board ID: " + boardId);
                }

                // Check for "message"
                if (board.has("message")) {
                    String message = board.getString("message");
                    System.out.println("Message: " + message);
                }

                // Check for "action"
                if (board.has("action")) {
                    String action = board.getString("action");
                    System.out.println("Action: " + action);

                    // Handle specific actions
                    switch (action) {
                        case "dealcards":
                            System.out.println("Handling action: dealcards");
                            // Implement logic for dealing cards
                            break;
                        case "addPlayer":
                            System.out.println("Handling action: dealcards");
                            // Implement logic for dealing cards
                            break;
                        case "bet":
                            System.out.println("Handling action: dealcards");
                            // Implement logic for dealing cards
                            break;
                        // Add other cases for additional actions
                        default:
                            System.out.println("Unknown action: " + action);
                            break;
                    }
                }
            } else {
                System.out.println("Key 'board' does not exist in the JSON.");
            }

            // Check for "status"
            if (jsonObject.has("status")) {
                String status = jsonObject.getString("status");
                System.out.println("Status: " + status);
                if ("error".equalsIgnoreCase(status)) {
                    System.out.println("An error occurred.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing JSON: " + e.getMessage());
        }
    }

    public static JSONObject getJSON(int id, String action, String status, String message) {
        Board board = Board.getBoardById(id);

        JSONObject jsonObject = new JSONObject();
        JSONObject boardJson = new JSONObject();
        boardJson.put("id", board.getBoardId());
        boardJson.put("action", action);
        boardJson.put("currentPlayer", board.getCurrentPlayer() != null ? board.getCurrentPlayer().getName() : "");
        boardJson.put("currentBet", board.getCurrentBet());
        boardJson.put("message", message);
        jsonObject.put("board", boardJson);
        jsonObject.put("status", status);
        return jsonObject;
    }

    public static JSONObject getJSON(String status) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", status);
        return jsonObject;
    }

}

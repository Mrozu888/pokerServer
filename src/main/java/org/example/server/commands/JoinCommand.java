package org.example.server.commands;


import org.example.game.Board;
import org.example.game.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.example.game.Board.boards;
import static org.example.server.JSONDataHandler.getJSON;

public class JoinCommand implements Command {
    @Override
    public JSONObject execute(Player player, String value) {

        return switch (player.getState()) {
            case LOBBY -> joinGame(player, value);
            default -> new JSONObject("{\"status\":\"Wrong operation\"}");
        };
    }

    public JSONObject joinGame(Player player, String value) {
        JSONObject json = new JSONObject();
        // Try to parse the name as an integer
        try {
            int id = Integer.parseInt(value); // Try parsing args[1] to an integer

            // Assuming you have a game object and method to add a player
            Board board = getBoardById(id);
            if (board != null) {
                return board.addPlayer(player);
            } else {
                return getJSON("Game not found with ID " + id);
            }
        } catch (NumberFormatException e) {
            return getJSON("Invalid game ID. Please provide a valid integer");
        }
    }

    private Board getBoardById(int id) {
        // Iterate through the list of games and return the game with the matching ID
        for (Board board : boards) {
            if (board.getBoardId() == id) {
                return board;
            }
        }
        return null; // Return null if no game found with the given ID
    }
}

package org.example.server.commands;


import org.example.game.Board;
import org.example.game.Player;
import org.json.JSONObject;

import static org.example.game.Board.boards;
import static org.example.server.JSONDataHandler.getJSON;

public class CreateCommand implements Command {
    @Override
    public JSONObject execute(Player player, String value) {
        JSONObject json = new JSONObject();
        return switch (player.getState()) {
            case LOBBY -> createBoard(player, value);
            default -> getJSON("Wrong operation");
        };
    }

    public static JSONObject createBoard(Player player, String value){
        // Try to parse the name as an integer
        try {
            int amount = Integer.parseInt(value); // Try parsing value to an integer
            if (amount < 2 || amount > 6) {
                return getJSON("Wrong player amount");
            }
            Board board = new Board(amount);
            boards.add(board);
            return board.addPlayer(player);

        } catch (NumberFormatException e) {
            return getJSON("Invalid player amount. Please provide a valid integer.");
        }
    }
}

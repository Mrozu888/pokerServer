package org.example.server.commands;


import org.example.game.Board;
import org.example.game.Player;
import org.json.JSONObject;

import java.io.IOException;

import static org.example.game.Board.boards;
import static org.example.game.Board.findPlayersBoard;

public class BetCommand implements Command{
    @Override
    public JSONObject execute(Player player, String value) {
        return switch (player.getState()) {
            case TURN -> bet(player, value);
            default -> new JSONObject("{\"status\":\"Wrong operation\"}");
        };
    }

    public static JSONObject bet(Player player, String value) {
        // Try to parse the name as an integer
        try {
            long amount = Integer.parseInt(value); // Try parsing args[1] to an integer
            Board board = findPlayersBoard(player);
            return board.placeBet(player, amount);
        } catch (NumberFormatException e) {
            return new JSONObject("{\"status\":\"Invalid amount\"}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

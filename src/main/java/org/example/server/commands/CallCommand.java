package org.example.server.commands;


import org.example.game.Board;
import org.example.game.Player;
import org.json.JSONObject;

import java.io.IOException;

import static org.example.game.Board.findPlayersBoard;

public class CallCommand implements Command {

    @Override
    public JSONObject execute(Player player, String value) throws IOException {
        return switch (player.getState()) {
            case TURN -> call(player);
            default -> new JSONObject("{\"status\":\"Wrong operation\"}");
        };
    }
    private JSONObject call(Player player) throws IOException {
        Board board = findPlayersBoard(player);
        return board.call(player);
    }
}

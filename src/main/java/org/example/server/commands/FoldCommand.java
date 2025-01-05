package org.example.server.commands;

import org.example.game.Board;
import org.example.game.Player;
import org.json.JSONObject;

import java.io.IOException;

import static org.example.game.Board.findPlayersBoard;

public class FoldCommand implements Command {

    @Override
    public JSONObject execute(Player player, String args) throws IOException {
        return switch (player.getState()) {
            case BET,CALL -> fold(player);
            default -> new JSONObject("{\"status\":\"Wrong operation\"}");
        };
    }
    private JSONObject fold(Player player) throws IOException {
        Board board = findPlayersBoard(player);
        return board.fold(player);
    }

}

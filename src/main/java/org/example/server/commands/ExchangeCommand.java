package org.example.server.commands;


import org.example.game.Board;
import org.example.game.Player;
import org.json.JSONObject;

import static org.example.game.Board.findPlayersBoard;
import static org.example.server.JSONDataHandler.getJSON;

public class ExchangeCommand implements Command {
    @Override
    public JSONObject execute(Player player, String args) {
        return switch (player.getState()) {
            case BEFORE_EXCHANGE -> exchange(player, args);
            default -> new JSONObject("{\"status\":\"Wrong operation\"}");
        };
    }

    public static JSONObject exchange(Player player, String args) {
        // Try to parse the name as an integer
        try {
            int[] cards = parseToArray(args); // Try parsing args[1] to an integer
            Board board = findPlayersBoard(player);
            return board.exchange(player, cards);
        } catch (NumberFormatException e) {
            return getJSON("Invalid amount. Please provide a valid integer.");
        }
    }

    public static int[] parseToArray(String text) {
        // Convert the string to a char array
        char[] chars = text.toCharArray();

        // Create an integer array of the same size
        int[] numbers = new int[chars.length];

        // Convert each character to an integer
        for (int i = 0; i < chars.length; i++) {
            numbers[i] = Character.getNumericValue(chars[i]);
        }

        return numbers;
    }

}

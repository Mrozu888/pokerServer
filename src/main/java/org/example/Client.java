package org.example;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class Client {

    private static String username = "";

    // ANSI escape codes for colors
    public static final String RESET = "\033[0m";  // Reset to default color
    public static final String BLUE = "\033[34m"; // Blue color

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new java.net.InetSocketAddress("localhost", 12345));
        socketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);

        // Create scanner to read input from user
        Scanner scanner = new Scanner(System.in);

        // Start a new thread to handle user input (sending messages)
        new Thread(() -> {
            try {
                while (true) {
                    if (username.equals("")) {
                        // Ask for the username only once
                        System.out.print("Enter username: ");
                        username = scanner.nextLine();
                        sendUsername(socketChannel, username);
                    } else {
                        // After the username is set, allow message sending
                        String message = scanner.nextLine();
                        sendMessage(socketChannel, message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Main loop to handle incoming messages from the server
        while (true) {
            try {
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);

                    if (key.isReadable()) {
                        handleRead(socketChannel);
                    }
                }
            } catch (IOException e) {
                // Catch IOException, including SocketException when the connection is reset
                System.out.println("Connection lost: " + e.getMessage());
                break; // Exit the loop and close the client gracefully
            }
        }
    }

    // Send the username to the server
    private static void sendUsername(SocketChannel socketChannel, String username) throws IOException {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("username", username);

        ByteBuffer buffer = ByteBuffer.wrap(jsonMessage.toString().getBytes(StandardCharsets.UTF_8));
        socketChannel.write(buffer);
    }

    // Send a regular command message to the server
    private static void sendMessage(SocketChannel socketChannel, String input) throws IOException {
        // Split the input into command and value parts
        String[] parts = input.split(" ", 2);
        String command = parts[0];
        String value = parts.length > 1 ? parts[1] : ""; // Default to empty string if no value

        // Create a JSON message with the command and value
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("command", command);    // The command sent
        jsonMessage.put("value", value);        // The value for the command

        // Send the message to the server
        ByteBuffer buffer = ByteBuffer.wrap(jsonMessage.toString().getBytes(StandardCharsets.UTF_8));
        socketChannel.write(buffer);

    }

    // Handle messages received from the server
    private static void handleRead(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int bytesRead = socketChannel.read(buffer);

            if (bytesRead == -1) {
                // If no data is read, the connection is closed
                System.out.println("Server closed the connection.");
                socketChannel.close();
                return;
            }

            String message = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
//            System.out.println("Raw message: " + message);

            JSONObject jsonObject = new JSONObject(message);

            // Parse `status` if available
            if (jsonObject.has("status")) {
                String mess = jsonObject.getString("status");
                System.out.println("Status: " + mess);
            }

            // Parse `message` if available
            if (jsonObject.has("message")) {
                String mess = jsonObject.getString("message");
                System.out.println("Message: " + mess);
            }

            // Parse `players` if available
            if (jsonObject.has("players")) {
                JSONArray players = jsonObject.getJSONArray("players");

                System.out.println("Players:");
                for (int i = 0; i < players.length(); i++) {
                    JSONObject player = players.getJSONObject(i);

                    // Extract player details
                    int id = player.optInt("id", -1);
                    String name = player.optString("name", "Unknown");
                    String state = player.optString("state", "No state provided");
                    String cards = player.optString("cards", "No cards provided");
                    int bet = player.optInt("bet", 0);

                    if(player.has("hand")) {
                        String hand = player.getString("hand");
                        System.out.printf("   Player %d: ID=%d, Name=%s, State=%s, Cards=%s, Bet=%d, Hand=%s%n",
                                i + 1, id, name, state, cards, bet, hand);
                    }
                    else{
                        System.out.printf("   Player %d: ID=%d, Name=%s, State=%s, Cards=%s, Bet=%d%n",
                                i + 1, id, name, state, cards, bet);
                    }


                }
            }

        } catch (SocketException e) {
            // Handle the case when the connection is reset
            System.out.println("Connection reset by server: " + e.getMessage());
            socketChannel.close(); // Close the socket gracefully
        }
    }

}

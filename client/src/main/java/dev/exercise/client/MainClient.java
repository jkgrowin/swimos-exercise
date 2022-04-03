package dev.exercise.client;

import dev.exercise.client.client.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.String.format;

public class MainClient {

    static final int PORT = 8888;
    static final String HOST = "localhost";

    public static void main(String[] args) {
        if (args .length< 2) {
            System.out.println("Missing parameters: 'host' 'port'");
        }

        try {
            final var client = new Client();
            client.startConnection(args[0], Integer.parseInt(args[1]));

            var stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                System.out.println(client.sendMessage(userInput));
            }
            System.out.println("Closing connection...");
            client.stopConnection();
        } catch (IOException e) {
            System.out.println(format("Failed to start connection: %s:%s", HOST, PORT));
        }
    }
}

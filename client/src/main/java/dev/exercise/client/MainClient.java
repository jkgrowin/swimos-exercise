package dev.exercise.client;

import dev.exercise.client.thread.ClientReadThread;
import dev.exercise.client.thread.ClientWriteThread;

import java.io.IOException;
import java.net.Socket;

import static java.lang.String.format;

public class MainClient {

    public static void main(String[] args) {
        if (args .length< 2) {
            System.out.println("Missing parameters: 'host' 'port'");
            return;
        }
        var address = args[0];
        var port =  Integer.parseInt(args[1]);

        try {
            var socket = new Socket(address, port);
            new ClientWriteThread(socket).start();
            new ClientReadThread(socket).start();
        } catch (IOException e) {
            System.out.println(format("Failed to start connection: %s:%s", address, port));
        }
    }
}

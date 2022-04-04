package dev.exercise.client;

import dev.exercise.client.thread.ClientReadThread;
import dev.exercise.client.thread.ClientWriteThread;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class MainClient {

    public static void main(String[] args) {
        if (args.length < 2) {
            log.error("Missing parameters: 'host' 'port'");
            return;
        }
        var address = args[0];
        var port = Integer.parseInt(args[1]);

        try {
            var socket = new Socket(address, port);
            new ClientWriteThread(socket).start();
            new ClientReadThread(socket).start();
            log.info("Successfully connected to {}:{}", address, port);
        } catch (IOException e) {
            log.error("Failed to start connection: {}:{}", address, port, e);
        }
    }
}

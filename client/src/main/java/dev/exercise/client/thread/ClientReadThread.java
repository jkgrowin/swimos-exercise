package dev.exercise.client.thread;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

@Slf4j
public class ClientReadThread extends Thread {
    private final Socket clientSocket;

    private final BufferedReader in;

    public ClientReadThread(@NonNull Socket socket) throws IOException {
        clientSocket = socket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        // continuously reads and prints incoming values
        while (true) {
            try {
                var line = readMessage();
                if (line == null) {
                    stopConnection();
                    log.info("Connection is now closed.");
                    System.exit(0);
                } else {
                    System.out.println(line);
                }
            } catch (IOException e) {
                log.error("Failed to read the message, {}", e.getMessage());
                return;
            }
        }
    }

    public String readMessage() throws IOException {
        return in.readLine();
    }

    public void stopConnection() throws IOException {
        in.close();
        clientSocket.close();
    }
}

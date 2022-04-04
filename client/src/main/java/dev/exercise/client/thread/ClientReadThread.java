package dev.exercise.client.thread;

import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientReadThread extends Thread{
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
                    return;
                } else {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Failed to read the message.");
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

package dev.exercise.client.client;

import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientWriteThread extends Thread{
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader stdIn;

    public ClientWriteThread(@NonNull Socket socket) throws IOException {
        this.clientSocket = socket;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        stdIn = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        while (true) {
            try {
                var line = stdIn.readLine();
                if (line == null) {
                    stopConnection();
                    return;
                } else {
                    out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Failed to write the message.");
                return;
            }
        }
    }

    public void stopConnection() throws IOException {
        stdIn.close();
        out.close();
        clientSocket.close();
    }
}

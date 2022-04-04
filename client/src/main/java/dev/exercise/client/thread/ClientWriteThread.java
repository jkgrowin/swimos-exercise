package dev.exercise.client.thread;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class ClientWriteThread extends Thread {
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
        // continuously reads user's input and sends it to server
        while (true) {
            try {
                var line = stdIn.readLine();
                if (line == null) {
                    stopConnection();
                    return;
                } else {
                    sendMessage(line);
                }
            } catch (IOException e) {
                log.error("Failed to write the message.", e);
                return;
            }
        }
    }

    public void sendMessage(@NonNull String line) {
        out.println(line);
    }

    public void stopConnection() throws IOException {
        stdIn.close();
        out.close();
        clientSocket.close();
    }
}

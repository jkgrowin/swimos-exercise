package dev.exercise.server;

import dev.exercise.server.thread.SingleThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedServer {

    static final int PORT = 8888;
    private static long state = 0L;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        Socket socket = null;

        serverSocket = new ServerSocket(PORT);
        System.out.println("Starting new server on port: " + PORT);

        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);

            }
            new SingleThread(socket).start();
        }
    }

    public static long getState() {
        return state;
    }

    public static void setState(long state) {
        ThreadedServer.state = state;
    }
}

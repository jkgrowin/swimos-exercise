package dev.exercise.server;

import dev.exercise.server.thread.SingleThread;
import lombok.NonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadedServer {

    private static long state = 0L;
    private static final ConcurrentHashMap<Integer, Socket> subscribers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(("Missing parameters: 'port'"));
            return;
        }
        int port = Integer.parseInt(args[0]);
        Socket socket = null;
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Failed to start a server: " + port);
            return;
        }

        System.out.println("Starting new server on port: " + port);

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

    public static void addSubscriber(@NonNull Integer key, @NonNull Socket socket) {
        subscribers.put(key, socket);
    }

    public static void removeSubscriber(@NonNull Integer key) {
        subscribers.remove(key);
    }

    public static void clearSubscribers() {
        subscribers.clear();
    }

    public static void sendToSubscribers(@NonNull String message) {
        subscribers.values().forEach(s -> {
            try {
                var out = new DataOutputStream(s.getOutputStream());
                out.writeBytes(message);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

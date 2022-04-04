package dev.exercise.server;

import dev.exercise.server.thread.ClientThread;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ThreadedServer {

    private static long state = 0L;
    private static final ConcurrentHashMap<Integer, ClientThread> subscribers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length < 1) {
            log.error(("Missing parameters: 'port'"));
            return;
        }
        var port = Integer.parseInt(args[0]);
        Socket socket = null;
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Failed to start a server: {}", port);
            return;
        }

        log.info("Starting new server on port: {}", port);

        // continuously awaits for new client connections
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                log.error("I/O error", e);
            }
            new ClientThread(socket).start();
        }
    }

    public static long getState() {
        return state;
    }

    public static void setState(long state) {
        ThreadedServer.state = state;
    }

    public static void addSubscriber(int key, @NonNull ClientThread clientThread) {
        subscribers.put(key, clientThread);
    }

    public static void removeSubscriber(int key) {
        subscribers.remove(key);
    }

    public static void clearSubscribers() {
        subscribers.clear();
    }

    public static void sendToSubscribers(@NonNull String message) {
        subscribers.values().forEach(s -> s.sendMessage(message));
    }
}

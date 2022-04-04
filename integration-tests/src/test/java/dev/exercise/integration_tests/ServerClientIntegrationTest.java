package dev.exercise.integration_tests;

import dev.exercise.client.thread.ClientReadThread;
import dev.exercise.client.thread.ClientWriteThread;
import dev.exercise.server.ThreadedServer;
import dev.exercise.server.thread.ClientThread;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ServerClientIntegrationTest {

    @Before
    public void setUp() {
        ThreadedServer.setState(0);
        ThreadedServer.clearSubscribers();
    }

    @Test
    public void test_singleClient() throws IOException {
        /* Test scenario 1:
        Single Client:

        > { "command": "get" }
        { "value": 0 }
        > { "command": "set", "value": 5 }
        > { "command": "get" }
        { "value": 5 }
        */
        var port = 7777;
        var serverSocket = new ServerSocket(port);
        var running = new AtomicBoolean(false);
        // Creates new server thread that accepts one socket connection
        new Thread(() -> {
            running.set(true);
            while (running.get()) {
                try {
                    var socket = serverSocket.accept();
                    if (socket != null) {
                        new ClientThread(socket).start();
                        running.set(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        var clientSocket = new Socket("localhost", port);
        var write = new ClientWriteThread(clientSocket);
        var read = new ClientReadThread(clientSocket);

        write.sendMessage("{\"command\":\"get\"}");
        assertEquals("{ \"value\": 0 }", read.readMessage());

        write.sendMessage("{\"command\":\"set\",\"value\":5}");
        write.sendMessage("{\"command\":\"get\"}");

        assertEquals("{ \"value\": 5 }", read.readMessage());

        write.stopConnection();
        read.stopConnection();
        serverSocket.close();
    }

    @Test
    public void test_ThreeClients() throws IOException {
        /* Test scenario 2:
        Client 1
        (Here the 'subscribe' command is executed before clients 2 and 3 are started.)
        > { "command": "subscribe" }
        {"value": 1 }
        { "value": 2 }
        { "value": 3 }
        { "value": 4 }
        { "value": 5 }
        { "value": 6 }
        Client 2

        > { "command": "set", "value": 1 }
        > { "command": "set", "value": 3 }
        > { "command": "set", "value": 5 }
        Client 3

        > { "command": "set", "value": 2 }
        > { "command": "set", "value": 4 }
        > { "command": "set", "value": 6 }
        */
        var port = 7778;
        var serverSocket = new ServerSocket(port);
        var running = new AtomicBoolean(false);
        // Creates new server thread that accepts three socket connection
        new Thread(() -> {
            running.set(true);
            int i =0;
            while (running.get()) {
                try {
                    var socket = serverSocket.accept();
                    if (socket != null) {
                        new ClientThread(socket).start();
                        i++;
                        if (i == 3) {
                            running.set(false);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        var clientSocket1 = new Socket("localhost", port);
        var write1 = new ClientWriteThread(clientSocket1);
        var read1 = new ClientReadThread(clientSocket1);
        var clientSocket2 = new Socket("localhost", port);
        var write2 = new ClientWriteThread(clientSocket2);
        var clientSocket3 = new Socket("localhost", port);
        var write3 = new ClientWriteThread(clientSocket3);


        write1.sendMessage("{\"command\":\"subscribe\"}");
        write2.sendMessage("{\"command\":\"set\",\"value\":1}");
        assertEquals("{ \"value\": 1 }", read1.readMessage());
        write3.sendMessage("{\"command\":\"set\",\"value\":2}");
        assertEquals("{ \"value\": 2 }", read1.readMessage());
        write2.sendMessage("{\"command\":\"set\",\"value\":3}");
        assertEquals("{ \"value\": 3 }", read1.readMessage());
        write3.sendMessage("{\"command\":\"set\",\"value\":4}");
        assertEquals("{ \"value\": 4 }", read1.readMessage());
        write2.sendMessage("{\"command\":\"set\",\"value\":5}");
        assertEquals("{ \"value\": 5 }", read1.readMessage());
        write3.sendMessage("{\"command\":\"set\",\"value\":6}");
        assertEquals("{ \"value\": 6 }", read1.readMessage());

        write1.stopConnection();
        read1.stopConnection();
        write2.stopConnection();
        write3.stopConnection();
    }

    @Test
    public void test_FiftyClients() throws IOException, InterruptedException {
        /* Test scenario 3:
         * In order to put some pressure on the server, we create 50 clients.
         * 20 of them subscribes state.
         * All 50 of them updates the state.
         * Messages count on all subscribers of them should be equal to 50.
         * We check first subscriber.
         */
        var port = 7779;
        var serverSocket = new ServerSocket(port);
        var running = new AtomicBoolean(false);
        // Creates new server thread that 50 one socket connection
        new Thread(() -> {
            running.set(true);
            int i = 0;
            while (running.get()) {
                try {
                    var socket = serverSocket.accept();
                    if (socket != null) {
                        new ClientThread(socket).start();
                        i++;
                        if (i == 50) {
                            running.set(false);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        var readers = new HashMap<Integer, ClientReadThread>();
        var writers = new HashMap<Integer, ClientWriteThread>();
        for (int i = 0; i<50; i++) {
            var clientSocket = new Socket("localhost", port);
            readers.put(i, new ClientReadThread(clientSocket));
            writers.put(i, new ClientWriteThread(clientSocket));
        }

        // adds subscribers
        for (int i = 0; i<20; i++) {
            writers.get(i).sendMessage("{\"command\":\"subscribe\"}");
        }

        // sets STATE
        for (int i = 0; i<50; i++) {
            writers.get(i).sendMessage(format("{\"command\":\"set\",\"value\":%s}", i));
        }

        // waits to make sure exit message is the last one
        Thread.sleep(2000);

        // usses static value to quit reading messages
        writers.get(49).sendMessage("{\"command\":\"set\",\"value\":11111111111111}");
        // gets one of the subscribers
        var reader = readers.get(0);

        var messageCount = 0;
        var response ="";
        while (!"{ \"value\": 11111111111111 }".equals(response = reader.readMessage())) {
            messageCount++;
        }

        // checks if all 50 state updates were sent back to subscriber
        assertEquals(50, messageCount);
    }

    @Test
    public void test_noSetValue() throws IOException {
        /* Test scenario 4:
        Single Client:

        > { "command": "pet" }
        { "unknown action": 0 }
        */
        var port = 7780;
        var serverSocket = new ServerSocket(port);
        var running = new AtomicBoolean(false);
        // Creates new server thread that accepts one socket connection
        new Thread(() -> {
            running.set(true);
            while (running.get()) {
                try {
                    var socket = serverSocket.accept();
                    if (socket != null) {
                        new ClientThread(socket).start();
                        running.set(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        var clientSocket = new Socket("localhost", port);
        var write = new ClientWriteThread(clientSocket);
        var read = new ClientReadThread(clientSocket);

        write.sendMessage("{\"command\":\"set\"}");
        assertEquals("Missing 'set' value.", read.readMessage());

        write.stopConnection();
        read.stopConnection();
        serverSocket.close();
    }

    @Test
    public void test_unknownCommand() throws IOException {
        /* Test scenario 5:
        Single Client:

        > { "command": "pet" }
        { "unknown action": 0 }
        */
        var port = 7781;
        var serverSocket = new ServerSocket(port);
        var running = new AtomicBoolean(false);
        // Creates new server thread that accepts one socket connection
        new Thread(() -> {
            running.set(true);
            while (running.get()) {
                try {
                    var socket = serverSocket.accept();
                    if (socket != null) {
                        new ClientThread(socket).start();
                        running.set(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        var clientSocket = new Socket("localhost", port);
        var write = new ClientWriteThread(clientSocket);
        var read = new ClientReadThread(clientSocket);

        write.sendMessage("{\"command\":\"pet\"}");
        assertEquals("Cannot find command pet or maximum value 9223372036854775807 exceeded.", read.readMessage());

        write.stopConnection();
        read.stopConnection();
        serverSocket.close();
    }

    @Test
    public void test_valueTooBig() throws IOException {
        /* Test scenario 6:
        Single Client:

        > { "command": "pet" }
        { "unknown action": 0 }
        */
        var port = 7781;
        var serverSocket = new ServerSocket(port);
        var running = new AtomicBoolean(false);
        // Creates new server thread that accepts one socket connection
        new Thread(() -> {
            running.set(true);
            while (running.get()) {
                try {
                    var socket = serverSocket.accept();
                    if (socket != null) {
                        new ClientThread(socket).start();
                        running.set(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        var clientSocket = new Socket("localhost", port);
        var write = new ClientWriteThread(clientSocket);
        var read = new ClientReadThread(clientSocket);

        write.sendMessage("{\"command\":\"set\",\"value\":9223372036854775808}");
        assertEquals("Incorrect syntax. Required: { \"command\": \"\" } or the maximum value 9223372036854775807 exceeded.", read.readMessage());

        write.stopConnection();
        read.stopConnection();
        serverSocket.close();
    }
}

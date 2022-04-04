package dev.exercise.integration_tests;

import dev.exercise.client.thread.ClientReadThread;
import dev.exercise.client.thread.ClientWriteThread;
import dev.exercise.server.ThreadedServer;
import dev.exercise.server.thread.SingleThread;
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
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class ServerClientIntegrationTest {

    @Before
    public void setUp() {
        ThreadedServer.setState(0);
        ThreadedServer.clearSubscribers();
    }

    @Test
    public void test_singleClient() throws IOException {
        var serverSocket = new ServerSocket(7777);
        var running = new AtomicBoolean(false);
        // Creates new server thread that accepts one socket connection
        new Thread(() -> {
            running.set(true);
            while (running.get()) {
                try {
                    var socket = serverSocket.accept();
                    if (socket != null) {
                        new SingleThread(socket).start();
                        running.set(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        var clientSocket = new Socket("localhost", 7777);
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
        var serverSocket = new ServerSocket(7778);
        var running = new AtomicBoolean(false);
        // Creates new server thread that accepts three socket connection
        new Thread(() -> {
            running.set(true);
            int i =0;
            while (running.get()) {
                try {
                    var socket = serverSocket.accept();
                    if (socket != null) {
                        new SingleThread(socket).start();
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
        var clientSocket1 = new Socket("localhost", 7778);
        var write1 = new ClientWriteThread(clientSocket1);
        var read1 = new ClientReadThread(clientSocket1);
        var clientSocket2 = new Socket("localhost", 7778);
        var write2 = new ClientWriteThread(clientSocket2);
        var clientSocket3 = new Socket("localhost", 7778);
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
        var serverSocket = new ServerSocket(7779);
        var running = new AtomicBoolean(false);
        // Creates new server thread that 50 one socket connection
        new Thread(() -> {
            running.set(true);
            int i =0;
            while (running.get()) {
                try {
                    var socket = serverSocket.accept();
                    if (socket != null) {
                        new SingleThread(socket).start();
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
            var clientSocket = new Socket("localhost", 7779);
            readers.put(i, new ClientReadThread(clientSocket));
            writers.put(i, new ClientWriteThread(clientSocket));
        }

        for (int i = 0; i<20; i++) {
            writers.get(i).sendMessage("{\"command\":\"subscribe\"}");
        }

        for (int i = 39; i<50; i++) {
            writers.get(i).sendMessage(format("{\"command\":\"set\",\"value\":%s}", i));
        }

        var reader = readers.get(0);

        var messageCount = 0;
        while (messageCount <= 10) {
            assertNotNull(reader.readMessage());
            messageCount++;
        }

    }
}

package dev.exercise.client.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ClientTest {
    private static final Pattern PATTERN = Pattern.compile("(?i)^\\{\\s*\"command\"\\s*:\\s*[\"]?(.*?)[\"]?,\\s*\"value\"\\s*:\\s*[\"]?(.*?)[\"]?\\s*}$");

    private Client client;

    @Before
    public void setUp() throws IOException {
        client = new Client();
        client.startConnection("127.0.0.1", 8888);
    }

    @Test
    public void testSendMessage_UnknownCommand() throws IOException {
        String response = client.sendMessage("{ \"command\": \"bet\", \"value\": 2 }");
        assertEquals("Cannot find command: bet", response);
    }

    @Test
    public void testSendMessage_invalidCommandFormat() throws IOException {
        String response = client.sendMessage("{ \"comsmand\": \"bet\", \"value\": 2 }");
        assertEquals("Missing 'command' keyword. Required: { \"command\": \"\"}", response);
    }

    @Test
    public void testSendMessage_invalidValue() throws IOException {
        String response = client.sendMessage("{ \"command\": \"set\", \"value\": 66666666666666666666666 }");
        assertEquals("Incorrect value format. Maximum value is: 9223372036854775807", response);
    }
}

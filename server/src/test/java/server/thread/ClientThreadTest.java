package server.thread;

import dev.exercise.server.enums.CommandType;
import dev.exercise.server.thread.ClientThread;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.Socket;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ClientThreadTest {
    @Mock
    private Socket socket;

    private ClientThread clientThread;

    @Before
    public void setUp() {
        clientThread = new ClientThread(socket);
    }

    @Test
    public void testParseLine_success() throws ParseException {
        var command = CommandType.SET;
        long value = 2;

        var line = "{ \"command\": \"set\", \"value\": 2 }";
        var result = clientThread.parseLine(line);
        assertEquals(command, result.getCommand());
        assertEquals(value, (long) result.getValue());
    }

    @Test(expected = ParseException.class)
    public void testParseLine_incorrectCommand() throws ParseException {
        var line = "{ \"band\": \"set\", \"value\": 2 }";
        clientThread.parseLine(line);
    }

    @Test(expected = ParseException.class)
    public void testParseLine_incorrectValue() throws ParseException {
        var line = "{ \"command\": \"set\", \"value\": 2d }";
        clientThread.parseLine(line);
    }

    @Test(expected = ParseException.class)
    public void testParseLine_valueTooBig() throws ParseException {
        var value = "9223372036854775808";
        var line = "{ \"command\": \"set\", \"value\": " + value + " }";
        clientThread.parseLine(line);
    }

    @Test
    public void testParseLine_get() throws ParseException {
        var command = CommandType.GET;
        var line = "{ \"command\": \"get\" }";
        var result = clientThread.parseLine(line);
        assertEquals(command, result.getCommand());
        assertNull(result.getValue());
    }
}

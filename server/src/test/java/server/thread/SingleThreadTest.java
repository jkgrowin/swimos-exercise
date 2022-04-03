package server.thread;

import dev.exercise.server.thread.SingleThread;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.Socket;
import java.text.ParseException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class SingleThreadTest {
    private static final Pattern PATTERN = Pattern.compile("(?i)^\\{\\s*\"command\"\\s*:\\s*[\"]?(.*?)[\"]?,\\s*\"value\"\\s*:\\s*[\"]?(.*?)[\"]?\\s*}$");

    @Mock
    private Socket socket;

    private SingleThread singleThread;

    @Before
    public void setUp() {
        singleThread = new SingleThread(socket);
    }

    @Test
    public void testParseLine_success() throws ParseException {
        var command = "set";
        long value = 2;

        var line = "{ \"command\": \"set\", \"value\": 2 }";
        var result = singleThread.parseLine(line);
        assertEquals(command, result.getCommand());
        assertEquals(value, (long) result.getValue());
    }

    @Test (expected = ParseException.class)
    public void testParseLine_incorrectCommand() throws ParseException {
        var line = "{ \"band\": \"set\", \"value\": 2 }";
        singleThread.parseLine(line);
    }

    @Test (expected = ParseException.class)
    public void testParseLine_incorrectValue() throws ParseException {
        var line = "{ \"command\": \"set\", \"value\": 2d }";
        singleThread.parseLine(line);
    }

    @Test(expected = ParseException.class)
    public void testParseLine_valueTooBig() throws ParseException {
        var value = "9223372036854775808";
        var line = "{ \"command\": \"set\", \"value\": " + value + " }";
        singleThread.parseLine(line);
    }

    @Test
    public void testParseLine_get() throws ParseException {
        var command = "get";
        var line = "{ \"command\": \"get\" }";
        var result = singleThread.parseLine(line);
        assertEquals(command, result.getCommand());
        assertNull(result.getValue());
    }
}

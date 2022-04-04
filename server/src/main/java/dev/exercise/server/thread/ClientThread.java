package dev.exercise.server.thread;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.exercise.server.ThreadedServer;
import dev.exercise.server.enums.CommandType;
import dev.exercise.server.model.CommandValue;
import dev.exercise.server.util.JsonPrettyPrinter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;

import static java.lang.String.format;

@Slf4j
public class ClientThread extends Thread {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .defaultPrettyPrinter(new JsonPrettyPrinter())
            .build();
    protected Socket socket;
    protected PrintWriter out;

    public ClientThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    @Override
    public void run() {
        InputStream inp;
        BufferedReader brinp;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            return;
        }
        String line;
        // continuously reads client's message and performs specified action
        while (true) {
            try {
                line = brinp.readLine();
                if (line == null) {
                    socket.close();
                    return;
                } else {
                    try {
                        var commandValue = parseLine(line);
                        performAction(commandValue);
                    } catch (ParseException e) {
                        out.println(e.getMessage());
                    }
                }
                out.flush();
            } catch (IOException e) {
                log.info("Client {} disconnected", socket.getLocalAddress());
                ThreadedServer.removeSubscriber(socket.hashCode());
                return;
            }
        }
    }

    /**
     * Performs specified action or sends back error message.
     * @param commandValue The {@link CommandValue}.
     */
    private void performAction(@NonNull CommandValue commandValue) {
        try {
            switch (commandValue.getCommand()) {
                case GET:
                    sendMessage(OBJECT_MAPPER.writeValueAsString(CommandValue.builder().value(ThreadedServer.getState()).build()));
                    break;
                case SET:
                    ThreadedServer.setState(commandValue.getValue());
                    ThreadedServer.sendToSubscribers(OBJECT_MAPPER.writeValueAsString(CommandValue.builder().value(ThreadedServer.getState()).build()));
                    break;
                case SUBSCRIBE:
                    ThreadedServer.addSubscriber(socket.hashCode(), this);
                    break;
            }
        } catch (JsonProcessingException | IllegalArgumentException e) {
            out.println(format("Cannot find command: %s", commandValue.getCommand()));
        }
    }

    /**
     * Sends message to Client.
     * @param message The message content.
     */
    public void sendMessage(@NonNull String message) {
        out.println(message);
        out.flush();
    }

    /**
     * Parse command line to {@link CommandValue}.
     * @param line The command line.
     * @return The {@link CommandValue}.
     */
    public CommandValue parseLine(@NonNull String line) throws ParseException {
        try {
            var commandValue = OBJECT_MAPPER.readValue(line, CommandValue.class);
            if (commandValue.getCommand() == null) {
                throw new ParseException("Missing command.", 0);
            }
            if (CommandType.SET == commandValue.getCommand() && commandValue.getValue() == null) {
                throw new ParseException("Missing 'set' value.", 0);
            }
            return commandValue;
        } catch (InvalidFormatException e) {
            throw new ParseException("Cannot find command " + e.getValue() + " or maximum value " + Long.MAX_VALUE + " exceeded.", 0);
        } catch (JsonProcessingException e) {
            throw new ParseException("Incorrect syntax. Required: { \"command\": \"\" } or the maximum value " + Long.MAX_VALUE + " exceeded.", 0);
        }
    }
}
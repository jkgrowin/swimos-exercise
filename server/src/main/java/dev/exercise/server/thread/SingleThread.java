package dev.exercise.server.thread;

import dev.exercise.server.ThreadedServer;
import dev.exercise.server.enums.CommandType;
import dev.exercise.server.model.CommandValue;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.ParseException;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class SingleThread extends Thread {

    private static final Pattern PATTERN = Pattern.compile("(?i)^\\{\\s*\"command\"\\s*:\\s*[\"]?(.*?)[\"]?\\s*(?:,\\s*\"value\"\\s*:\\s*[\"]?(.*?)[\"]?\\s*)?}$");
    protected Socket socket;

    public SingleThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    @Override
    public void run() {
        InputStream inp;
        BufferedReader brinp;
        DataOutputStream out;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                line = brinp.readLine();
                if (line == null || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    return;
                } else {
                    try {
                        var commandValue = parseLine(line);
                        performAction(out, commandValue);
                    } catch (ParseException e) {
                        out.writeBytes(e.getMessage() + "\n");
                    }
                }
                out.flush();
            } catch (IOException e) {
                System.out.println("Could not send response to client " + socket.getLocalAddress());
                return;
            }
        }
    }

    private void performAction(@NonNull DataOutputStream out, @NonNull CommandValue commandValue) throws IOException {
        try {
            var command = commandValue.getCommand().toUpperCase();
            switch (CommandType.valueOf(command)) {
                case GET:
                    out.writeBytes("{ \"value\": " + ThreadedServer.getState() + " }" + "\n");
                    break;
                case SET:
                    ThreadedServer.setState(commandValue.getValue());
                    ThreadedServer.sendToSubscribers("{ \"value\": " + ThreadedServer.getState() + " }" + "\n");
                    break;
                case SUBSCRIBE:
                    ThreadedServer.addSubscriber(socket.hashCode(), socket);
                    break;
            }
        } catch (IllegalArgumentException e) {
            out.writeBytes(format("Cannot find command: %s" + "\n", commandValue.getCommand()));
        }
    }

    /**
     * Parse command line to {@link CommandValue}.
     * @param line The command line.
     * @return The {@link CommandValue}.
     */
    public CommandValue parseLine(@NonNull String line) throws ParseException {
        var commandValue = new CommandValue();

        var matcher = PATTERN.matcher(line);
        if (matcher.find()) {
            commandValue.setCommand(matcher.group(1).trim());
            try {
                if (commandValue.getCommand().equals("set")) {
                    if (matcher.group(2) == null) {
                        throw new ParseException("Missing 'set' value.", 0);
                    }
                    var value = Long.parseLong(matcher.group(2).trim());
                    commandValue.setValue(value);
                }
            } catch(NumberFormatException e) {
                throw new ParseException(format("Incorrect value format. Maximum value is: %s", Long.MAX_VALUE), 0);
            }
        } else {
            throw new ParseException("Incorrect syntax. Required: { \"command\": \"\" }", 0);
        }
        return commandValue;
    }

}
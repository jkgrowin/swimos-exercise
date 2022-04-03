package dev.exercise.server.enums;

public enum CommandType {
    GET("get"),
    SET("set"),
    SUBSCRIBE("subscribe");

    private final String command;

    CommandType(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}

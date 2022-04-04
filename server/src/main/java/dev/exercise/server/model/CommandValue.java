package dev.exercise.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.exercise.server.enums.CommandType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandValue {
    private CommandType command;
    private Long value;
}

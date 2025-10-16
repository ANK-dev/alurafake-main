package br.com.alura.AluraFake.task.dto;

import java.util.List;

public sealed interface NewTaskDTO permits NewOpenTextDTO, NewSingleChoiceDTO, NewMultipleChoiceDTO {
    Long getCourseId();
    String getStatement();
    Integer getOrder();
    default List<OptionDTO> getOptions() {
        return List.of();
    }
}

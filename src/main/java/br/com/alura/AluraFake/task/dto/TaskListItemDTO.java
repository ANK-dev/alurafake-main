package br.com.alura.AluraFake.task.dto;

import br.com.alura.AluraFake.task.Task;
import br.com.alura.AluraFake.task.Type;

import java.util.List;
import java.util.stream.Collectors;

public class TaskListItemDTO {

    private Long id;
    private Integer order;
    private String statement;
    private Type type;
    private List<OptionListItemDTO> options;

    public TaskListItemDTO(Task task) {
        this.id = task.getId();
        this.order = task.getOrderIndex();
        this.statement = task.getStatement();
        this.type = task.getType();
        this.options = task.getOptions().stream()
                .map(OptionListItemDTO::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public Integer getOrder() {
        return order;
    }

    public String getStatement() {
        return statement;
    }

    public Type getType() {
        return type;
    }

    public List<OptionListItemDTO> getOptions() {
        return options;
    }
}
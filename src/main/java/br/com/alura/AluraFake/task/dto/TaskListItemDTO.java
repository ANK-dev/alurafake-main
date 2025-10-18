package br.com.alura.AluraFake.task.dto;

import br.com.alura.AluraFake.task.Task;
import br.com.alura.AluraFake.task.Type;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TaskListItemDTO implements Serializable {

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
                .toList();
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

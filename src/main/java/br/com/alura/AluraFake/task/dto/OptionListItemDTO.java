package br.com.alura.AluraFake.task.dto;

import br.com.alura.AluraFake.task.TaskOption;

public class OptionListItemDTO {

    private Long id;
    private String option;
    private boolean isCorrect;

    public OptionListItemDTO(TaskOption option) {
        this.id = option.getId();
        this.option = option.getOptionText();
        this.isCorrect = option.isCorrect();
    }

    public Long getId() {
        return id;
    }

    public String getOption() {
        return option;
    }

    public boolean getIsCorrect() {
        return isCorrect;
    }
}
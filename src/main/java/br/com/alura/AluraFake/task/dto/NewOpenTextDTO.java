package br.com.alura.AluraFake.task.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public final class NewOpenTextDTO implements NewTaskDTO {
    @NotNull
    private Long courseId;

    @NotBlank
    @Length(min = 4, max = 255)
    private String statement;

    @NotNull
    @Min(1)
    private Integer order;

    public NewOpenTextDTO() {}

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}

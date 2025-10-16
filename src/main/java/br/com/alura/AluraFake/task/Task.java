package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer orderIndex;
    private String statement;
    @Enumerated(EnumType.STRING)
    private Type type;
    @ManyToOne(optional = false)
    private Course course;
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskOption> taskOptions = new ArrayList<>();

    @Deprecated
    public Task() {}

    public Task(Course course, String statement, Integer orderIndex, Type type) {
        this.course = course;
        this.statement = statement;
        this.orderIndex = orderIndex;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getStatement() {
        return statement;
    }

    public Type getType() {
        return type;
    }

    public Course getCourse() {
        return course;
    }

    public List<TaskOption> getOptions() {
        return taskOptions;
    }

    public void addOption(TaskOption taskOption) {
        taskOption.setTask(this);
        this.taskOptions.add(taskOption);
    }

}

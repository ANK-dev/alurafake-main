package br.com.alura.AluraFake.course;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDetailListItemDTO implements Serializable {

    private Long id;
    private String title;
    private String description;
    private Status status;
    private LocalDateTime publishedAt;
    private Integer totalCourseTasks;

    public CourseDetailListItemDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Integer getTotalCourseTasks() {
        return totalCourseTasks;
    }

    public void setTotalCourseTasks(Integer totalCourseTasks) {
        this.totalCourseTasks = totalCourseTasks;
    }
}

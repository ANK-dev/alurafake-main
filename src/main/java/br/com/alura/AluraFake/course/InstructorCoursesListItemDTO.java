package br.com.alura.AluraFake.course;

import java.io.Serializable;
import java.util.List;

public class InstructorCoursesListItemDTO implements Serializable {
    private Integer totalInstructorCourses;
    private List<CourseDetailListItemDTO> coursesDetail;

    public InstructorCoursesListItemDTO(Integer totalInstructorCourses, List<CourseDetailListItemDTO> coursesDetail) {
        this.totalInstructorCourses = totalInstructorCourses;
        this.coursesDetail = coursesDetail;
    }

    public Integer getTotalInstructorCourses() {
        return totalInstructorCourses;
    }

    public List<CourseDetailListItemDTO> getCoursesDetail() {
        return coursesDetail;
    }
}

package br.com.alura.AluraFake.course;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long>{

    int countByInstructorId(Long instructorId);
    List<Course> findByInstructorId(Long instructorId);
}

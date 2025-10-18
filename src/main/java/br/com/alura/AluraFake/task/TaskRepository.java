package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByCourseAndStatement(Course course, String statement);

    int countByCourseId(Long courseId);

    List<Task> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE Task t SET t.orderIndex = t.orderIndex + 1 WHERE t.course_id = :courseId AND t.orderIndex >= :from ORDER BY orderIndex DESC", nativeQuery = true)
    int shiftOrders(Long courseId, Integer from);
}

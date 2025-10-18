package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByCourseAndStatement__should_return_true_when_statement_present_in_course() {
        User user = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);
        userRepository.save(user);

        Course course = new Course("Java", "Curso de Java", user);
        courseRepository.save(course);

        Task t = new Task(course, "O que aprendemos hoje?", 1, Type.OPEN_TEXT);
        taskRepository.save(t);

        boolean exists = taskRepository.existsByCourseAndStatement(course, "O que aprendemos hoje?");
        assertThat(exists).isTrue();

        boolean notExists = taskRepository.existsByCourseAndStatement(course, "Outro statement");
        assertThat(notExists).isFalse();
    }

    @Test
    void countByCourseId__should_return_task_count() {
        User user = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);
        userRepository.save(user);

        Course course1 = new Course("Java", "Curso de Java", user);
        Course course2 = new Course("Python", "Curso de Python", user);

        courseRepository.save(course1);
        courseRepository.save(course2);

        Task course1task1 = new Task(course1, "Java Task 1", 1, Type.OPEN_TEXT);
        Task course1task2 = new Task(course1, "Java Task 2", 2, Type.OPEN_TEXT);
        Task course2task1 = new Task(course2, "Python Task 1", 1, Type.OPEN_TEXT);

        taskRepository.save(course1task1);
        taskRepository.save(course1task2);
        taskRepository.save(course2task1);

        int countCourse1 = taskRepository.countByCourseId(course1.getId());
        int countCourse2 = taskRepository.countByCourseId(course2.getId());

        assertThat(countCourse1).isEqualTo(2);
        assertThat(countCourse2).isEqualTo(1);
    }

    @Test
    void findByCourseIdOrderByOrderIndexAsc__should_return_course_tasks_ordered_by_order_index_ascending() {
        User user = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);
        userRepository.save(user);

        Course course = new Course("Java", "Curso de Java", user);
        courseRepository.save(course);

        taskRepository.save(new Task(course, "Java Task 3", 3, Type.OPEN_TEXT));
        taskRepository.save(new Task(course, "Java Task 1", 1, Type.OPEN_TEXT));
        taskRepository.save(new Task(course, "Java Task 2", 2, Type.OPEN_TEXT));

        List<Task> tasks = taskRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());

        assertThat(tasks.size()).isEqualTo(3);
        assertThat(tasks.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(tasks.get(0).getStatement()).isEqualTo("Java Task 1");
        assertThat(tasks.get(1).getOrderIndex()).isEqualTo(2);
        assertThat(tasks.get(1).getStatement()).isEqualTo("Java Task 2");
        assertThat(tasks.get(2).getOrderIndex()).isEqualTo(3);
        assertThat(tasks.get(2).getStatement()).isEqualTo("Java Task 3");
    }

    @Test
    void shiftOrders__should_update_order_indexes() {
        User user = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);
        userRepository.save(user);

        Course course = new Course("Java", "Curso de Java", user);
        courseRepository.save(course);

        Task task1 = new Task(course, "Java Task 1", 1, Type.OPEN_TEXT);
        Task task2 = new Task(course, "Java Task 2", 2, Type.OPEN_TEXT);
        Task task3 = new Task(course, "Java Task 3", 3, Type.OPEN_TEXT);
        Task task4 = new Task(course, "Java Task 4", 4, Type.OPEN_TEXT);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);

        int rowsChanged = taskRepository.shiftOrders(course.getId(), 2);
        assertThat(rowsChanged).isEqualTo(3);

        List<Task> updatedTasks = taskRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());
        assertThat(updatedTasks.size()).isEqualTo(4);
        assertThat(updatedTasks.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(updatedTasks.get(1).getOrderIndex()).isEqualTo(3);
        assertThat(updatedTasks.get(2).getOrderIndex()).isEqualTo(4);
        assertThat(updatedTasks.get(3).getOrderIndex()).isEqualTo(5);
    }
}

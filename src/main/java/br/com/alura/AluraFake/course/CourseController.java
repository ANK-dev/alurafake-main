package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.task.Type;
import br.com.alura.AluraFake.task.dto.TaskListItemDTO;
import br.com.alura.AluraFake.user.*;
import br.com.alura.AluraFake.util.ErrorItemDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public CourseController(CourseRepository courseRepository, UserRepository userRepository, TaskRepository taskRepository){
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    @PostMapping("/course/new")
    public ResponseEntity createCourse(@Valid @RequestBody NewCourseDTO newCourse) {

        //Caso implemente o bonus, pegue o instrutor logado
        Optional<User> possibleAuthor = userRepository
                .findByEmail(newCourse.getEmailInstructor())
                .filter(User::isInstructor);

        if(possibleAuthor.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorItemDTO("emailInstructor", "Usuário não é um instrutor"));
        }

        Course course = new Course(newCourse.getTitle(), newCourse.getDescription(), possibleAuthor.get());

        courseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/course/all")
    public ResponseEntity<List<CourseListItemDTO>> createCourse() {
        List<CourseListItemDTO> courses = courseRepository.findAll().stream()
                .map(CourseListItemDTO::new)
                .toList();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/course/{id}/publish")
    public ResponseEntity createCourse(@PathVariable("id") Long id) {
        Optional<Course> courseOptional = courseRepository.findById(id);
        if (courseOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Course course = courseOptional.get();

        List<TaskListItemDTO> tasks = taskRepository
                .findByCourseIdOrderByOrderIndexAsc(id)
                .stream()
                .map(TaskListItemDTO::new)
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO("tasks", "No tasks are present"));
        }

        if (tasks.size() < 3) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO("tasks", "At least 3 tasks are required"));
        }

        Set<Type> requiredTypes = Set.of(Type.values());
        Set<Type> foundTypes = tasks.stream().map(TaskListItemDTO::getType).collect(Collectors.toSet());

        if (!foundTypes.containsAll(requiredTypes)) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO("tasks", "At least 1 task of each type is required"));
        }

        boolean consecutive = tasks.getLast().getOrder() - tasks.getFirst().getOrder() == tasks.size() - 1;
        if (!consecutive) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO("order", "Order is not consecutive"));
        }

        if (!Status.BUILDING.equals(course.getStatus())) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO("status", "Status is not BUILDING"));
        }

        course.setStatus(Status.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());

        courseRepository.save(course);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/course/{id}/tasks")
    public ResponseEntity<List<TaskListItemDTO>> listCourseTasks(@PathVariable("id") Long id) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<TaskListItemDTO> tasks = taskRepository
                .findByCourseIdOrderByOrderIndexAsc(id)
                .stream()
                .map(TaskListItemDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(tasks);
    }

}

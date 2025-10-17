package br.com.alura.AluraFake.instructor;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseDetailListItemDTO;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.InstructorCoursesListItemDTO;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import br.com.alura.AluraFake.util.ErrorItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class InstructorController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public InstructorController(UserRepository userRepository, CourseRepository courseRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    @GetMapping("/instructor/{userId}/courses")
    public ResponseEntity instructorCourses(@PathVariable("userId") Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();

        if (!user.isInstructor()) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO("role", "User role is not INSTRUCTOR"));
        }

        List<Course> courses = courseRepository.findByInstructorId(userId);

        if (courses.isEmpty()) {
            return ResponseEntity.ok().body(List.of());
        }

        int totalInstructorCourses = courseRepository.countByInstructorId(userId);

        List<CourseDetailListItemDTO> coursesDetail = new ArrayList<>();

        for (Course course : courses) {
            Long courseId = course.getId();
            int totalCourseTasks = taskRepository.countByCourseId(courseId);

            CourseDetailListItemDTO courseDetail = new CourseDetailListItemDTO();
            courseDetail.setId(courseId);
            courseDetail.setTitle(course.getTitle());
            courseDetail.setDescription(course.getDescription());
            courseDetail.setStatus(course.getStatus());
            courseDetail.setPublishedAt(course.getPublishedAt());
            courseDetail.setTotalCourseTasks(totalCourseTasks);

            coursesDetail.add(courseDetail);
        }

        InstructorCoursesListItemDTO dto = new InstructorCoursesListItemDTO(totalInstructorCourses, coursesDetail);

        return ResponseEntity.ok().body(dto);
    }
}

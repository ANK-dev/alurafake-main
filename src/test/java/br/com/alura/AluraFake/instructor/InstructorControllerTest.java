package br.com.alura.AluraFake.instructor;

import br.com.alura.AluraFake.course.*;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstructorController.class)
public class InstructorControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CourseRepository courseRepository;
    @MockBean
    private TaskRepository taskRepository;

    private static final String INSTRUCTOR_COURSES_ENDPOINT = "/instructor/{userId}/courses";

    User sampleUser;
    List<Course> sampleCourses;
    CourseDetailListItemDTO sampleDto0;
    CourseDetailListItemDTO sampleDto1;
    CourseDetailListItemDTO sampleDto2;

    @BeforeEach
    void setup() {
        sampleUser = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);
        ReflectionTestUtils.setField(sampleUser, "id", 1L);

        Course course1 = new Course("Java", "Curso de Java", sampleUser);
        ReflectionTestUtils.setField(course1, "id", 1L);

        Course course2 = new Course("Python", "Curso de Python", sampleUser);
        ReflectionTestUtils.setField(course2, "id", 2L);
        course2.setStatus(Status.PUBLISHED);
        course2.setPublishedAt(LocalDateTime.of(2025, 10, 18, 12, 00, 00));

        Course course3 = new Course("Ruby", "Curso de Ruby", sampleUser);
        ReflectionTestUtils.setField(course3, "id", 3L);

        sampleCourses = List.of(course1, course2, course3);

        sampleDto0 = new CourseDetailListItemDTO();
        sampleDto0.setId(course1.getId());
        sampleDto0.setTitle(course1.getTitle());
        sampleDto0.setDescription(course1.getDescription());
        sampleDto0.setStatus(course1.getStatus());
        sampleDto0.setPublishedAt(course1.getPublishedAt());
        sampleDto0.setTotalCourseTasks(10);

        sampleDto1 = new CourseDetailListItemDTO();
        sampleDto1.setId(course2.getId());
        sampleDto1.setTitle(course2.getTitle());
        sampleDto1.setDescription(course2.getDescription());
        sampleDto1.setStatus(course2.getStatus());
        sampleDto1.setPublishedAt(course2.getPublishedAt());
        sampleDto1.setTotalCourseTasks(9);

        sampleDto2 = new CourseDetailListItemDTO();
        sampleDto2.setId(course3.getId());
        sampleDto2.setTitle(course3.getTitle());
        sampleDto2.setDescription(course3.getDescription());
        sampleDto2.setStatus(course3.getStatus());
        sampleDto2.setPublishedAt(course3.getPublishedAt());
        sampleDto2.setTotalCourseTasks(8);
    }

    @Test
    void instructorCourses__should_return_not_found_when_user_missing() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get(INSTRUCTOR_COURSES_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void instructorCourses__should_return_bad_request_when_user_not_instructor() throws Exception {
        sampleUser = new User("Paulo", "paulo@alura.com.br", Role.STUDENT);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get(INSTRUCTOR_COURSES_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("role"))
                .andExpect(jsonPath("$.message").value("User role is not INSTRUCTOR"));
    }

    @Test
    void instructorCourses__should_return_empty_list_when_courses_missing() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(courseRepository.findByInstructorId(1L)).thenReturn(List.of());

        mockMvc.perform(get(INSTRUCTOR_COURSES_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void instructorCourses__should_list_courses_from_instructor() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(courseRepository.findByInstructorId(1L)).thenReturn(sampleCourses);
        when(courseRepository.countByInstructorId(1L)).thenReturn(sampleCourses.size());

        when(taskRepository.countByCourseId(1L)).thenReturn(sampleDto0.getTotalCourseTasks());
        when(taskRepository.countByCourseId(2L)).thenReturn(sampleDto1.getTotalCourseTasks());
        when(taskRepository.countByCourseId(3L)).thenReturn(sampleDto2.getTotalCourseTasks());

        mockMvc.perform(get(INSTRUCTOR_COURSES_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInstructorCourses").value(3))
                .andExpect(jsonPath("$.coursesDetail[0].id").value(1))
                .andExpect(jsonPath("$.coursesDetail[0].title").value("Java"))
                .andExpect(jsonPath("$.coursesDetail[0].description").value("Curso de Java"))
                .andExpect(jsonPath("$.coursesDetail[0].status").value("BUILDING"))
                .andExpect(jsonPath("$.coursesDetail[0].totalCourseTasks").value(10))
                .andExpect(jsonPath("$.coursesDetail[1].id").value(2))
                .andExpect(jsonPath("$.coursesDetail[1].title").value("Python"))
                .andExpect(jsonPath("$.coursesDetail[1].description").value("Curso de Python"))
                .andExpect(jsonPath("$.coursesDetail[1].status").value("PUBLISHED"))
                .andExpect(jsonPath("$.coursesDetail[1].publishedAt").value("2025-10-18T12:00:00"))
                .andExpect(jsonPath("$.coursesDetail[1].totalCourseTasks").value(9))
                .andExpect(jsonPath("$.coursesDetail[2].id").value(3))
                .andExpect(jsonPath("$.coursesDetail[2].title").value("Ruby"))
                .andExpect(jsonPath("$.coursesDetail[2].description").value("Curso de Ruby"))
                .andExpect(jsonPath("$.coursesDetail[2].status").value("BUILDING"))
                .andExpect(jsonPath("$.coursesDetail[2].totalCourseTasks").value(8));
    }
}


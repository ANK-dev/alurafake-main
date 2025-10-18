package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.Task;
import br.com.alura.AluraFake.task.TaskOption;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.task.Type;
import br.com.alura.AluraFake.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CourseRepository courseRepository;
    @MockBean
    private TaskRepository taskRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String COURSE_NEW_ENDPOINT = "/course/new";
    private static final String COURSE_ALL_ENDPOINT = "/course/all";
    private static final String COURSE_PUBLISH_ENDPOINT = "/course/{id}/publish";
    private static final String COURSE_TASKS_ENDPOINT = "/course/{id}/tasks";

    NewCourseDTO sampleNewCourseDTO;
    Course sampleCourse;

    Task sampleOpenTextTask;
    Task sampleSingleChoiceTask;
    Task sampleMultipleChoiceTask;
    Task sampleMultipleChoiceTask2;
    List<Task> sampleTasks;

    @BeforeEach
    void setup() {
        String title = "Java";
        String description = "Curso de Java";
        String emailInstructor = "paulo@alura.com.br";
        User user = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);

        sampleNewCourseDTO = new NewCourseDTO();
        sampleNewCourseDTO.setTitle(title);
        sampleNewCourseDTO.setDescription(description);
        sampleNewCourseDTO.setEmailInstructor(emailInstructor);

        sampleCourse = new Course(title, description, user);

        sampleOpenTextTask = new Task(sampleCourse, "Descreva a lição de hoje", 1, Type.OPEN_TEXT);

        sampleSingleChoiceTask = new Task(sampleCourse, "O que aprendemos hoje?", 2, Type.SINGLE_CHOICE);
        sampleSingleChoiceTask.addOption(new TaskOption("Java", true));
        sampleSingleChoiceTask.addOption(new TaskOption("Python", false));

        sampleMultipleChoiceTask = new Task(sampleCourse, "Quais linguagens são ensinadas no curso?", 3, Type.MULTIPLE_CHOICE);
        sampleMultipleChoiceTask.addOption(new TaskOption("COBOL", false));
        sampleMultipleChoiceTask.addOption(new TaskOption("Java", true));
        sampleMultipleChoiceTask.addOption(new TaskOption("Python", true));

        sampleMultipleChoiceTask2 = new Task(sampleCourse, "Quais linguagens não são ensinadas no curso?", 4, Type.MULTIPLE_CHOICE);
        sampleMultipleChoiceTask2.addOption(new TaskOption("COBOL", true));
        sampleMultipleChoiceTask2.addOption(new TaskOption("Java", false));
        sampleMultipleChoiceTask2.addOption(new TaskOption("FORTRAN", true));

        sampleTasks = List.of(sampleOpenTextTask, sampleSingleChoiceTask, sampleMultipleChoiceTask, sampleMultipleChoiceTask2);
    }

    @Test
    void newCourseDTO__should_return_bad_request_when_email_is_invalid() throws Exception {

        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("paulo@alura.com.br");

        doReturn(Optional.empty()).when(userRepository)
                .findByEmail(newCourseDTO.getEmailInstructor());

        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("emailInstructor"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }


    @Test
    void newCourseDTO__should_return_bad_request_when_email_is_no_instructor() throws Exception {

        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("paulo@alura.com.br");

        User user = mock(User.class);
        doReturn(false).when(user).isInstructor();

        doReturn(Optional.of(user)).when(userRepository)
                .findByEmail(newCourseDTO.getEmailInstructor());

        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("emailInstructor"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void newCourseDTO__should_return_created_when_new_course_request_is_valid() throws Exception {

        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("paulo@alura.com.br");

        User user = mock(User.class);
        doReturn(true).when(user).isInstructor();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(newCourseDTO.getEmailInstructor());

        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isCreated());

        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void listAllCourses__should_list_all_courses() throws Exception {
        User paulo = new User("Paulo", "paulo@alua.com.br", Role.INSTRUCTOR);

        Course java = new Course("Java", "Curso de java", paulo);
        Course hibernate = new Course("Hibernate", "Curso de hibernate", paulo);
        Course spring = new Course("Spring", "Curso de spring", paulo);

        when(courseRepository.findAll()).thenReturn(Arrays.asList(java, hibernate, spring));

        mockMvc.perform(get("/course/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"))
                .andExpect(jsonPath("$[0].description").value("Curso de java"))
                .andExpect(jsonPath("$[1].title").value("Hibernate"))
                .andExpect(jsonPath("$[1].description").value("Curso de hibernate"))
                .andExpect(jsonPath("$[2].title").value("Spring"))
                .andExpect(jsonPath("$[2].description").value("Curso de spring"));
    }

    @Test
    void listCourseTasks__should_return_not_found_when_course_missing() throws Exception {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get(COURSE_TASKS_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    void listCourseTasks__should_return_ordered_tasks() throws Exception {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(taskRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(sampleTasks);

        mockMvc.perform(get(COURSE_TASKS_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].order").value(1))
                .andExpect(jsonPath("$[1].order").value(2))
                .andExpect(jsonPath("$[2].order").value(3))
                .andExpect(jsonPath("$[3].order").value(4));

        verify(courseRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).findByCourseIdOrderByOrderIndexAsc(1L);
    }

    @Test
    void publishCourse__should_return_bad_request_when_tasks_missing() throws Exception {
        sampleTasks = List.of();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(taskRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(sampleTasks);

        mockMvc.perform(post(COURSE_PUBLISH_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("tasks"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(courseRepository, times(0)).save(any(Course.class));
    }

    @Test
    void publishCourse__should_return_bad_request_when_under_1_task_of_each_type() throws Exception {
        sampleTasks = List.of(sampleOpenTextTask, sampleMultipleChoiceTask, sampleMultipleChoiceTask2);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(taskRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(sampleTasks);

        mockMvc.perform(post(COURSE_PUBLISH_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("tasks"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(courseRepository, times(0)).save(any(Course.class));
    }

    @Test
    void publishCourse__should_return_bad_request_when_under_3_tasks() throws Exception {
        sampleTasks = List.of(sampleOpenTextTask, sampleMultipleChoiceTask);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(taskRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(sampleTasks);

        mockMvc.perform(post(COURSE_PUBLISH_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("tasks"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(courseRepository, times(0)).save(any(Course.class));
    }

    @Test
    void publishCourse__should_create_when_1_task_of_each_type() throws Exception {
        sampleTasks = List.of(
                sampleOpenTextTask,
                sampleSingleChoiceTask,
                sampleMultipleChoiceTask,
                sampleMultipleChoiceTask2
        );

        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(taskRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(sampleTasks);

        mockMvc.perform(post(COURSE_PUBLISH_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void publishCourse__should_return_bad_request_when_order_not_consecutive() throws Exception {
        sampleOpenTextTask.setOrderIndex(1);
        sampleSingleChoiceTask.setOrderIndex(2);
        sampleMultipleChoiceTask.setOrderIndex(4);
        sampleMultipleChoiceTask2.setOrderIndex(5);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(taskRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(sampleTasks);

        mockMvc.perform(post(COURSE_PUBLISH_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("order"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(courseRepository, times(0)).save(any(Course.class));
    }

    @Test
    void publishCourse__should_return_bad_request_when_status_published() throws Exception {
        sampleCourse.setStatus(Status.PUBLISHED);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(taskRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(sampleTasks);

        mockMvc.perform(post(COURSE_PUBLISH_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("status"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(courseRepository, times(0)).save(any(Course.class));
    }

    @Test
    void publishCourse__should_return_not_found_when_course_missing() throws Exception {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post(COURSE_PUBLISH_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(courseRepository, times(0)).save(any(Course.class));
    }

    @Test
    void publishCourse__should_update_status_and_publishedAt_when_valid() throws Exception {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(taskRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(sampleTasks);

        assertEquals(Status.BUILDING, sampleCourse.getStatus());
        assertNull(sampleCourse.getPublishedAt());

        mockMvc.perform(post(COURSE_PUBLISH_ENDPOINT, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(Status.PUBLISHED, sampleCourse.getStatus());
        assertNotNull(sampleCourse.getPublishedAt());

        verify(courseRepository, times(1)).save(any(Course.class));
    }
}

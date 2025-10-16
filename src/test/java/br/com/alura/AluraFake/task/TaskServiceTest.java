package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.NewMultipleChoiceDTO;
import br.com.alura.AluraFake.task.dto.NewOpenTextDTO;
import br.com.alura.AluraFake.task.dto.NewSingleChoiceDTO;
import br.com.alura.AluraFake.task.dto.OptionDTO;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.util.ErrorItemDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private TaskService taskService;

    // Helpers
    private User instructor() {
        return new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);
    }

    private OptionDTO option(String text, boolean correct) {
        OptionDTO o = new OptionDTO();
        o.setOption(text);
        o.setIsCorrect(correct);
        return o;
    }

    // -----------------------
    // OpenText tests
    // -----------------------

    @Test
    void createNewTask__openText_should_return_not_found_when_course_missing() {
        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(999L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void createNewTask__openText_should_return_bad_request_when_course_is_published() {
        Course course = new Course("Java", "desc", instructor());
        course.setStatus(Status.PUBLISHED);

        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertInstanceOf(ErrorItemDTO.class, resp.getBody());
        assertEquals("courseId", ((ErrorItemDTO) resp.getBody()).getField());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void createNewTask__openText_should_return_bad_request_when_order_invalid() {
        Course course = new Course("Java", "desc", instructor());

        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(999);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("order", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__openText_should_shift_and_save_when_insert_in_middle() {
        Course course = new Course("Java", "desc", instructor());

        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(2);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(2);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());

        verify(taskRepository, times(1)).shiftOrders(1L, 2);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createNewTask__openText_should_return_bad_request_when_statement_duplicate() {
        Course course = new Course("Java", "desc", instructor());

        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("Duplicate Statement");
        dto.setOrder(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(true);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("statement", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__openText_should_return_bad_request_when_order_null() {
        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("Order is null");
        dto.setOrder(null);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("order", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__openText_should_return_bad_request_when_multiple_fields_are_null() {
        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(null);
        dto.setStatement(null);
        dto.setOrder(null);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("courseId", ((List<ErrorItemDTO>) resp.getBody()).get(0).getField());
        assertEquals("statement", ((List<ErrorItemDTO>) resp.getBody()).get(1).getField());
        assertEquals("order", ((List<ErrorItemDTO>) resp.getBody()).get(2).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__openText_should_return_bad_request_when_order_less_than_one() {
        Course course = new Course("Java", "desc", instructor());

        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("Order < 1");
        dto.setOrder(0);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("order", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__openText_should_return_bad_request_when_statement_too_short() {
        Course course = new Course("Java", "desc", instructor());

        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("abc"); // 3 chars (min = 4)
        dto.setOrder(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertInstanceOf(ErrorItemDTO.class, resp.getBody());
        assertEquals("statement", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__openText_should_return_bad_request_when_statement_too_long() {
        Course course = new Course("Java", "desc", instructor());

        String longStatement = "x".repeat(256); // 256 chars (max = 255)

        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement(longStatement);
        dto.setOrder(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertInstanceOf(ErrorItemDTO.class, resp.getBody());
        assertEquals("statement", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__openText_should_create_when_statement_length_is_minimum() {
        Course course = new Course("Java", "desc", instructor());

        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("abcd"); // exactly 4 chars (min = 4)
        dto.setOrder(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(0);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createNewTask__openText_should_create_when_statement_length_is_maximum() {
        Course course = new Course("Java", "desc", instructor());

        String longStatement = "x".repeat(255); // exactly 255 chars (max = 255)

        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement(longStatement);
        dto.setOrder(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(0);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.OPEN_TEXT);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // -----------------------
    // SingleChoice tests
    // -----------------------

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_options_invalid_duplicate() {
        Course course = new Course("Java", "desc", instructor());

        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("Java", true),
                option("java", false) // duplicate ignoring case
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__singleChoice_should_create_when_valid() {
        Course course = new Course("Java", "desc", instructor());

        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("Java", true),
                option("Python", false),
                option("Ruby", false)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(0);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createNewTask__singleChoice_should_create_when_order_is_append_no_shift() {
        Course course = new Course("Java", "desc", instructor());

        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Append order");
        dto.setOrder(2); // requestedOrder == existingCount + 1
        dto.setOptions(Arrays.asList(
                option("Java", true),
                option("Python", false)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(1); // existingCount = 1

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());

        verify(taskRepository, never()).shiftOrders(anyLong(), anyInt());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_options_null() {
        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Options is null");
        dto.setOrder(1);
        dto.setOptions(null);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_option_too_short() {
        Course course = new Course("Java", "desc", instructor());

        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Option text too short");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("abc", true),  // option text is 3 chars (min = 4)
                option("Python", false)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_option_equals_statement() {
        Course course = new Course("Java", "desc", instructor());

        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Same text");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("Same text", true),
                option("Other", false)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_no_correct_option() {
        Course course = new Course("Java", "desc", instructor());

        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("No correct option");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("aaaa", false),
                option("bbbb", false)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_all_options_correct() {
        Course course = new Course("Java", "desc", instructor());

        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("All options are correct");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("aaaa", true),
                option("bbbb", true)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_option_too_long() {
        Course course = new Course("Java", "desc", instructor());

        String longOption = "x".repeat(81); // option text is over 80 chars (max = 80)

        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Long option statement");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option(longOption, true),
                option("Valid option", false)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_too_few_options_single_choice() {
        Course course = new Course("Java", "desc", instructor());

        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Too few options");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("aaaa", true) // only 1 option (min = 2)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.SINGLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    // -----------------------
    // MultipleChoice tests
    // -----------------------

    @Test
    void createNewTask__multipleChoice_should_return_bad_request_when_all_correct() {
        Course course = new Course("Java", "desc", instructor());

        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("Java", true),
                option("Spring", true),
                option("Kotlin", true)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.MULTIPLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__multipleChoice_should_create_when_valid_and_shift_if_needed() {
        Course course = new Course("Java", "desc", instructor());

        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(2);
        dto.setOptions(Arrays.asList(
                option("Java", true),
                option("Spring", true),
                option("Ruby", false)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(2); // will cause shift

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.MULTIPLE_CHOICE);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        verify(taskRepository, times(1)).shiftOrders(1L, 2);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createNewTask__multipleChoice_should_return_bad_request_when_only_one_correct() {
        Course course = new Course("Java", "desc", instructor());

        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Only one correct option");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("Java", true),
                option("Spring", false),
                option("Ruby", false)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.MULTIPLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    // New: multiple choice too few options (less than 3)
    @Test
    void createNewTask__multipleChoice_should_return_bad_request_when_too_few_options() {
        Course course = new Course("Java", "desc", instructor());

        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Too few options");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("Option A", true),
                option("Option B", false) // 2 options (min = 3)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.MULTIPLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__multipleChoice_should_return_bad_request_when_too_many_options() {
        Course course = new Course("Java", "desc", instructor());

        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Too many options");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                option("aaaa", true),
                option("bbbb", false),
                option("cccc", false),
                option("dddd", false),
                option("eeee", false),
                option("ffff", false)   // 6 options (max = 5)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.MULTIPLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__multipleChoice_should_return_bad_request_when_option_has_null_text() {
        Course course = new Course("Java", "desc", instructor());

        OptionDTO nullOption = new OptionDTO();
        nullOption.setOption(null);
        nullOption.setIsCorrect(true);

        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Null option");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                nullOption,
                option("aaaa", true),
                option("bbbb", false)
        ));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseId(1L)).thenReturn(0);
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.MULTIPLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createNewTask__multipleChoice_should_return_bad_request_when_options_null() {
        OptionDTO nullOption = new OptionDTO();
        nullOption.setOption(null);
        nullOption.setIsCorrect(true);

        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("Null option");
        dto.setOrder(1);
        dto.setOptions(null);

        ResponseEntity<?> resp = taskService.createNewTask(dto, Type.MULTIPLE_CHOICE);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());
        verify(taskRepository, never()).save(any());
    }
}
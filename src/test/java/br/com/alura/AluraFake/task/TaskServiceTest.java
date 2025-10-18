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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

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

    private NewOpenTextDTO sampleOpenTextDTO;
    private NewSingleChoiceDTO sampleSingleChoiceDTO;
    private NewMultipleChoiceDTO sampleMultipleChoiceDTO;
    private Course sampleCourse;

    @BeforeEach
    void setup() {
        String statement = "O que aprendemos hoje?";
        Long courseId = 1L;
        Integer order = 1;
        List<OptionDTO> singleChoiceOptions = List.of(
                new OptionDTO("Java", true),
                new OptionDTO("Python", false)
        );
        List<OptionDTO> multipleChoiceOptions = List.of(
                new OptionDTO("Java", true),
                new OptionDTO("Spring", true),
                new OptionDTO("Ruby", false)
        );

        sampleOpenTextDTO = new NewOpenTextDTO();
        sampleOpenTextDTO.setCourseId(courseId);
        sampleOpenTextDTO.setStatement(statement);
        sampleOpenTextDTO.setOrder(order);

        sampleSingleChoiceDTO = new NewSingleChoiceDTO();
        sampleSingleChoiceDTO.setCourseId(courseId);
        sampleSingleChoiceDTO.setStatement(statement);
        sampleSingleChoiceDTO.setOrder(order);
        sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

        sampleMultipleChoiceDTO = new NewMultipleChoiceDTO();
        sampleMultipleChoiceDTO.setCourseId(courseId);
        sampleMultipleChoiceDTO.setStatement(statement);
        sampleMultipleChoiceDTO.setOrder(order);
        sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

        sampleCourse = new Course(
                "Java",
                "desc",
                new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR)
        );
    }

    // -----------------------
    // General tests
    // -----------------------

    @Nested
    class GeneralTests {
        @Test
        void createNewTask__should_return_bad_request_when_statement_length_under_4_chars() {
            String shortStatement = "abc";
            sampleOpenTextDTO.setStatement(shortStatement);
            sampleSingleChoiceDTO.setStatement(shortStatement);
            sampleMultipleChoiceDTO.setStatement(shortStatement);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

            String expectedField = "statement";

            ResponseEntity<?> openTextResponse = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.BAD_REQUEST, openTextResponse.getStatusCode());
            assertNotNull(openTextResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) openTextResponse.getBody()).getField());

            ResponseEntity<?> singleChoiceResponse = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, singleChoiceResponse.getStatusCode());
            assertNotNull(singleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) singleChoiceResponse.getBody()).getField());

            ResponseEntity<?> multipleChoiceResponse = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, multipleChoiceResponse.getStatusCode());
            assertNotNull(multipleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) multipleChoiceResponse.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__should_create_when_statement_length_4_chars() {
            String minimumStatement = "abcd"; // exactly 4 chars (min = 4)
            sampleOpenTextDTO.setStatement(minimumStatement);
            sampleSingleChoiceDTO.setStatement(minimumStatement);
            sampleMultipleChoiceDTO.setStatement(minimumStatement);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);

            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleOpenTextDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> responseOpenText = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseOpenText.getStatusCode());

            ResponseEntity<?> responseSingleChoice = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseSingleChoice.getStatusCode());

            ResponseEntity<?> responseMultipleChoice = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseMultipleChoice.getStatusCode());

            verify(taskRepository, times(3)).save(any(Task.class));
        }

        @Test
        void createNewTask__should_return_bad_request_when_statement_length_over_255_chars() {
            String longStatement = "x".repeat(256); // 256 chars (max = 255)
            sampleOpenTextDTO.setStatement(longStatement);
            sampleSingleChoiceDTO.setStatement(longStatement);
            sampleMultipleChoiceDTO.setStatement(longStatement);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

            String expectedField = "statement";

            ResponseEntity<?> openTextResponse = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.BAD_REQUEST, openTextResponse.getStatusCode());
            assertNotNull(openTextResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) openTextResponse.getBody()).getField());

            ResponseEntity<?> singleChoiceResponse = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, singleChoiceResponse.getStatusCode());
            assertNotNull(singleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) singleChoiceResponse.getBody()).getField());

            ResponseEntity<?> multipleChoiceResponse = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, multipleChoiceResponse.getStatusCode());
            assertNotNull(multipleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) multipleChoiceResponse.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask___should_create_when_statement_length_255_chars() {
            String maximumStatement = "x".repeat(255); // exactly 255 chars (max = 255)
            sampleOpenTextDTO.setStatement(maximumStatement);
            sampleSingleChoiceDTO.setStatement(maximumStatement);
            sampleMultipleChoiceDTO.setStatement(maximumStatement);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);

            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleOpenTextDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> responseOpenText = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseOpenText.getStatusCode());

            ResponseEntity<?> responseSingleChoice = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseSingleChoice.getStatusCode());

            ResponseEntity<?> responseMultipleChoice = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseMultipleChoice.getStatusCode());

            verify(taskRepository, times(3)).save(any(Task.class));
        }

        @Test
        void createNewTask__should_return_bad_request_when_statement_duplicate_for_course() {
            String statement = "Duplicate statement";
            sampleOpenTextDTO.setStatement(statement);
            sampleSingleChoiceDTO.setStatement(statement);
            sampleMultipleChoiceDTO.setStatement(statement);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleOpenTextDTO.getStatement())).thenReturn(true);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(true);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(true);

            String expectedField = "statement";

            ResponseEntity<?> openTextResponse = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.BAD_REQUEST, openTextResponse.getStatusCode());
            assertNotNull(openTextResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) openTextResponse.getBody()).getField());

            ResponseEntity<?> singleChoiceResponse = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, singleChoiceResponse.getStatusCode());
            assertNotNull(singleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) singleChoiceResponse.getBody()).getField());

            ResponseEntity<?> multipleChoiceResponse = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, multipleChoiceResponse.getStatusCode());
            assertNotNull(multipleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) multipleChoiceResponse.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__should_return_bad_request_when_order_less_than_1() {
            Integer order = 0;
            sampleOpenTextDTO.setOrder(order);
            sampleSingleChoiceDTO.setOrder(order);
            sampleMultipleChoiceDTO.setOrder(order);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleOpenTextDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            String expectedField = "order";

            ResponseEntity<?> openTextResponse = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.BAD_REQUEST, openTextResponse.getStatusCode());
            assertNotNull(openTextResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) openTextResponse.getBody()).getField());

            ResponseEntity<?> singleChoiceResponse = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, singleChoiceResponse.getStatusCode());
            assertNotNull(singleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) singleChoiceResponse.getBody()).getField());

            ResponseEntity<?> multipleChoiceResponse = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, multipleChoiceResponse.getStatusCode());
            assertNotNull(multipleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) multipleChoiceResponse.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__should_shift_order_and_save_when_inserted_in_middle() {
            sampleOpenTextDTO.setOrder(3);
            sampleSingleChoiceDTO.setOrder(3);
            sampleMultipleChoiceDTO.setOrder(3);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleOpenTextDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            // Course already contains multiple tasks, insertion of the new task will cause a shift in the sequence
            when(taskRepository.countByCourseId(1L)).thenReturn(5);

            ResponseEntity<?> responseOpenText = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseOpenText.getStatusCode());

            ResponseEntity<?> responseSingleChoice = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseSingleChoice.getStatusCode());

            ResponseEntity<?> responseMultipleChoice = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseMultipleChoice.getStatusCode());

            verify(taskRepository, times(3)).shiftOrders(1L, 3);
            verify(taskRepository, times(3)).save(any(Task.class));
        }

        @Test
        void createNewTask__singleChoice_should_create_when_order_is_append_no_shift() {
            sampleOpenTextDTO.setOrder(3);
            sampleSingleChoiceDTO.setOrder(3);
            sampleMultipleChoiceDTO.setOrder(3);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleOpenTextDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            // Course already contains multiple tasks, insertion of the new task will only append the sequence
            when(taskRepository.countByCourseId(1L)).thenReturn(2);

            ResponseEntity<?> responseOpenText = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseOpenText.getStatusCode());

            ResponseEntity<?> responseSingleChoice = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseSingleChoice.getStatusCode());

            ResponseEntity<?> responseMultipleChoice = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseMultipleChoice.getStatusCode());

            verify(taskRepository, times(0)).shiftOrders(anyLong(), anyInt());
            verify(taskRepository, times(3)).save(any(Task.class));
        }

        @Test
        void createNewTask__should_return_bad_request_when_order_invalid() {
            sampleOpenTextDTO.setOrder(10);
            sampleSingleChoiceDTO.setOrder(10);
            sampleMultipleChoiceDTO.setOrder(10);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(7);

            String expectedField = "order";

            ResponseEntity<?> responseOpenText = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.BAD_REQUEST, responseOpenText.getStatusCode());
            assertNotNull(responseOpenText.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) responseOpenText.getBody()).getField());

            ResponseEntity<?> responseSingleChoice = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, responseSingleChoice.getStatusCode());
            assertNotNull(responseSingleChoice.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) responseSingleChoice.getBody()).getField());

            ResponseEntity<?> responseMultipleChoice = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, responseMultipleChoice.getStatusCode());
            assertNotNull(responseMultipleChoice.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) responseMultipleChoice.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__should_return_bad_request_when_course_published()  {
            sampleCourse.setStatus(Status.PUBLISHED);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

            String expectedField = "status";

            ResponseEntity<?> openTextResponse = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.BAD_REQUEST, openTextResponse.getStatusCode());
            assertNotNull(openTextResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) openTextResponse.getBody()).getField());

            ResponseEntity<?> singleChoiceResponse = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, singleChoiceResponse.getStatusCode());
            assertNotNull(singleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) singleChoiceResponse.getBody()).getField());

            ResponseEntity<?> multipleChoiceResponse = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, multipleChoiceResponse.getStatusCode());
            assertNotNull(multipleChoiceResponse.getBody());
            assertEquals(expectedField, ((ErrorItemDTO) multipleChoiceResponse.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @SuppressWarnings("unchecked")
        @Test
        void createNewTask__should_return_bad_request_when_fields_are_null_or_blank() {
            sampleOpenTextDTO.setCourseId(null);
            sampleOpenTextDTO.setStatement("");
            sampleOpenTextDTO.setOrder(null);

            sampleSingleChoiceDTO.setCourseId(null);
            sampleSingleChoiceDTO.setStatement("");
            sampleSingleChoiceDTO.setOrder(null);
            sampleSingleChoiceDTO.setOptions(null);

            sampleMultipleChoiceDTO.setCourseId(null);
            sampleMultipleChoiceDTO.setStatement("");
            sampleMultipleChoiceDTO.setOrder(null);
            sampleMultipleChoiceDTO.setOptions(null);

            Set<String> expectedFieldsOpenText = Set.of("courseId", "statement", "order");
            Set<String> expectedFieldsChoice = Set.of("courseId", "statement", "order", "options");

            ResponseEntity<?> openTextResponse = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.BAD_REQUEST, openTextResponse.getStatusCode());
            assertNotNull(openTextResponse.getBody());
            Set<String> bodyFieldsOpenText = ((List<ErrorItemDTO>) openTextResponse.getBody()).stream()
                    .map(ErrorItemDTO::getField)
                    .collect(Collectors.toSet());
            assertTrue(bodyFieldsOpenText.containsAll(expectedFieldsOpenText));

            ResponseEntity<?> singleChoiceResponse = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, singleChoiceResponse.getStatusCode());
            assertNotNull(singleChoiceResponse.getBody());
            Set<String> bodyFieldsSingleChoice = ((List<ErrorItemDTO>) singleChoiceResponse.getBody()).stream()
                    .map(ErrorItemDTO::getField)
                    .collect(Collectors.toSet());
            assertTrue(bodyFieldsSingleChoice.containsAll(expectedFieldsChoice));

            ResponseEntity<?> multipleChoiceResponse = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, multipleChoiceResponse.getStatusCode());
            assertNotNull(multipleChoiceResponse.getBody());
            Set<String> bodyFieldsMultipleChoice = ((List<ErrorItemDTO>) multipleChoiceResponse.getBody()).stream()
                    .map(ErrorItemDTO::getField)
                    .collect(Collectors.toSet());
            assertTrue(bodyFieldsMultipleChoice.containsAll(expectedFieldsChoice));

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__should_return_not_found_when_course_missing() {
            when(courseRepository.findById(1L)).thenReturn(Optional.empty());

            ResponseEntity<?> openTextResponse = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.NOT_FOUND, openTextResponse.getStatusCode());

            ResponseEntity<?> singleChoiceResponse = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.NOT_FOUND, singleChoiceResponse.getStatusCode());

            ResponseEntity<?> multipleChoiceResponse = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.NOT_FOUND, multipleChoiceResponse.getStatusCode());

            verifyNoInteractions(taskRepository);
        }
    }

    // -----------------------
    // OpenText tests
    // -----------------------

    @Nested
    class OpenTextTests {
        @Test
        void createNewTask__openText_should_create_when_valid() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleOpenTextDTO.getStatement())).thenReturn(false);
            when(taskRepository.countByCourseId(1L)).thenReturn(0);

            ResponseEntity<?> responseOpenText = taskService.createNewTask(sampleOpenTextDTO, Type.OPEN_TEXT);
            assertEquals(HttpStatus.CREATED, responseOpenText.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }
    }

    // -----------------------
    // SingleChoice tests
    // -----------------------

    @Nested
    class SingleChoiceTests {
        @Test
        void createNewTask__singleChoice_should_return_bad_request_when_under_2_options() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("Java", true)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__singleChoice_should_create_when_2_options() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void createNewTask__singleChoice_should_return_bad_request_when_over_5_options() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", false),
                    new OptionDTO("Ruby", false),
                    new OptionDTO("JavaScript", false),
                    new OptionDTO("COBOL", false),
                    new OptionDTO("Visual Basic", false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__singleChoice_should_create_when_5_options() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", false),
                    new OptionDTO("Ruby", false),
                    new OptionDTO("JavaScript", false),
                    new OptionDTO("COBOL", false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void createNewTask__singleChoice_should_return_bad_request_when_all_options_incorrect() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("Java", false),
                    new OptionDTO("Python", false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__singleChoice_should_return_bad_request_when_all_options_correct() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", true)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__singleChoice_should_return_bad_request_when_option_length_under_4_chars() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("C++", false),
                    new OptionDTO("Bash", false),
                    new OptionDTO("Perl", false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__singleChoice_should_create_when_option_length_4_chars() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Curl", false),
                    new OptionDTO("Bash", false),
                    new OptionDTO("Perl", false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void createNewTask__singleChoice_should_return_bad_request_when_option_length_over_80_chars() {
            String longOption1 = "x".repeat(80);
            String longOption2 = "y".repeat(81);

            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO(longOption1, true),
                    new OptionDTO(longOption2, false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__singleChoice_should_create_when_option_length_80_chars() {
            String longOption1 = "x".repeat(80);
            String longOption2 = "y".repeat(80);

            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO(longOption1, true),
                    new OptionDTO(longOption2, false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void createNewTask__singleChoice_should_return_bad_request_when_options_duplicate() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("java", false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__singleChoice_should_return_bad_request_when_option_equals_statement() {
            sampleSingleChoiceDTO.setStatement("Java");

            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("java", true),
                    new OptionDTO("Python", false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__singleChoice_should_return_bad_request_when_options_null() {
            List<OptionDTO> singleChoiceOptions = List.of(
                    new OptionDTO("java", true),
                    new OptionDTO("Python", false),
                    new OptionDTO("Ruby", false),
                    new OptionDTO("JavaScript", false),
                    new OptionDTO(null, false)
            );

            sampleSingleChoiceDTO.setOptions(singleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__singleChoice_should_create_when_valid() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleSingleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleSingleChoiceDTO, Type.SINGLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }
    }

    // -----------------------
    // MultipleChoice tests
    // -----------------------

    @Nested
    class MultipleChoiceTests {
        @Test
        void createNewTask__multipleChoice_should_return_bad_request_when_under_3_options() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", true)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__multipleChoice_should_create_when_3_options() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", true),
                    new OptionDTO("Ruby", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void createNewTask__multipleChoice_should_return_bad_request_when_over_5_options() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", true),
                    new OptionDTO("Ruby", true),
                    new OptionDTO("JavaScript", true),
                    new OptionDTO("COBOL", false),
                    new OptionDTO("Visual Basic", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__multipleChoice_should_create_when_5_options() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", true),
                    new OptionDTO("Ruby", true),
                    new OptionDTO("JavaScript", false),
                    new OptionDTO("COBOL", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void createNewTask__multipleChoice_should_return_bad_request_when_under_2_options_correct() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", false),
                    new OptionDTO("Ruby", false),
                    new OptionDTO("JavaScript", false),
                    new OptionDTO("COBOL", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__multipleChoice_should_create_when_2_options_correct() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", true),
                    new OptionDTO("Ruby", false),
                    new OptionDTO("JavaScript", false),
                    new OptionDTO("COBOL", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void createNewTask__multipleChoice_should_return_bad_request_when_no_incorrect_option() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", true),
                    new OptionDTO("Ruby", true),
                    new OptionDTO("JavaScript", true),
                    new OptionDTO("COBOL", true)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__multipleChoice_should_create_when_1_option_incorrect() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Python", true),
                    new OptionDTO("Ruby", true),
                    new OptionDTO("JavaScript", true),
                    new OptionDTO("COBOL", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }


        @Test
        void createNewTask__multipleChoice_should_return_bad_request_when_option_length_under_4_chars() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("C++", true),
                    new OptionDTO("Bash", false),
                    new OptionDTO("Perl", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__multipleChoice_should_create_when_option_length_4_chars() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("Curl", true),
                    new OptionDTO("Bash", false),
                    new OptionDTO("Perl", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void createNewTask__multipleChoice_should_return_bad_request_when_option_length_over_80_chars() {
            String longOption1 = "w".repeat(80);
            String longOption2 = "x".repeat(81);
            String longOption3 = "y".repeat(80);
            String longOption4 = "z".repeat(80);

            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO(longOption1, true),
                    new OptionDTO(longOption2, true),
                    new OptionDTO(longOption3, false),
                    new OptionDTO(longOption4, false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__multipleChoice_should_create_when_option_length_80_chars() {
            String longOption1 = "w".repeat(80);
            String longOption2 = "x".repeat(80);
            String longOption3 = "y".repeat(80);
            String longOption4 = "z".repeat(80);

            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO(longOption1, true),
                    new OptionDTO(longOption2, true),
                    new OptionDTO(longOption3, false),
                    new OptionDTO(longOption4, false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void createNewTask__multipleChoice_should_return_bad_request_when_options_duplicate() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("Java", true),
                    new OptionDTO("java", true),
                    new OptionDTO("Ruby", false),
                    new OptionDTO("JavaScript", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__multipleChoice_should_return_bad_request_when_option_equals_statement() {
            sampleSingleChoiceDTO.setStatement("Java");

            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("java", true),
                    new OptionDTO("Python", false),
                    new OptionDTO("Ruby", false),
                    new OptionDTO("JavaScript", false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__multipleChoice_should_return_bad_request_when_options_null() {
            List<OptionDTO> multipleChoiceOptions = List.of(
                    new OptionDTO("java", true),
                    new OptionDTO("Python", true),
                    new OptionDTO("Ruby", false),
                    new OptionDTO("JavaScript", false),
                    new OptionDTO(null, false)
            );

            sampleMultipleChoiceDTO.setOptions(multipleChoiceOptions);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("options", ((ErrorItemDTO) resp.getBody()).getField());

            verify(taskRepository, never()).save(any());
        }

        @Test
        void createNewTask__multipleChoice_should_create_when_valid() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(taskRepository.countByCourseId(1L)).thenReturn(0);
            when(taskRepository.existsByCourseAndStatement(sampleCourse, sampleMultipleChoiceDTO.getStatement())).thenReturn(false);

            ResponseEntity<?> resp = taskService.createNewTask(sampleMultipleChoiceDTO, Type.MULTIPLE_CHOICE);
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());

            verify(taskRepository, times(1)).save(any(Task.class));
        }
    }
}

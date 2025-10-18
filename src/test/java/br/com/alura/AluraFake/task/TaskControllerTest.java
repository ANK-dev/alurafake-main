package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.NewMultipleChoiceDTO;
import br.com.alura.AluraFake.task.dto.NewOpenTextDTO;
import br.com.alura.AluraFake.task.dto.NewSingleChoiceDTO;
import br.com.alura.AluraFake.task.dto.OptionDTO;
import br.com.alura.AluraFake.util.ErrorItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TASK_NEW_OPEN_TEXT_ENDPOINT = "/task/new/opentext";
    private static final String TASK_NEW_SINGLE_CHOICE_ENDPOINT = "/task/new/singlechoice";
    private static final String TASK_NEW_MULTIPLE_CHOICE_ENDPOINT = "/task/new/multiplechoice";

    private NewOpenTextDTO sampleOpenTextDTO;
    private NewSingleChoiceDTO sampleSingleChoiceDTO;
    private NewMultipleChoiceDTO sampleMultipleChoiceDTO;

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
    }

    @Test
    void createNewTask__should_return_bad_request_when_statement_under_4_chars() throws Exception {
        String shortStatement = "abc";
        sampleOpenTextDTO.setStatement(shortStatement);
        sampleSingleChoiceDTO.setStatement(shortStatement);
        sampleMultipleChoiceDTO.setStatement(shortStatement);

        mockMvc.perform(post(TASK_NEW_OPEN_TEXT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOpenTextDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));

        mockMvc.perform(post(TASK_NEW_SINGLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleSingleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));

        mockMvc.perform(post(TASK_NEW_MULTIPLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMultipleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    @Test
    void createNewTask__should_return_bad_request_when_statement_over_255_chars() throws Exception {
        String longStatement = "x".repeat(256); // 256 chars (max = 255)
        sampleOpenTextDTO.setStatement(longStatement);
        sampleSingleChoiceDTO.setStatement(longStatement);
        sampleMultipleChoiceDTO.setStatement(longStatement);

        mockMvc.perform(post(TASK_NEW_OPEN_TEXT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOpenTextDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));

        mockMvc.perform(post(TASK_NEW_SINGLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleSingleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));

        mockMvc.perform(post(TASK_NEW_MULTIPLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMultipleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    @Test
    void createNewTask__should_return_bad_request_when_statement_duplicate() throws Exception {
        String statement = "Duplicate statement";
        sampleOpenTextDTO.setStatement(statement);
        sampleSingleChoiceDTO.setStatement(statement);
        sampleMultipleChoiceDTO.setStatement(statement);

        when(taskService.createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT)))
                .thenReturn(ResponseEntity.badRequest().build());
        when(taskService.createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE)))
                .thenReturn(ResponseEntity.badRequest().build());
        when(taskService.createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE)))
                .thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(post(TASK_NEW_OPEN_TEXT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOpenTextDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(1)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));

        mockMvc.perform(post(TASK_NEW_SINGLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleSingleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(1)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));

        mockMvc.perform(post(TASK_NEW_MULTIPLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMultipleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(1)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    @Test
    void createNewTask__should_return_bad_request_when_order_less_than_1() throws Exception {
        Integer order = 0;
        sampleOpenTextDTO.setOrder(order);
        sampleSingleChoiceDTO.setOrder(order);
        sampleMultipleChoiceDTO.setOrder(order);

        mockMvc.perform(post(TASK_NEW_OPEN_TEXT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOpenTextDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));

        mockMvc.perform(post(TASK_NEW_SINGLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleSingleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));

        mockMvc.perform(post(TASK_NEW_MULTIPLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMultipleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void createNewTask__should_return_bad_request_when_course_published() throws Exception {
        String field = "status";
        String message = "Course must be in BUILDING status to receive tasks";

        when(taskService.createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT)))
                .thenReturn((ResponseEntity) ResponseEntity.badRequest().body(new ErrorItemDTO(field, message)));
        when(taskService.createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE)))
                .thenReturn((ResponseEntity) ResponseEntity.badRequest().body(new ErrorItemDTO(field, message)));
        when(taskService.createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE)))
                .thenReturn((ResponseEntity) ResponseEntity.badRequest().body(new ErrorItemDTO(field, message)));

        mockMvc.perform(post(TASK_NEW_OPEN_TEXT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOpenTextDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value(field));

        verify(taskService, times(1)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));

        mockMvc.perform(post(TASK_NEW_SINGLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleSingleChoiceDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value(field));

        verify(taskService, times(1)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));

        mockMvc.perform(post(TASK_NEW_MULTIPLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMultipleChoiceDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value(field));

        verify(taskService, times(1)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    @Test
    void createNewTask__should_return_bad_request_when_fields_are_null_or_blank() throws Exception {
        sampleOpenTextDTO.setCourseId(null);
        sampleOpenTextDTO.setStatement("");
        sampleOpenTextDTO.setOrder(null);

        sampleSingleChoiceDTO.setCourseId(null);
        sampleSingleChoiceDTO.setStatement("");
        sampleSingleChoiceDTO.setOrder(null);

        sampleMultipleChoiceDTO.setCourseId(null);
        sampleMultipleChoiceDTO.setStatement("");
        sampleMultipleChoiceDTO.setOrder(null);

        mockMvc.perform(post(TASK_NEW_OPEN_TEXT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOpenTextDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));

        mockMvc.perform(post(TASK_NEW_SINGLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleSingleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));

        mockMvc.perform(post(TASK_NEW_MULTIPLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMultipleChoiceDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, times(0)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    @Test
    void createNewTask__should_create_when_valid() throws Exception {
        when(taskService.createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT)))
                .thenReturn(ResponseEntity.status(201).build());
        when(taskService.createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE)))
                .thenReturn(ResponseEntity.status(201).build());
        when(taskService.createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE)))
                .thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post(TASK_NEW_OPEN_TEXT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOpenTextDTO)))
                .andExpect(status().isCreated());

        verify(taskService, times(1)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));

        mockMvc.perform(post(TASK_NEW_SINGLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleSingleChoiceDTO)))
                .andExpect(status().isCreated());

        verify(taskService, times(1)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));

        mockMvc.perform(post(TASK_NEW_MULTIPLE_CHOICE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMultipleChoiceDTO)))
                .andExpect(status().isCreated());

        verify(taskService, times(1)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }
}

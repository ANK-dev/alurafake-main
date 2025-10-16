package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.NewMultipleChoiceDTO;
import br.com.alura.AluraFake.task.dto.NewOpenTextDTO;
import br.com.alura.AluraFake.task.dto.NewSingleChoiceDTO;
import br.com.alura.AluraFake.task.dto.OptionDTO;
import br.com.alura.AluraFake.util.ErrorItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    // -----------------------
    // OpenText tests
    // -----------------------

    @Test
    void createNewTask__openText_should_return_not_found_when_course_missing() throws Exception {
        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(999L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);

        when(taskService.createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT)))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));
    }

    @Test
    void createNewTask__openText_should_return_bad_request_when_course_not_building() throws Exception {
        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);

        when(taskService.createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT)))
                .thenReturn((ResponseEntity) ResponseEntity.badRequest().body(new ErrorItemDTO("courseId", "Course must be in BUILDING status to receive tasks")));

        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("courseId"));

        verify(taskService, times(1)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));
    }

    @Test
    void createNewTask__openText_should_create_task_when_valid() throws Exception {
        NewOpenTextDTO dto = new NewOpenTextDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);

        when(taskService.createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT)))
                .thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(taskService, times(1)).createNewTask(any(NewOpenTextDTO.class), eq(Type.OPEN_TEXT));
    }

    // -----------------------
    // SingleChoice tests
    // -----------------------

    @Test
    void createNewTask__singleChoice_should_return_not_found_when_course_missing() throws Exception {
        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(99L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(List.of(
                createOption("Java", true),
                createOption("Python", false)
        ));

        when(taskService.createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE)))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));
    }

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_order_sequence_invalid() throws Exception {
        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(2);
        dto.setOptions(List.of(
                createOption("Java", true),
                createOption("Python", false)
        ));

        when(taskService.createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE)))
                .thenReturn((ResponseEntity) ResponseEntity.badRequest().body(new ErrorItemDTO("order", "Invalid order sequence")));

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("order"));

        verify(taskService, times(1)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));
    }

    @Test
    void createNewTask__singleChoice_should_return_bad_request_when_options_invalid_size() throws Exception {
        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(List.of(createOption("Java", true))); // only 1 option

        when(taskService.createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE)))
                .thenReturn((ResponseEntity) ResponseEntity.badRequest().body(new ErrorItemDTO("options", "Single choice must have between 2 and 5 options")));

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("options"));

        verify(taskService, times(1)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));
    }

    @Test
    void createNewTask__singleChoice_should_create_task_when_valid() throws Exception {
        NewSingleChoiceDTO dto = new NewSingleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                createOption("Java", true),
                createOption("Python", false),
                createOption("Ruby", false)
        ));

        when(taskService.createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE)))
                .thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(taskService, times(1)).createNewTask(any(NewSingleChoiceDTO.class), eq(Type.SINGLE_CHOICE));
    }

    // -----------------------
    // MultipleChoice tests
    // -----------------------

    @Test
    void createNewTask__multipleChoice_should_return_not_found_when_course_missing() throws Exception {
        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(999L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                createOption("Java", true),
                createOption("Spring", true),
                createOption("Ruby", false)
        ));

        when(taskService.createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE)))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(post("/task/new/multiplechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    @Test
    void createNewTask__multipleChoice_should_return_bad_request_when_options_count_invalid() throws Exception {
        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(List.of(
                createOption("Java", true),
                createOption("Python", true) // only 2 options (needs min 3)
        ));

        when(taskService.createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE)))
                .thenReturn((ResponseEntity) ResponseEntity.badRequest().body(new ErrorItemDTO("options", "Multiple choice must have between 3 and 5 options")));

        mockMvc.perform(post("/task/new/multiplechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("options"));

        verify(taskService, times(1)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    @Test
    void createNewTask__multipleChoice_should_return_bad_request_when_correctness_rules_violate() throws Exception {
        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                createOption("Java", true),
                createOption("Spring", true),
                createOption("Kotlin", true)
        ));

        when(taskService.createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE)))
                .thenReturn((ResponseEntity) ResponseEntity.badRequest().body(new ErrorItemDTO("options", "Multiple choice must have two or more correct options and at least one incorrect option")));

        mockMvc.perform(post("/task/new/multiplechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("options"));

        verify(taskService, times(1)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    @Test
    void createNewTask__multipleChoice_should_create_task_when_valid() throws Exception {
        NewMultipleChoiceDTO dto = new NewMultipleChoiceDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos hoje?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                createOption("Java", true),
                createOption("Spring", true),
                createOption("Ruby", false)
        ));

        when(taskService.createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE)))
                .thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/task/new/multiplechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(taskService, times(1)).createNewTask(any(NewMultipleChoiceDTO.class), eq(Type.MULTIPLE_CHOICE));
    }

    // helper
    private OptionDTO createOption(String text, boolean isCorrect) {
        OptionDTO o = new OptionDTO();
        o.setOption(text);
        o.setIsCorrect(isCorrect);
        return o;
    }
}
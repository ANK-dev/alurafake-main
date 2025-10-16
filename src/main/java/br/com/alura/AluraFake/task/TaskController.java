package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.NewMultipleChoiceDTO;
import br.com.alura.AluraFake.task.dto.NewOpenTextDTO;
import br.com.alura.AluraFake.task.dto.NewSingleChoiceDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/task/new/opentext")
    public ResponseEntity<?> newOpenTextExercise(@Valid @RequestBody NewOpenTextDTO dto) {
        return taskService.createNewTask(dto, Type.OPEN_TEXT);
    }

    @PostMapping("/task/new/singlechoice")
    public ResponseEntity<?> newSingleChoice(@Valid @RequestBody NewSingleChoiceDTO dto) {
        return taskService.createNewTask(dto, Type.SINGLE_CHOICE);
    }

    @PostMapping("/task/new/multiplechoice")
    public ResponseEntity<?> newMultipleChoice(@Valid @RequestBody NewMultipleChoiceDTO dto) {
        return taskService.createNewTask(dto, Type.MULTIPLE_CHOICE);
    }
}
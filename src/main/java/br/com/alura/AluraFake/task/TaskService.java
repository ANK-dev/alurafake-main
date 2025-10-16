package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.*;
import br.com.alura.AluraFake.util.ErrorItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, CourseRepository courseRepository) {
        this.taskRepository = taskRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public <T extends NewTaskDTO> ResponseEntity<?> createNewTask(T dto, Type type) {
        ResponseEntity<?> fieldsNullErr = validateFieldsNotNull(dto, type);
        if (fieldsNullErr != null) {
            return fieldsNullErr;
        }

        Optional<Course> courseOptional = courseRepository.findById(dto.getCourseId());
        if (courseOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Course course = courseOptional.get();

        ResponseEntity<?> courseBuildingErr = validateCourseIsBuilding(course);
        if (courseBuildingErr != null) {
            return courseBuildingErr;
        }

        String statementTrimmed = dto.getStatement().trim();

        ResponseEntity<?> statementLengthErr = validateStatementLength(statementTrimmed);
        if (statementLengthErr != null) {
            return statementLengthErr;
        }

        ResponseEntity<?> statementUniqueErr = validateStatementUnique(course, statementTrimmed);
        if (statementUniqueErr != null) {
            return statementUniqueErr;
        }

        int existingCount = taskRepository.countByCourseId(dto.getCourseId());
        ResponseEntity<?> orderErr = validateOrderSequence(existingCount, dto.getOrder());
        if (orderErr != null) {
            return orderErr;
        }

        if (Type.SINGLE_CHOICE.equals(type) || Type.MULTIPLE_CHOICE.equals(type)) {
            ResponseEntity<?> optionsErr = validateOptions(dto.getOptions(), statementTrimmed, type);
            if (optionsErr != null) {
                return optionsErr;
            }
        }

        if (dto.getOrder() <= existingCount) {
            taskRepository.shiftOrders(dto.getCourseId(), dto.getOrder());
        }

        Task task = new Task(course, statementTrimmed, dto.getOrder(), type);
        for (OptionDTO o : dto.getOptions()) {
            task.addOption(new TaskOption(o.getOption().trim(), Boolean.TRUE.equals(o.getIsCorrect())));
        }
        taskRepository.save(task);

        return ResponseEntity.status(201).build();
    }

    // ----------
    // Validation
    // ----------

    private <T extends NewTaskDTO> ResponseEntity<?> validateFieldsNotNull(T dto, Type type) {
        List<ErrorItemDTO> nullFields = new ArrayList<>();

        if (dto.getCourseId() == null) {
            nullFields.add(new ErrorItemDTO("courseId", "courseId is required"));
        }
        if (dto.getStatement() == null) {
            nullFields.add(new ErrorItemDTO("statement", "statement is required"));
        }
        if (dto.getOptions() == null && (Type.SINGLE_CHOICE.equals(type) || Type.MULTIPLE_CHOICE.equals(type))) {
            nullFields.add(new ErrorItemDTO("options", "options is required"));
        }
        if (dto.getOrder() == null) {
            nullFields.add(new ErrorItemDTO("order", "order is required"));
        }

        if (!nullFields.isEmpty()) {
            if (nullFields.size() == 1) {
                return ResponseEntity.badRequest().body(nullFields.getFirst());
            }

            return ResponseEntity.badRequest().body(nullFields);
        }

        return null;
    }

    private ResponseEntity<?> validateCourseIsBuilding(Course course) {
        if (!Status.BUILDING.equals(course.getStatus())) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO(
                    "courseId",
                    "Course must be in BUILDING status to receive tasks"
            ));
        }
        return null;
    }

    private ResponseEntity<?> validateStatementLength(String statement) {
        if (statement.length() < 4 || statement.length() > 255) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO(
                    "statement",
                    "Statement must be between 4 and 255 characters"
            ));
        }
        return null;
    }

    private ResponseEntity<?> validateStatementUnique(Course course, String statement) {
        if (taskRepository.existsByCourseAndStatement(course, statement)) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO(
                    "statement",
                    "Statement already exists in this course"
            ));
        }
        return null;
    }

    private ResponseEntity<?> validateOrderSequence(int existingCount, Integer requestedOrder) {
        if (requestedOrder < 1) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO(
                    "order",
                    "Order must be a positive integer"
            ));
        }
        if (requestedOrder > existingCount + 1) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO(
                    "order",
                    "Invalid order sequence"
            ));
        }
        return null;
    }

    private ResponseEntity<?> validateOptions(List<OptionDTO> options, String statement, Type type) {
        boolean singleChoice = Type.SINGLE_CHOICE.equals(type);
        int min = singleChoice ? 2 : 3;
        int max = 5;
        if (options.size() < min || options.size() > max) {
            return ResponseEntity.badRequest().body(new ErrorItemDTO(
                    "options",
                    singleChoice
                            ? "Single choice must have between 2 and 5 options"
                            : "Multiple choice must have between 3 and 5 options"
            ));
        }

        int correctCount = 0;
        Set<String> optionSet = new HashSet<>();
        String statementLowercase = statement.toLowerCase();

        for (OptionDTO dto : options) {
            String option = dto.getOption();
            if (option == null) {
                return ResponseEntity.badRequest().body(new ErrorItemDTO(
                        "options",
                        "Option text must not be null"
                ));
            }
            String optionTrimmedLowercase = option.trim().toLowerCase();

            if (optionTrimmedLowercase.length() < 4 || optionTrimmedLowercase.length() > 80) {
                return ResponseEntity.badRequest().body(new ErrorItemDTO(
                        "options",
                        "Each option must have between 4 and 80 characters"
                ));
            }
            if (!optionSet.add(optionTrimmedLowercase)) {
                return ResponseEntity.badRequest().body(new ErrorItemDTO(
                        "options",
                        "Options must be unique"
                ));
            }
            if (optionTrimmedLowercase.equals(statementLowercase)) {
                return ResponseEntity.badRequest().body(new ErrorItemDTO(
                        "options",
                        "Options must not be equal to the statement"
                ));
            }
            if (Boolean.TRUE.equals(dto.getIsCorrect())) {
                correctCount++;
            }
        }

        if (singleChoice) {
            if (correctCount != 1) {
                return ResponseEntity.badRequest().body(new ErrorItemDTO(
                        "options",
                        "Single choice must have exactly one correct option"
                ));
            }
        } else {
            if (correctCount < 2 || correctCount >= options.size()) {
                return ResponseEntity.badRequest().body(new ErrorItemDTO(
                        "options",
                        "Multiple choice must have two or more correct options and at least one incorrect option"
                ));
            }
        }

        return null;
    }
}

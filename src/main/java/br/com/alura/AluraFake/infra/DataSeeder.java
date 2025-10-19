package br.com.alura.AluraFake.infra;

import br.com.alura.AluraFake.course.*;
import br.com.alura.AluraFake.task.Task;
import br.com.alura.AluraFake.task.TaskOption;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.task.Type;
import br.com.alura.AluraFake.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;

    public DataSeeder(UserRepository userRepository, CourseRepository courseRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) {
        if (!"dev".equals(activeProfile)) return;

        if (userRepository.count() == 0) {
            User caio = new User("Caio", "caio@alura.com.br", Role.STUDENT);
            User paulo = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);

            userRepository.saveAll(Arrays.asList(caio, paulo));

            Course javaCourse = new Course("Java", "Aprenda Java com Alura", paulo);
            Course springCourse = new Course("Spring Boot", "Desenvolva aplicações com Spring Boot", paulo);
            Course pythonCourse = new Course("Python", "Introdução ao Python", paulo);

            javaCourse.setStatus(Status.PUBLISHED);
            javaCourse.setPublishedAt(LocalDateTime.of(2021, 6, 15, 10, 0));   // 2021-06-15 10:00

            springCourse.setStatus(Status.PUBLISHED);
            springCourse.setPublishedAt(LocalDateTime.of(2023, 3, 20, 14, 30));  // 2023-03-20 14:30

            courseRepository.saveAll(Arrays.asList(javaCourse, springCourse, pythonCourse));

            Task javaOpen = new Task(javaCourse, "Descreva a lição de hoje", 1, Type.OPEN_TEXT);

            Task javaSingle = new Task(javaCourse, "Qual linguagem usamos para este curso?", 2, Type.SINGLE_CHOICE);
            javaSingle.addOption(new TaskOption("Java", true));
            javaSingle.addOption(new TaskOption("Python", false));

            Task javaMultiple = new Task(javaCourse, "Quais tecnologias fazem parte do ecossistema Java?", 3, Type.MULTIPLE_CHOICE);
            javaMultiple.addOption(new TaskOption("Spring", true));
            javaMultiple.addOption(new TaskOption("Hibernate", true));
            javaMultiple.addOption(new TaskOption("Django", false));

            Task springOpen = new Task(springCourse, "Explique o propósito do Spring Boot", 1, Type.OPEN_TEXT);

            Task springSingle = new Task(springCourse, "Qual anotação inicializa uma aplicação Spring Boot?", 2, Type.SINGLE_CHOICE);
            springSingle.addOption(new TaskOption("@SpringBootApplication", true));
            springSingle.addOption(new TaskOption("@Configuration", false));

            Task springMultiple = new Task(springCourse, "Quais módulos são comuns em uma aplicação Spring Boot?", 3, Type.MULTIPLE_CHOICE);
            springMultiple.addOption(new TaskOption("Spring MVC", true));
            springMultiple.addOption(new TaskOption("Spring Data JPA", true));
            springMultiple.addOption(new TaskOption("Flask", false));

            Task pythonOpen = new Task(pythonCourse, "O que é uma lista em Python?", 1, Type.OPEN_TEXT);

            Task pythonSingle = new Task(pythonCourse, "Qual é a extensão padrão de arquivos Python?", 2, Type.SINGLE_CHOICE);
            pythonSingle.addOption(new TaskOption(".py", true));
            pythonSingle.addOption(new TaskOption(".java", false));

            List<Task> allTasks = List.of(
                    javaOpen, javaSingle, javaMultiple,
                    springOpen, springSingle, springMultiple,
                    pythonOpen, pythonSingle
            );

            taskRepository.saveAll(allTasks);
        }
    }
}

package com.example.demo;

import com.example.demo.model.Subject;
import com.example.demo.model.Task;
import com.example.demo.repository.SubjectRepository;
import com.example.demo.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(TaskRepository taskRepository, SubjectRepository subjectRepository) {
        return args -> {
            if (subjectRepository.count() == 0) {
                subjectRepository.save(new Subject("Lập trình di động", "INT3110"));
                subjectRepository.save(new Subject("Kỹ thuật đồ họa", "INT3306"));
                subjectRepository.save(new Subject("Lập trình hướng đối tượng", "INT2204"));
                System.out.println(">> Đã nạp dữ liệu mẫu Subject!");
            }

            if (taskRepository.count() == 0) {
                taskRepository.save(new Task("Nộp báo cáo cuối kỳ Mobile", System.currentTimeMillis() + 86400000L, 1) {{
                    setPriority("high");
                    setCompleted(false);
                }});
                taskRepository.save(new Task("Học bài thi Kỹ thuật đồ họa", System.currentTimeMillis() + 172800000L, 2) {{
                    setPriority("high");
                    setCompleted(false);
                }});
                taskRepository.save(new Task("Làm bài tập Java", System.currentTimeMillis() - 86400000L, 3) {{
                    setPriority("medium");
                    setCompleted(false);
                }});
                System.out.println(">> Đã nạp dữ liệu mẫu Task thành công!");
            }
        };
    }
}

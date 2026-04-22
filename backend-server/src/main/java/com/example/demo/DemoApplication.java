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
	CommandLineRunner initDatabase(TaskRepository taskRepository, SubjectRepository subjectRepository) {
		return args -> {
			// Tạo môn mẫu
			Subject mobile = subjectRepository.save(new Subject("Môn Mobile", "INT3117"));
			Subject csdl = subjectRepository.save(new Subject("Môn CSDL phân tán", "INT3215"));

			// Tạo task cho môn Mobile
			taskRepository.save(new Task("Giới thiệu bài toán", 1741564800000L, mobile.getId().intValue())); // 10/3/2026
			taskRepository.save(new Task("Phác thảo figma", 1742428800000L, mobile.getId().intValue()));    // 20/3/2026
			taskRepository.save(new Task("Code front-end", 1744243200000L, mobile.getId().intValue()));     // 10/4/2026
			
			// Tạo task cho môn CSDL
			taskRepository.save(new Task("Thiết kế lược đồ", 1741996800000L, csdl.getId().intValue()));     // 15/3/2026
			taskRepository.save(new Task("Cài đặt cụm DB", 1742860800000L, csdl.getId().intValue()));       // 25/3/2026
		};
	}
}

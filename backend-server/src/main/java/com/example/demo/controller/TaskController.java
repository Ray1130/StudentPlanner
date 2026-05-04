package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskRepository.save(task);
    }

    @GetMapping("/date")
    public List<Task> getTasksByDate(@RequestParam Long start, @RequestParam Long end) {
        return taskRepository.findByDueDateBetween(start, end);
    }

    @GetMapping("/priority/high")
    public List<Task> getHighPriorityUncompletedTasks() {
        return taskRepository.findByPriorityAndIsCompleted("high", false);
    }

    @PostMapping("/update")
    public Task updateTask(@RequestBody Task task) {
        return taskRepository.save(task);
    }

    @PostMapping("/delete")
    public void deleteTask(@RequestBody Integer id) {
        taskRepository.deleteById(id.longValue());
    }
}

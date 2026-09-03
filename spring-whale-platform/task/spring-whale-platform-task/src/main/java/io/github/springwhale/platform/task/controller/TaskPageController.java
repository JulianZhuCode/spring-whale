package io.github.springwhale.platform.task.controller;

import io.github.springwhale.database.SortUtils;
import io.github.springwhale.framework.thymeleaf.controller.AdminPage;
import io.github.springwhale.platform.task.enums.TaskStatus;
import io.github.springwhale.platform.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@AdminPage
@Controller
@RequestMapping("/admin/task")
@RequiredArgsConstructor
public class TaskPageController {

    private final TaskService taskService;

    @GetMapping({"", "/", "/batch"})
    public String batch(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String taskType,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String sort,
                        Model model) {

        Sort sortObj = SortUtils.buildSort(sort);
        Page<?> taskPage;

        if (taskType != null && !taskType.isBlank()) {
            taskPage = taskService.findByTaskType(taskType, page, size, sort);
        } else if (status != null && !status.isBlank()) {
            TaskStatus taskStatus = parseStatus(status);
            if (taskStatus != null) {
                taskPage = taskService.findByStatus(taskStatus, page, size, sort);
            } else {
                taskPage = taskService.findAll(page, size, sort);
            }
        } else {
            taskPage = taskService.findAll(page, size, sort);
        }

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("page", taskPage);
        model.addAttribute("taskType", taskType);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("sortField", SortUtils.getSortField(sortObj));
        model.addAttribute("sortDirection", SortUtils.getSortDirection(sortObj));

        return "admin/task/batch";
    }

    private TaskStatus parseStatus(String status) {
        try {
            return TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
package io.github.springwhale.platform.task.ui.menu;

import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registers task management menu items in the admin console sidebar.
 */
@Component
public class TaskMenuProvider implements AdminMenuProvider {

    @Override
    public List<MenuItem> getMenus() {
        return List.of(
                MenuItem.group("task", "Tasks", "menu.task", "⚙️", 20),
                MenuItem.leaf("task-batch", "task", "Batch Tasks", "menu.task.batch", "/admin/task/batch", null, "task:create", 1)
        );
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
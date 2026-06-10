package io.github.springwhale.framework.thymeleaf.controller;

import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Comparator;
import java.util.List;

/**
 * Controller advice that injects the sidebar menu tree and current path
 * into every admin page model, so layout fragments can access them.
 */
@ControllerAdvice(annotations = AdminPage.class)
@RequiredArgsConstructor
public class AdminControllerAdvice {

    private final List<AdminMenuProvider> menuProviders;

    @ModelAttribute("menuGroups")
    public List<MenuItem.MenuGroup> menuGroups() {
        List<MenuItem> allItems = menuProviders.stream()
                .sorted(Comparator.comparingInt(AdminMenuProvider::getOrder))
                .flatMap(p -> p.getMenus().stream())
                .toList();
        return MenuItem.buildTree(allItems);
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}

package io.github.springwhale.framework.thymeleaf.controller;

import io.github.springwhale.framework.thymeleaf.config.AdminProperties;
import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller advice that injects the sidebar menu tree and current path
 * into every admin page model, so layout fragments can access them.
 * <p>
 * Menu items with a {@link MenuItem#getPermission() permission} are only
 * visible to users who hold that authority or the {@code *} wildcard.
 * </p>
 */
@ControllerAdvice(annotations = AdminPage.class)
@RequiredArgsConstructor
public class AdminControllerAdvice {

    private static final String WILDCARD = "*";

    private final List<AdminMenuProvider> menuProviders;
    private final AdminProperties adminProperties;

    /**
     * Extracts authority strings from the current SecurityContext.
     * Returns an empty set if not authenticated.
     */
    private static Set<String> getCurrentAuthorities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Set.of();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    /**
     * A menu item is visible when:
     * <ul>
     *   <li>It has no permission requirement, OR</li>
     *   <li>The user holds the wildcard ({@code *}), OR</li>
     *   <li>The user holds the specific permission</li>
     * </ul>
     */
    private static boolean isVisible(MenuItem item, Set<String> authorities) {
        String permission = item.getPermission();
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        return authorities.contains(WILDCARD) || authorities.contains(permission);
    }

    @ModelAttribute("menuGroups")
    public List<MenuItem.MenuGroup> menuGroups() {
        Set<String> authorities = getCurrentAuthorities();

        List<MenuItem> allItems = menuProviders.stream()
                .sorted(Comparator.comparingInt(AdminMenuProvider::getOrder))
                .flatMap(p -> p.getMenus().stream())
                .filter(item -> isVisible(item, authorities))
                .toList();
        return MenuItem.buildTree(allItems);
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * Exposes current user authorities to templates so they can
     * conditionally show/hide buttons and actions.
     */
    @ModelAttribute("userAuthorities")
    public Set<String> userAuthorities() {
        return getCurrentAuthorities();
    }

    /**
     * Exposes admin properties to templates so they can access
     * configurable brand name, short name, and copyright.
     */
    @ModelAttribute("adminProps")
    public AdminProperties adminProps() {
        return adminProperties;
    }
}

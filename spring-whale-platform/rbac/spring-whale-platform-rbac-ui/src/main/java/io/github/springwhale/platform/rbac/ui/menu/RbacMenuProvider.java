package io.github.springwhale.platform.rbac.ui.menu;

import io.github.springwhale.framework.core.utils.AuthUtil;
import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import io.github.springwhale.platform.rbac.constant.RbacConstants;
import io.github.springwhale.platform.rbac.dao.entity.MenuEntity;
import io.github.springwhale.platform.rbac.dao.entity.RoleEntity;
import io.github.springwhale.platform.rbac.dao.entity.RoleMenuEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserRoleEntity;
import io.github.springwhale.platform.rbac.dao.repository.MenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleMenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleRepository;
import io.github.springwhale.platform.rbac.enums.MenuType;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registers RBAC module menu items in the admin console sidebar.
 * <p>
 * Menus are loaded from the database ({@code rbac_menu} table) and filtered
 * by the current user's role-based permissions.
 * Only visible and enabled menus of type {@code DIRECTORY} or {@code MENU}
 * are included. BUTTON-type permissions are excluded from the sidebar.
 * </p>
 * <p>
 * Each leaf menu item carries a {@code permission} matching the
 * corresponding menu code so that the sidebar only shows entries
 * the current user is authorized to access.
 * </p>
 */
@RequiredArgsConstructor
public class RbacMenuProvider implements AdminMenuProvider {

    private final MenuRepository menuRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;

    @Override
    public List<MenuItem> getMenus() {
        Integer userId = AuthUtil.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }

        List<MenuEntity> menus = resolveAllowedMenus(userId);
        return toMenuItems(menus);
    }

    @Override
    public int getOrder() {
        return 10;
    }

    private List<MenuEntity> resolveAllowedMenus(Integer userId) {
        List<Integer> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        List<RoleEntity> roles = roleRepository.findAllById(roleIds);
        boolean isSuperAdmin = roles.stream().anyMatch(
                r -> r.getStatus() == 1 && RbacConstants.SUPER_ADMIN_ROLE_CODE.equals(r.getCode()));

        if (isSuperAdmin) {
            return fetchVisibleMenus(null);
        }

        Set<Integer> allowedMenuIds = roleMenuRepository.findByRoleIdIn(roleIds).stream()
                .map(RoleMenuEntity::getMenuId)
                .collect(Collectors.toCollection(HashSet::new));

        return fetchVisibleMenus(allowedMenuIds);
    }

    private List<MenuEntity> fetchVisibleMenus(Set<Integer> allowedMenuIds) {
        List<MenuEntity> source = allowedMenuIds == null
                ? menuRepository.findAll()
                : menuRepository.findAllById(allowedMenuIds);

        return source.stream()
                .filter(m -> m.getVisible() == 1 && m.getStatus() == 1)
                .filter(m -> m.getType() == MenuType.DIRECTORY || m.getType() == MenuType.MENU)
                .sorted(Comparator.comparing(MenuEntity::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<MenuItem> toMenuItems(List<MenuEntity> entities) {
        Map<Integer, String> idToCode = entities.stream()
                .collect(Collectors.toMap(MenuEntity::getId, MenuEntity::getCode));

        return entities.stream()
                .map(entity -> toMenuItem(entity, idToCode))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private MenuItem toMenuItem(MenuEntity entity, Map<Integer, String> idToCode) {
        int sort = entity.getSort() != null ? entity.getSort() : 0;
        if (entity.getParentId() == null) {
            return MenuItem.group(
                    entity.getCode(),
                    entity.getName(),
                    entity.getNameI18nKey(),
                    entity.getIcon(),
                    sort);
        }
        String parentCode = idToCode.get(entity.getParentId());
        if (parentCode == null) {
            return null;
        }
        return MenuItem.leaf(
                entity.getCode(),
                parentCode,
                entity.getName(),
                entity.getNameI18nKey(),
                entity.getPath(),
                entity.getIcon(),
                entity.getPermission(),
                sort);
    }
}
package io.github.springwhale.thymeleaf.test.menu;

import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuItemTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("group() should create a group with null parentKey and url")
        void group() {
            MenuItem item = MenuItem.group("rbac", "RBAC", "shield", 10);

            assertTrue(item.isGroup());
            assertFalse(item.isLeaf());
            assertEquals("rbac", item.getKey());
            assertNull(item.getParentKey());
            assertNull(item.getUrl());
            assertEquals("shield", item.getIcon());
            assertEquals(10, item.getSort());
        }

        @Test
        @DisplayName("group() with i18n should set labelI18nKey")
        void groupWithI18n() {
            MenuItem item = MenuItem.group("rbac", "RBAC", "menu.rbac", "shield", 10);

            assertEquals("menu.rbac", item.getLabelI18nKey());
        }

        @Test
        @DisplayName("leaf() should create a leaf with parentKey and url")
        void leaf() {
            MenuItem item = MenuItem.leaf("rbac-users", "rbac", "Users", "/admin/rbac/users", 1);

            assertTrue(item.isLeaf());
            assertFalse(item.isGroup());
            assertEquals("rbac-users", item.getKey());
            assertEquals("rbac", item.getParentKey());
            assertEquals("/admin/rbac/users", item.getUrl());
            assertNull(item.getPermission());
            assertNull(item.getIcon());
        }

        @Test
        @DisplayName("leaf() with icon should set icon")
        void leafWithIcon() {
            MenuItem item = MenuItem.leaf("rbac-users", "rbac", "Users",
                    "/admin/rbac/users", "people", 1);

            assertEquals("people", item.getIcon());
        }

        @Test
        @DisplayName("leaf() with permission should set permission")
        void leafWithPermission() {
            MenuItem item = MenuItem.leaf("rbac-users", "rbac", "Users",
                    "/admin/rbac/users", "people", "rbac:user:read", 1);

            assertEquals("rbac:user:read", item.getPermission());
        }

        @Test
        @DisplayName("leaf() with i18n should set all fields")
        void leafWithI18n() {
            MenuItem item = MenuItem.leaf("rbac-users", "rbac", "Users",
                    "menu.rbac.users", "/admin/rbac/users", "people", "rbac:user:read", 1);

            assertEquals("menu.rbac.users", item.getLabelI18nKey());
            assertEquals("people", item.getIcon());
            assertEquals("rbac:user:read", item.getPermission());
        }
    }

    @Nested
    @DisplayName("buildTree()")
    class BuildTree {

        @Test
        @DisplayName("should build a single group with children")
        void singleGroupWithChildren() {
            List<MenuItem> items = List.of(
                    MenuItem.group("rbac", "RBAC", "shield", 10),
                    MenuItem.leaf("rbac-users", "rbac", "Users", "/admin/rbac/users", 1));

            List<MenuItem.MenuGroup> groups = MenuItem.buildTree(items);

            assertEquals(1, groups.size());
            assertEquals("rbac", groups.get(0).getKey());
            assertEquals(1, groups.get(0).getChildren().size());
            assertEquals("rbac-users", groups.get(0).getChildren().get(0).getKey());
        }

        @Test
        @DisplayName("should sort groups by sort order")
        void sortGroups() {
            List<MenuItem> items = List.of(
                    MenuItem.group("z", "Last", "gear", 99),
                    MenuItem.group("a", "First", "house", 1));

            List<MenuItem.MenuGroup> groups = MenuItem.buildTree(items);

            assertEquals("a", groups.get(0).getKey());
            assertEquals("z", groups.get(1).getKey());
        }

        @Test
        @DisplayName("should sort children within groups")
        void sortChildren() {
            List<MenuItem> items = List.of(
                    MenuItem.group("rbac", "RBAC", "shield", 10),
                    MenuItem.leaf("rbac-roles", "rbac", "Roles", "/admin/rbac/roles", 2),
                    MenuItem.leaf("rbac-users", "rbac", "Users", "/admin/rbac/users", 1));

            List<MenuItem.MenuGroup> groups = MenuItem.buildTree(items);

            assertEquals("rbac-users", groups.get(0).getChildren().get(0).getKey());
            assertEquals("rbac-roles", groups.get(0).getChildren().get(1).getKey());
        }

        @Test
        @DisplayName("should handle orphan leaf with no matching group")
        void orphanLeaf() {
            List<MenuItem> items = List.of(
                    MenuItem.leaf("orphan", "nonexistent", "Orphan", "/orphan", 1));

            List<MenuItem.MenuGroup> groups = MenuItem.buildTree(items);

            assertTrue(groups.isEmpty());
        }

        @Test
        @DisplayName("should handle multiple groups with children")
        void multipleGroups() {
            List<MenuItem> items = List.of(
                    MenuItem.group("rbac", "RBAC", "shield", 10),
                    MenuItem.leaf("rbac-users", "rbac", "Users", "/admin/rbac/users", 1),
                    MenuItem.group("task", "Tasks", "list-check", 20),
                    MenuItem.leaf("task-list", "task", "Task List", "/admin/task", 1));

            List<MenuItem.MenuGroup> groups = MenuItem.buildTree(items);

            assertEquals(2, groups.size());
            assertEquals("rbac", groups.get(0).getKey());
            assertEquals(1, groups.get(0).getChildren().size());
            assertEquals("task", groups.get(1).getKey());
            assertEquals(1, groups.get(1).getChildren().size());
        }

        @Test
        @DisplayName("should handle empty list")
        void emptyList() {
            List<MenuItem.MenuGroup> groups = MenuItem.buildTree(List.of());

            assertTrue(groups.isEmpty());
        }

        @Test
        @DisplayName("should handle group with no children")
        void groupWithNoChildren() {
            List<MenuItem> items = List.of(
                    MenuItem.group("empty", "Empty Group", "folder", 1));

            List<MenuItem.MenuGroup> groups = MenuItem.buildTree(items);

            assertEquals(1, groups.size());
            assertTrue(groups.get(0).getChildren().isEmpty());
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class Equality {

        @Test
        @DisplayName("should be equal when key and sort match")
        void equal() {
            MenuItem a = MenuItem.group("rbac", "RBAC", "shield", 10);
            MenuItem b = MenuItem.group("rbac", "Different", "gear", 10);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when key differs")
        void notEqualKey() {
            MenuItem a = MenuItem.group("rbac", "RBAC", "shield", 10);
            MenuItem b = MenuItem.group("task", "RBAC", "shield", 10);

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("should not be equal when sort differs")
        void notEqualSort() {
            MenuItem a = MenuItem.group("rbac", "RBAC", "shield", 10);
            MenuItem b = MenuItem.group("rbac", "RBAC", "shield", 20);

            assertNotEquals(a, b);
        }
    }
}
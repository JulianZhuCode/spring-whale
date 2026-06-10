package io.github.springwhale.framework.thymeleaf.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single menu item in the admin sidebar.
 */
public class MenuItem {

    private final String key;
    private final String parentKey;
    private final String label;
    private final String url;
    private final String icon;
    private final int sort;

    private MenuItem(String key, String parentKey, String label, String url, String icon, int sort) {
        this.key = key;
        this.parentKey = parentKey;
        this.label = label;
        this.url = url;
        this.icon = icon;
        this.sort = sort;
    }

    /**
     * Creates a top-level group menu item (has children, no URL).
     */
    public static MenuItem group(String key, String label, String icon, int sort) {
        return new MenuItem(key, null, label, null, icon, sort);
    }

    /**
     * Creates a leaf menu item (child of a group, has URL).
     */
    public static MenuItem leaf(String key, String parentKey, String label, String url, int sort) {
        return new MenuItem(key, parentKey, label, url, null, sort);
    }

    /**
     * Creates a leaf menu item with icon.
     */
    public static MenuItem leaf(String key, String parentKey, String label, String url, String icon, int sort) {
        return new MenuItem(key, parentKey, label, url, icon, sort);
    }

    // ---- Getters ----

    public String getKey() { return key; }
    public String getParentKey() { return parentKey; }
    public String getLabel() { return label; }
    public String getUrl() { return url; }
    public String getIcon() { return icon; }
    public int getSort() { return sort; }

    public boolean isGroup() {
        return parentKey == null;
    }

    public boolean isLeaf() {
        return parentKey != null;
    }

    // ---- Tree building ----

    /**
     * Builds a sorted menu tree from a flat list of items from all providers.
     * Returns only top-level groups with children populated.
     */
    public static List<MenuGroup> buildTree(List<MenuItem> allItems) {
        List<MenuItem> sorted = allItems.stream()
                .sorted(Comparator.comparingInt(MenuItem::getSort))
                .toList();

        List<MenuGroup> groups = new ArrayList<>();
        for (MenuItem item : sorted) {
            if (item.isGroup()) {
                MenuGroup group = new MenuGroup(item.getKey(), item.getLabel(), item.getIcon(), item.getSort());
                groups.add(group);
            }
        }

        for (MenuItem item : sorted) {
            if (item.isLeaf() && item.getParentKey() != null) {
                groups.stream()
                        .filter(g -> g.getKey().equals(item.getParentKey()))
                        .findFirst()
                        .ifPresent(g -> g.addChild(item));
            }
        }

        groups.sort(Comparator.comparingInt(MenuGroup::getSort));
        return groups;
    }

    /**
     * A group node in the menu tree, containing child menu items.
     */
    public static class MenuGroup {
        private final String key;
        private final String label;
        private final String icon;
        private final int sort;
        private final List<MenuItem> children = new ArrayList<>();

        MenuGroup(String key, String label, String icon, int sort) {
            this.key = key;
            this.label = label;
            this.icon = icon;
            this.sort = sort;
        }

        void addChild(MenuItem child) {
            children.add(child);
            children.sort(Comparator.comparingInt(MenuItem::getSort));
        }

        public String getKey() { return key; }
        public String getLabel() { return label; }
        public String getIcon() { return icon; }
        public int getSort() { return sort; }
        public List<MenuItem> getChildren() { return children; }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuItem menuItem)) return false;
        return sort == menuItem.sort && Objects.equals(key, menuItem.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, sort);
    }
}

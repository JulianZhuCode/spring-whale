package io.github.springwhale.database;

import org.springframework.data.domain.Sort;

public final class SortUtils {

    private SortUtils() {
    }

    public static Sort buildSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }

    public static String getSortField(Sort sort) {
        if (sort == null || sort.isEmpty()) {
            return "id";
        }
        return sort.get().findFirst().map(Sort.Order::getProperty).orElse("id");
    }

    public static String getSortDirection(Sort sort) {
        if (sort == null || sort.isEmpty()) {
            return "desc";
        }
        return sort.get().findFirst().map(o -> o.isAscending() ? "asc" : "desc").orElse("desc");
    }

}
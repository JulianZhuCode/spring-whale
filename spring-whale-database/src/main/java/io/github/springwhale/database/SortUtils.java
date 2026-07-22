package io.github.springwhale.database;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SortUtils {

    private SortUtils() {
    }

    public static Sort buildSort(String sort) {
        return buildSort(sort, Set.of());
    }

    public static Sort buildSort(String sort, Set<String> allowedFields) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        String[] parts = sort.split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (int i = 0; i < parts.length; i += 2) {
            String field = parts[i].trim();
            if (field.isEmpty()) {
                continue;
            }
            if (!allowedFields.isEmpty() && !allowedFields.contains(field)) {
                continue;
            }
            Sort.Direction direction = (i + 1 < parts.length && parts[i + 1].trim().equalsIgnoreCase("asc"))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            orders.add(new Sort.Order(direction, field));
        }
        if (orders.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        return Sort.by(orders);
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
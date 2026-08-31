package io.github.springwhale.database;

import io.github.springwhale.framework.core.utils.AuthUtil;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Simplified base entity with {@code id}, {@code createTime}, {@code createBy},
 * and optimistic locking ({@code @Version}).
 *
 * <p>Use when you don't need soft delete or update-time auditing.</p>
 */
@MappedSuperclass
@Data
public abstract class SimpleBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createTime;

    private Integer createBy;

    @Version
    private Integer version;

    @PrePersist
    public void prePersist() {
        this.createBy = AuthUtil.getUserId();
        this.createTime = LocalDateTime.now();
    }
}
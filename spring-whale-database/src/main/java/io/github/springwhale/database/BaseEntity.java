package io.github.springwhale.database;

import io.github.springwhale.framework.core.utils.AuthUtil;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Base JPA entity with audit fields, optimistic locking, and soft delete.
 *
 * <h3>Features</h3>
 * <ul>
 *   <li><b>Auto-audit</b> — {@code createBy}, {@code updateBy}, {@code createTime}, {@code updateTime}
 *       are populated automatically via {@code @PrePersist} / {@code @PreUpdate}</li>
 *   <li><b>Optimistic locking</b> — {@code @Version} on {@code version} field</li>
 *   <li><b>Soft delete</b> — {@code @SQLDelete} sets {@code delFlag = 1} instead of physical DELETE,
 *       {@code @SQLRestriction} filters out deleted records</li>
 * </ul>
 */
@Data
@MappedSuperclass
@SQLDelete(sql = "UPDATE #{entityName} SET del_flag = 1, update_time = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("del_flag = 0")
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer createBy;

    private Integer updateBy;

    @Version
    private Integer version;

    /**
     * 0 = normal, 1 = deleted
     */
    private Integer delFlag = 0;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.createBy = AuthUtil.getUserId();
        this.updateBy = AuthUtil.getUserId();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
        this.updateBy = AuthUtil.getUserId();
    }
}
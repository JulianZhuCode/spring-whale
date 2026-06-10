package io.github.springwhale.database;

import io.github.springwhale.framework.core.utils.AuthUtil;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
@SQLDelete(sql = "UPDATE #{entityName} SET del_flag = 1, update_time = now() WHERE id = ?")
@SQLRestriction("del_flag = 0")
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer createBy;

    private Integer updateBy;
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

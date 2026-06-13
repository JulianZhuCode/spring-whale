package io.github.springwhale.database;

import io.github.springwhale.framework.core.utils.AuthUtil;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class SimpleBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createTime;

    private Integer createBy;

    @PrePersist
    public void prePersist() {
        this.createBy = AuthUtil.getUserId();
        this.createTime = LocalDateTime.now();
    }
}
package io.github.springwhale.database.flyway.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "flyway_error_log")
@Data
public class FlywayErrorLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serverName;

    private LocalDateTime createTime;
    
    @Column(columnDefinition = "text")
    private String message;
}
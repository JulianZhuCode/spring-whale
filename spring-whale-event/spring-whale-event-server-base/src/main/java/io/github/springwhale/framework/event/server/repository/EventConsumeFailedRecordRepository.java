package io.github.springwhale.framework.event.server.repository;

import io.github.springwhale.framework.event.server.entity.EventConsumeFailedRecordEntity;
import io.github.springwhale.framework.event.server.enums.EventConsumeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventConsumeFailedRecordRepository extends JpaRepository<EventConsumeFailedRecordEntity, String>, JpaSpecificationExecutor<EventConsumeFailedRecordEntity> {

    List<EventConsumeFailedRecordEntity> findByStatus(EventConsumeStatus status);

    Page<EventConsumeFailedRecordEntity> findByStatus(EventConsumeStatus status, Pageable pageable);

    List<EventConsumeFailedRecordEntity> findByStatusAndNextRetryTimeBefore(EventConsumeStatus status, LocalDateTime time);

    Page<EventConsumeFailedRecordEntity> findByStatusAndNextRetryTimeBefore(EventConsumeStatus status, LocalDateTime time, Pageable pageable);

    boolean existsByMessageIdAndStatus(String messageId, EventConsumeStatus status);

    List<EventConsumeFailedRecordEntity> findByBusinessName(String businessName);

    List<EventConsumeFailedRecordEntity> findByListenerName(String listenerName);

    @Modifying
    @Query("UPDATE EventConsumeFailedRecordEntity e SET e.status = :status, e.nextRetryTime = :nextRetryTime, e.errorStack = :errorStack, e.updateTime = now() WHERE e.messageId = :messageId")
    int updateRetryResult(@Param("messageId") String messageId,
                          @Param("status") EventConsumeStatus status,
                          @Param("nextRetryTime") LocalDateTime nextRetryTime,
                          @Param("errorStack") String errorStack);

    @Modifying
    @Query("UPDATE EventConsumeFailedRecordEntity e SET e.retryCount = e.retryCount+1, e.status = :newStatus, e.updateTime = now() WHERE e.messageId = :messageId and e.status = :expectedStatus")
    int casTransitionStatus(@Param("messageId") String messageId,
                            @Param("expectedStatus") EventConsumeStatus expectedStatus,
                            @Param("newStatus") EventConsumeStatus newStatus);

}
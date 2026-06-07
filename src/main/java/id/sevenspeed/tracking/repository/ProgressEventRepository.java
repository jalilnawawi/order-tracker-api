package id.sevenspeed.tracking.repository;

import id.sevenspeed.tracking.entity.ProgressEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ProgressEventRepository extends Repository<ProgressEvent, Long> {
    ProgressEvent save(ProgressEvent event);
    Optional<ProgressEvent> findById(Long id);
    List<ProgressEvent> findByBatchIdOrderByPerformedAtAsc(Long batchId);
    Page<ProgressEvent> findByBatchId(Long batchId, Pageable pageable);

    // Untuk state machine validation di ProgressEventService
    Optional<ProgressEvent> findTopByBatchIdOrderByPerformedAtDesc(Long batchId);
    List<ProgressEvent> findByBatchIdAndWorkflowStepId(Long batchId, Long stepId);

    // STEP_STARTED terbaru untuk current step entry (performedAt >= enteredAt)
    @Query("""
            SELECT pe FROM ProgressEvent pe
            WHERE pe.batch.id = :batchId
            AND pe.workflowStep.id = :stepId
            AND pe.eventType = 'STEP_STARTED'
            AND pe.performedAt >= :enteredAt
            ORDER BY pe.performedAt DESC
            LIMIT 1
            """)
    Optional<ProgressEvent> findCurrentStepStartEvent(
            @Param("batchId") Long batchId,
            @Param("stepId") Long stepId,
            @Param("enteredAt") OffsetDateTime enteredAt);
}
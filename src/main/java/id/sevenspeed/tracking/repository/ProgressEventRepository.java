package id.sevenspeed.tracking.repository;

import id.sevenspeed.tracking.entity.ProgressEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

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
}
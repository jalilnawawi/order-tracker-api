package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.request.progress.CreateProgressEventRequest;
import id.sevenspeed.tracking.entity.ProgressEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProgressEventService {

    Page<ProgressEvent> findByBatchId(Long batchId, Pageable pageable);

    ProgressEvent append(Long batchId, CreateProgressEventRequest request);
}
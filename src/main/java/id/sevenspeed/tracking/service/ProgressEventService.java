package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.request.progress.CreateProgressEventRequest;
import id.sevenspeed.tracking.dto.response.progress.ProgressEventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProgressEventService {

    Page<ProgressEventResponse> findByBatchId(Long batchId, Pageable pageable);

    ProgressEventResponse append(Long batchId, CreateProgressEventRequest request);
}
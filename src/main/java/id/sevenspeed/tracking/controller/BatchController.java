package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.request.batch.CreateBatchRequest;
import id.sevenspeed.tracking.dto.request.batch.UpdateBatchRequest;
import id.sevenspeed.tracking.dto.request.progress.CreateProgressEventRequest;
import id.sevenspeed.tracking.dto.response.batch.BatchDetailResponse;
import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.dto.response.progress.ProgressEventResponse;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.entity.ProgressEvent;
import id.sevenspeed.tracking.service.BatchService;
import id.sevenspeed.tracking.service.ProgressEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;
    private final ProgressEventService progressEventService;

    @GetMapping("/orders/{orderId}/batches")
    public ResponseEntity<ApiResponse<List<BatchSummaryResponse>>> findByOrderId(
            @PathVariable Long orderId) {
        List<OrderBatch> batches = batchService.findByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.ok(
                batches.stream().map(BatchSummaryResponse::from).toList()));
    }

    @PostMapping("/orders/{orderId}/batches")
    public ResponseEntity<ApiResponse<BatchDetailResponse>> create(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateBatchRequest request) {
        OrderBatch batch = batchService.create(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(BatchDetailResponse.from(batch)));
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<BatchDetailResponse>> findById(
            @PathVariable Long id) {
        OrderBatch batch = batchService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(BatchDetailResponse.from(batch)));
    }

    @PatchMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<BatchDetailResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateBatchRequest request) {
        OrderBatch batch = batchService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(BatchDetailResponse.from(batch)));
    }

    @GetMapping("/batches/{id}/progress-events")
    public ResponseEntity<ApiResponse<List<ProgressEventResponse>>> findProgressEvents(
            @PathVariable Long id,
            Pageable pageable) {
        Page<ProgressEvent> events = progressEventService.findByBatchId(id, pageable);
        return ResponseEntity.ok(ApiResponse.paginated(
                events.map(ProgressEventResponse::from)));
    }

    @PostMapping("/batches/{id}/progress-events")
    public ResponseEntity<ApiResponse<ProgressEventResponse>> appendProgressEvent(
            @PathVariable Long id,
            @Valid @RequestBody CreateProgressEventRequest request) {
        ProgressEvent event = progressEventService.append(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ProgressEventResponse.from(event)));
    }
}
package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.request.batch.CreateBatchRequest;
import id.sevenspeed.tracking.dto.request.batch.UpdateBatchRequest;
import id.sevenspeed.tracking.dto.request.progress.CreateProgressEventRequest;
import id.sevenspeed.tracking.dto.response.batch.BatchDetailResponse;
import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.dto.response.progress.ProgressEventResponse;
import id.sevenspeed.tracking.service.BatchService;
import id.sevenspeed.tracking.service.ProgressEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        return ResponseEntity.ok(ApiResponse.ok(
                batchService.findByOrderId(orderId)));
    }

    @PostMapping("/orders/{orderId}/batches")
    public ResponseEntity<ApiResponse<BatchDetailResponse>> create(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(batchService.create(orderId, request)));
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<BatchDetailResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(batchService.findById(id)));
    }

    @PatchMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<BatchDetailResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateBatchRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(batchService.update(id, request)));
    }

    @GetMapping("/batches/{id}/progress-events")
    public ResponseEntity<ApiResponse<List<ProgressEventResponse>>> findProgressEvents(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.paginated(
                progressEventService.findByBatchId(id, pageable)));
    }

    @PostMapping("/batches/{id}/progress-events")
    public ResponseEntity<ApiResponse<ProgressEventResponse>> appendProgressEvent(
            @PathVariable Long id,
            @Valid @RequestBody CreateProgressEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(progressEventService.append(id, request)));
    }
}
package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.request.batch.CreateBatchRequest;
import id.sevenspeed.tracking.dto.request.batch.UpdateBatchRequest;
import id.sevenspeed.tracking.dto.response.batch.BatchDetailResponse;
import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.entity.OrderBatch;

import java.util.List;

public interface BatchService {

    List<BatchSummaryResponse> findByOrderId(Long orderId);

    BatchDetailResponse findById(Long id);

    BatchDetailResponse create(Long orderId, CreateBatchRequest request);

    BatchDetailResponse update(Long id, UpdateBatchRequest request);

    OrderBatch findEntityById(Long id); // dipakai internal service lain
}
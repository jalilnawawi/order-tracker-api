package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.request.batch.CreateBatchRequest;
import id.sevenspeed.tracking.dto.request.batch.UpdateBatchRequest;
import id.sevenspeed.tracking.entity.OrderBatch;

import java.util.List;

public interface BatchService {

    List<OrderBatch> findByOrderId(Long orderId);

    OrderBatch findById(Long id);

    OrderBatch create(Long orderId, CreateBatchRequest request);

    OrderBatch update(Long id, UpdateBatchRequest request);
}
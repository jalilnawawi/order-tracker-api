package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.request.batch.CreateBatchRequest;
import id.sevenspeed.tracking.dto.request.batch.UpdateBatchRequest;
import id.sevenspeed.tracking.dto.response.batch.BatchDetailResponse;
import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.entity.Order;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.entity.ProductType;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.OrderBatchRepository;
import id.sevenspeed.tracking.repository.ProductTypeRepository;
import id.sevenspeed.tracking.service.BatchService;
import id.sevenspeed.tracking.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final OrderBatchRepository orderBatchRepository;
    private final ProductTypeRepository productTypeRepository;
    private final OrderService orderService;

    @Override
    @Transactional(readOnly = true)
    public List<BatchSummaryResponse> findByOrderId(Long orderId) {
        orderService.findEntityById(orderId); // validasi order exists
        return orderBatchRepository.findByOrderId(orderId)
                .stream()
                .map(BatchSummaryResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BatchDetailResponse findById(Long id) {
        return BatchDetailResponse.from(findEntityById(id));
    }

    @Override
    @Transactional
    public BatchDetailResponse create(Long orderId, CreateBatchRequest request) {
        Order order = orderService.findEntityById(orderId);
        ProductType productType = productTypeRepository
                .findById(request.getProductTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductType", request.getProductTypeId()));

        String batchNumber = generateBatchNumber(order.getOrderNumber(), order.getId());

        OrderBatch batch = new OrderBatch();
        batch.setBatchNumber(batchNumber);
        batch.setOrder(order);
        batch.setProductType(productType);
        batch.setQuantity(request.getQuantity());
        batch.setUnit(request.getUnit() != null ? request.getUnit() : "pcs");
        batch.setSpecifications(request.getSpecifications());
        batch.setNotes(request.getNotes());
        batch.setStatus("DRAFT");

        return BatchDetailResponse.from(orderBatchRepository.save(batch));
    }

    @Override
    @Transactional
    public BatchDetailResponse update(Long id, UpdateBatchRequest request) {
        OrderBatch batch = findEntityById(id);

        if (request.getQuantity() != null) batch.setQuantity(request.getQuantity());
        if (request.getSpecifications() != null) batch.setSpecifications(request.getSpecifications());
        if (request.getNotes() != null) batch.setNotes(request.getNotes());
        if (request.getStatus() != null) batch.setStatus(request.getStatus());

        return BatchDetailResponse.from(orderBatchRepository.save(batch));
    }

    @Override
    public OrderBatch findEntityById(Long id) {
        return orderBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", id));
    }

    private String generateBatchNumber(String orderNumber, Long orderId) {
        long count = orderBatchRepository.findByOrderId(orderId).size() + 1;
        return String.format("%s-B%02d", orderNumber, count);
    }
}
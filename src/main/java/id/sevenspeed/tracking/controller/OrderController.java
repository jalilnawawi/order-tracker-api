package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.request.order.CreateOrderRequest;
import id.sevenspeed.tracking.dto.request.order.UpdateOrderRequest;
import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.dto.response.order.OrderDetailResponse;
import id.sevenspeed.tracking.dto.response.order.OrderListItemResponse;
import id.sevenspeed.tracking.dto.response.order.OrderResponse;
import id.sevenspeed.tracking.entity.Order;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.repository.OrderBatchRepository;
import id.sevenspeed.tracking.service.BatchService;
import id.sevenspeed.tracking.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final BatchService batchService;
    private final OrderBatchRepository orderBatchRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderListItemResponse>>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId,
            Pageable pageable) {
        Page<Order> orders = orderService.findAll(status, customerId, pageable);
        return ResponseEntity.ok(ApiResponse.paginated(orders.map(o ->
                OrderListItemResponse.from(o,
                        orderBatchRepository.findByOrderId(o.getId()).size()))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(OrderResponse.from(order)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> findById(
            @PathVariable Long id) {
        Order order = orderService.findById(id);
        List<OrderBatch> batches = batchService.findByOrderId(id);
        return ResponseEntity.ok(ApiResponse.ok(
                OrderDetailResponse.from(order,
                        batches.stream()
                                .map(BatchSummaryResponse::from)
                                .toList())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateOrderRequest request) {
        Order order = orderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(OrderResponse.from(order)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
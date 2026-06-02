package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.request.order.CreateOrderRequest;
import id.sevenspeed.tracking.dto.request.order.UpdateOrderRequest;
import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.dto.response.order.OrderDetailResponse;
import id.sevenspeed.tracking.dto.response.order.OrderListItemResponse;
import id.sevenspeed.tracking.dto.response.order.OrderResponse;
import id.sevenspeed.tracking.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderListItemResponse>>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.paginated(
                orderService.findAll(status, customerId, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
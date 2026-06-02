package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.dto.response.common.UserResponse;
import id.sevenspeed.tracking.dto.response.division.QueueItemResponse;
import id.sevenspeed.tracking.dto.response.order.OrderDetailResponse;
import id.sevenspeed.tracking.dto.response.order.OrderListItemResponse;
import id.sevenspeed.tracking.entity.Order;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.entity.User;
import id.sevenspeed.tracking.exception.BusinessException;
import id.sevenspeed.tracking.repository.OrderBatchRepository;
import id.sevenspeed.tracking.security.CustomUserDetails;
import id.sevenspeed.tracking.service.BatchService;
import id.sevenspeed.tracking.service.DivisionService;
import id.sevenspeed.tracking.service.OrderService;
import id.sevenspeed.tracking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;
    private final OrderService orderService;
    private final DivisionService divisionService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getCurrentUserResponse()));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderListItemResponse>>> getMyOrders(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        CustomUserDetails currentUser = userService.getCurrentUser();
        Page<OrderListItemResponse> orders = orderService.findAll(status, currentUser.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.paginated(orders));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getMyOrderDetail(
            @PathVariable Long id) {
        CustomUserDetails currentUser = userService.getCurrentUser();
        OrderDetailResponse order = orderService.findByIdAndCustomerId(id, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<QueueItemResponse>>> getMyQueue() {
        CustomUserDetails currentUser = userService.getCurrentUser();
        if (currentUser.getDivisionId() == null) {
            throw new BusinessException("NO_DIVISION",
                    "User does not have a division assigned", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        List<QueueItemResponse> queue = divisionService.findQueueByDivisionId(
                currentUser.getDivisionId());
        return ResponseEntity.ok(ApiResponse.ok(queue));
    }
}
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
    private final BatchService batchService;
    private final DivisionService divisionService;
    private final OrderBatchRepository orderBatchRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        User user = userService.getCurrentUserEntity();
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roleCode(user.getRole().getCode())
                .divisionId(user.getDivision() != null ? user.getDivision().getId() : null)
                .divisionCode(user.getDivision() != null ? user.getDivision().getCode() : null)
                .divisionName(user.getDivision() != null ? user.getDivision().getName() : null)
                .customerType(user.getCustomerType())
                .institutionName(user.getInstitutionName())
                .isActive(user.getIsActive())
                .build()));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderListItemResponse>>> getMyOrders(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        CustomUserDetails currentUser = userService.getCurrentUser();
        Page<Order> orders = orderService.findAll(status, currentUser.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.paginated(orders.map(o ->
                OrderListItemResponse.from(o,
                        orderBatchRepository.findByOrderId(o.getId()).size()))));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getMyOrderDetail(
            @PathVariable Long id) {
        CustomUserDetails currentUser = userService.getCurrentUser();
        Order order = orderService.findByIdAndCustomerId(id, currentUser.getUserId());
        List<OrderBatch> batches = batchService.findByOrderId(id);
        return ResponseEntity.ok(ApiResponse.ok(
                OrderDetailResponse.from(order,
                        batches.stream()
                                .map(BatchSummaryResponse::from)
                                .toList())));
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<QueueItemResponse>>> getMyQueue() {
        CustomUserDetails currentUser = userService.getCurrentUser();
        if (currentUser.getDivisionId() == null) {
            throw new BusinessException("NO_DIVISION",
                    "User does not have a division assigned", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        List<OrderBatch> queue = divisionService.findQueueByDivisionId(
                currentUser.getDivisionId());
        return ResponseEntity.ok(ApiResponse.ok(
                queue.stream().map(QueueItemResponse::from).toList()));
    }
}
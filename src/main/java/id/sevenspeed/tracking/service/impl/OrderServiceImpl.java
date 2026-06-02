package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.request.order.CreateOrderRequest;
import id.sevenspeed.tracking.dto.request.order.UpdateOrderRequest;
import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.dto.response.order.OrderDetailResponse;
import id.sevenspeed.tracking.dto.response.order.OrderListItemResponse;
import id.sevenspeed.tracking.dto.response.order.OrderResponse;
import id.sevenspeed.tracking.entity.Order;
import id.sevenspeed.tracking.entity.User;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.OrderBatchRepository;
import id.sevenspeed.tracking.repository.OrderRepository;
import id.sevenspeed.tracking.service.OrderService;
import id.sevenspeed.tracking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderBatchRepository orderBatchRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListItemResponse> findAll(String status, Long customerId, Pageable pageable) {
        List<Specification<Order>> specs = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            specs.add((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (customerId != null) {
            specs.add((root, query, cb) ->
                    cb.equal(root.get("customer").get("id"), customerId));
        }

        Specification<Order> spec = specs.isEmpty()
                ? Specification.allOf()
                : Specification.allOf(specs);

        return orderRepository.findAll(spec, pageable)
                .map(o -> OrderListItemResponse.from(o,
                        orderBatchRepository.findByOrderId(o.getId()).size()));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse findById(Long id) {
        Order order = findEntityById(id);
        List<BatchSummaryResponse> batches = orderBatchRepository
                .findByOrderId(id)
                .stream()
                .map(BatchSummaryResponse::from)
                .toList();
        return OrderDetailResponse.from(order, batches);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse findByIdAndCustomerId(Long id, Long customerId) {
        Order order = findEntityById(id);
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Order", id);
        }
        List<BatchSummaryResponse> batches = orderBatchRepository
                .findByOrderId(id)
                .stream()
                .map(BatchSummaryResponse::from)
                .toList();
        return OrderDetailResponse.from(order, batches);
    }

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        User customer = userService.getUserById(request.getCustomerId());
        User currentUser = userService.getCurrentUserEntity();
        String orderNumber = generateOrderNumber();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setTitle(request.getTitle());
        order.setDescription(request.getDescription());
        order.setStatus("DRAFT");
        order.setOrderDate(request.getOrderDate());
        order.setDeadlineDate(request.getDeadlineDate());
        order.setNotes(request.getNotes());
        order.setCreatedBy(currentUser);

        return OrderResponse.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse update(Long id, UpdateOrderRequest request) {
        Order order = findEntityById(id);

        if (request.getTitle() != null) order.setTitle(request.getTitle());
        if (request.getDescription() != null) order.setDescription(request.getDescription());
        if (request.getNotes() != null) order.setNotes(request.getNotes());
        if (request.getDeadlineDate() != null) order.setDeadlineDate(request.getDeadlineDate());
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
            if ("COMPLETED".equals(request.getStatus())) {
                order.setCompletedAt(OffsetDateTime.now());
            }
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Order order = findEntityById(id);
        orderRepository.delete(order);
    }

    @Override
    public Order findEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private String generateOrderNumber() {
        String year = String.valueOf(java.time.LocalDate.now().getYear());
        long count = orderRepository.count() + 1;
        return String.format("SPK-%s-%04d", year, count);
    }
}
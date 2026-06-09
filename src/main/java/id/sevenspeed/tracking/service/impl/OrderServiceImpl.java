package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.request.order.CreateOrderRequest;
import id.sevenspeed.tracking.dto.request.order.UpdateOrderRequest;
import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.dto.response.order.OrderDetailResponse;
import id.sevenspeed.tracking.dto.response.order.OrderListItemResponse;
import id.sevenspeed.tracking.dto.response.order.OrderResponse;
import id.sevenspeed.tracking.dto.response.order.OrderTimelineStepResponse;
import id.sevenspeed.tracking.entity.Order;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.entity.ProgressEvent;
import id.sevenspeed.tracking.entity.User;
import id.sevenspeed.tracking.entity.WorkflowStep;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.OrderBatchRepository;
import id.sevenspeed.tracking.repository.OrderRepository;
import id.sevenspeed.tracking.repository.ProgressEventRepository;
import id.sevenspeed.tracking.repository.WorkflowStepRepository;
import id.sevenspeed.tracking.service.OrderService;
import id.sevenspeed.tracking.service.UserService;
import id.sevenspeed.tracking.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderBatchRepository orderBatchRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final ProgressEventRepository progressEventRepository;
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
    @Transactional(readOnly = true)
    public List<OrderTimelineStepResponse> getCustomerTimeline(Long id, Long customerId) {
        Order order = findEntityById(id);
        if (!order.getCustomer().getId().equals(customerId)) {
            // 404 wajar, bukan 500 — order bukan milik customer ini
            throw new ResourceNotFoundException("Order", id);
        }

        // Seed-nya 1 batch/order. Kalau ada >1, pakai batch pertama (by id).
        List<OrderBatch> batches = orderBatchRepository.findByOrderId(id);
        if (batches.isEmpty()) {
            return List.of();
        }
        OrderBatch batch = batches.stream()
                .min(Comparator.comparing(OrderBatch::getId))
                .orElseThrow();

        Long workflowId = batch.getProductType().getWorkflow().getId();
        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowIdOrderBySequenceNumberAsc(workflowId);

        // Event terakhir per step (urut performedAt asc → entry terakhir menang)
        Map<Long, ProgressEvent> latestByStep = progressEventRepository
                .findByBatchIdOrderByPerformedAtAsc(batch.getId())
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getWorkflowStep().getId(),
                        e -> e,
                        (a, b) -> b));

        Long currentStepId = batch.getCurrentStep() != null
                ? batch.getCurrentStep().getId() : null;
        boolean batchOnHold = "ON_HOLD".equals(batch.getStatus());

        return steps.stream()
                .map(step -> {
                    ProgressEvent latest = latestByStep.get(step.getId());
                    String status = resolveStepStatus(step, latest, currentStepId, batchOnHold);
                    return OrderTimelineStepResponse.builder()
                            .stepName(step.getName())
                            .status(status)
                            .message(buildTimelineMessage(step.getName(), status))
                            .at(latest != null ? DateTimeUtil.format(latest.getPerformedAt()) : null)
                            .build();
                })
                .toList();
    }

    private String resolveStepStatus(WorkflowStep step, ProgressEvent latest,
                                     Long currentStepId, boolean batchOnHold) {
        String type = latest != null ? latest.getEventType() : null;
        boolean isCurrent = currentStepId != null && step.getId().equals(currentStepId);

        if ("STEP_COMPLETED".equals(type)) {
            return "DONE";
        }
        if ("STEP_FAILED".equals(type)) {
            return "FAILED";
        }
        if (isCurrent) {
            return batchOnHold ? "ON_HOLD" : "CURRENT";
        }
        if ("STEP_STARTED".equals(type) || "RESUMED".equals(type) || "ON_HOLD".equals(type)) {
            return "ON_HOLD".equals(type) ? "ON_HOLD" : "CURRENT";
        }
        return "UPCOMING";
    }

    private String buildTimelineMessage(String stepName, String status) {
        return switch (status) {
            case "DONE" -> "Tahap " + stepName + " telah selesai.";
            case "CURRENT" -> "Sedang dikerjakan: " + stepName + ".";
            case "FAILED" -> "Tahap " + stepName + " bermasalah dan sedang ditangani.";
            case "ON_HOLD" -> "Tahap " + stepName + " ditahan sementara.";
            default -> "Menunggu tahap " + stepName + ".";
        };
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
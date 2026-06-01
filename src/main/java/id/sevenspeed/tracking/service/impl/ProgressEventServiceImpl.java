package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.request.progress.CreateProgressEventRequest;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.entity.ProgressEvent;
import id.sevenspeed.tracking.entity.WorkflowStep;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.OrderBatchRepository;
import id.sevenspeed.tracking.repository.OrderRepository;
import id.sevenspeed.tracking.repository.ProgressEventRepository;
import id.sevenspeed.tracking.repository.WorkflowStepRepository;
import id.sevenspeed.tracking.security.CustomUserDetails;
import id.sevenspeed.tracking.service.ProgressEventService;
import id.sevenspeed.tracking.service.UserService;
import id.sevenspeed.tracking.validator.ProgressEventValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressEventServiceImpl implements ProgressEventService {

    private final ProgressEventRepository progressEventRepository;
    private final OrderRepository orderRepository;
    private final OrderBatchRepository orderBatchRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final ProgressEventValidator validator;
    private final UserService userService;

    @Override
    public Page<ProgressEvent> findByBatchId(Long batchId, Pageable pageable) {
        return progressEventRepository.findByBatchId(batchId, pageable);
    }

    @Override
    @Transactional
    public ProgressEvent append(Long batchId, CreateProgressEventRequest request) {
        CustomUserDetails currentUser = userService.getCurrentUser();
        OrderBatch batch = orderBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", batchId));

        WorkflowStep step = workflowStepRepository.findById(request.getWorkflowStepId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStep", request.getWorkflowStepId()));

        WorkflowStep targetStep = resolveTargetStep(request.getTargetWorkflowStepId());
        ProgressEvent correctsEvent = resolveCorrectsEvent(request.getCorrectsEventId());

        validator.validate(request.getEventType(), batch, step, targetStep, correctsEvent, currentUser);

        ProgressEvent event = buildEvent(request, batch, step, correctsEvent);
        ProgressEvent saved = progressEventRepository.save(event);

        applyBatchSideEffects(request.getEventType(), batch, step, targetStep);

        return saved;
    }

    private WorkflowStep resolveTargetStep(Long targetStepId) {
        if (targetStepId == null) return null;
        return workflowStepRepository.findById(targetStepId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStep", targetStepId));
    }

    private ProgressEvent resolveCorrectsEvent(Long correctsEventId) {
        if (correctsEventId == null) return null;
        return progressEventRepository.findById(correctsEventId)
                .orElseThrow(() -> new ResourceNotFoundException("ProgressEvent", correctsEventId));
    }

    private ProgressEvent buildEvent(CreateProgressEventRequest request, OrderBatch batch,
                                     WorkflowStep step, ProgressEvent correctsEvent) {
        ProgressEvent event = new ProgressEvent();
        event.setBatch(batch);
        event.setWorkflowStep(step);
        event.setEventType(request.getEventType());
        event.setPerformedBy(userService.getCurrentUserEntity());
        event.setNotes(request.getNotes());
        event.setMetadata(request.getMetadata());
        event.setCorrectsEvent(correctsEvent);
        return event;
    }

    private void applyBatchSideEffects(String eventType, OrderBatch batch,
                                       WorkflowStep step, WorkflowStep targetStep) {
        switch (eventType) {
            case "STEP_STARTED" -> {
                if (batch.getStartedAt() == null) batch.setStartedAt(OffsetDateTime.now());
                batch.setStatus("IN_PROGRESS");
                batch.setCurrentStep(step);
                batch.setCurrentStepEnteredAt(OffsetDateTime.now());
            }
            case "STEP_COMPLETED" -> {
                if (Boolean.TRUE.equals(step.getIsFinal())) {
                    batch.setStatus("COMPLETED");
                    batch.setCompletedAt(OffsetDateTime.now());
                }
                batch.setCurrentStep(step);
            }
            case "STEP_FAILED" -> batch.setCurrentStep(step);
            case "REWORK" -> {
                batch.setStatus("IN_PROGRESS");
                batch.setCurrentStep(targetStep);
                batch.setCurrentStepEnteredAt(OffsetDateTime.now());
            }
            case "ON_HOLD" -> batch.setStatus("ON_HOLD");
            case "RESUMED" -> batch.setStatus("IN_PROGRESS");
            case "CORRECTION" -> log.info("CORRECTION event — no batch state change");
        }

        orderBatchRepository.save(batch);

        if ("COMPLETED".equals(batch.getStatus())) {
            cascadeOrderStatus(batch.getOrder().getId());
        }
    }

    private void cascadeOrderStatus(Long orderId) {
        List<OrderBatch> allBatches = orderBatchRepository.findByOrderId(orderId);

        boolean allCompleted = allBatches.stream()
                .allMatch(b -> "COMPLETED".equals(b.getStatus()));

        if (allCompleted) {
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus("COMPLETED");
                order.setCompletedAt(OffsetDateTime.now());
                orderRepository.save(order);
                log.info("Order {} auto-completed — all batches completed", order.getOrderNumber());
            });
        }
    }
}
package id.sevenspeed.tracking.validator;

import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.entity.ProgressEvent;
import id.sevenspeed.tracking.entity.WorkflowStep;
import id.sevenspeed.tracking.exception.BatchInvalidTransitionException;
import id.sevenspeed.tracking.exception.BatchOperatorWrongDivisionException;
import id.sevenspeed.tracking.exception.BatchTargetStepInvalidException;
import id.sevenspeed.tracking.repository.ProgressEventRepository;
import id.sevenspeed.tracking.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProgressEventValidator {

    private final ProgressEventRepository progressEventRepository;

    public void validate(String eventType, OrderBatch batch,
                         WorkflowStep step, WorkflowStep targetStep,
                         ProgressEvent correctsEvent, CustomUserDetails currentUser) {
        switch (eventType) {
            case "STEP_STARTED" -> validateStepStarted(batch, step, currentUser);
            case "STEP_COMPLETED" -> validateStepCompleted(batch, step, currentUser);
            case "STEP_FAILED" -> validateStepFailed(batch, step, currentUser);
            case "REWORK" -> validateRework(batch, targetStep, currentUser);
            case "ON_HOLD" -> validateOnHold(batch);
            case "RESUMED" -> validateResumed(batch);
            case "CORRECTION" -> validateCorrection(correctsEvent, currentUser);
            default -> throw new BatchInvalidTransitionException("Unknown event type: " + eventType);
        }
    }

    private void validateStepStarted(OrderBatch batch, WorkflowStep step, CustomUserDetails currentUser) {
        checkDivisionMatch(step, currentUser);

        Optional<ProgressEvent> lastEvent =
                progressEventRepository.findTopByBatchIdOrderByPerformedAtDesc(batch.getId());

        if (lastEvent.isEmpty()) {
            // Batch baru — hanya boleh START di step pertama (sequence 1)
            if (step.getSequenceNumber() != 1) {
                throw new BatchInvalidTransitionException(
                        "New batch must start at sequence 1");
            }
            return;
        }

        ProgressEvent last = lastEvent.get();
        boolean lastIsCompleted = "STEP_COMPLETED".equals(last.getEventType());
        boolean prevStep = last.getWorkflowStep().getSequenceNumber()
                == step.getSequenceNumber() - 1;

        if (!lastIsCompleted || !prevStep) {
            throw new BatchInvalidTransitionException(
                    "STEP_STARTED requires previous step to be STEP_COMPLETED");
        }
    }

    private void validateStepCompleted(OrderBatch batch, WorkflowStep step, CustomUserDetails currentUser) {
        checkDivisionMatch(step, currentUser);

        ProgressEvent last = progressEventRepository
                .findTopByBatchIdOrderByPerformedAtDesc(batch.getId())
                .orElseThrow(() -> new BatchInvalidTransitionException(
                        "No events found for this batch"));

        boolean lastIsStarted = "STEP_STARTED".equals(last.getEventType());
        boolean sameStep = last.getWorkflowStep().getId().equals(step.getId());

        if (!lastIsStarted || !sameStep) {
            throw new BatchInvalidTransitionException(
                    "STEP_COMPLETED requires STEP_STARTED at the same step");
        }
    }

    private void validateStepFailed(OrderBatch batch, WorkflowStep step, CustomUserDetails currentUser) {
        boolean isAdmin = "ADMIN".equals(currentUser.getRoleCode());
        if (!isAdmin) {
            checkDivisionMatch(step, currentUser);
        }

        if (!"IN_PROGRESS".equals(batch.getStatus())) {
            throw new BatchInvalidTransitionException(
                    "STEP_FAILED requires batch status IN_PROGRESS");
        }
    }

    private void validateRework(OrderBatch batch, WorkflowStep targetStep, CustomUserDetails currentUser) {
        if (!"ADMIN".equals(currentUser.getRoleCode())) {
            throw new BatchInvalidTransitionException(
                    "Only admin can trigger REWORK");
        }

        if (targetStep == null) {
            throw new BatchInvalidTransitionException(
                    "REWORK requires targetWorkflowStepId");
        }

        WorkflowStep currentStep = batch.getCurrentStep();
        if (currentStep == null || targetStep.getSequenceNumber() >= currentStep.getSequenceNumber()) {
            throw new BatchTargetStepInvalidException();
        }
    }

    private void validateOnHold(OrderBatch batch) {
        if (!"IN_PROGRESS".equals(batch.getStatus())) {
            throw new BatchInvalidTransitionException(
                    "ON_HOLD requires batch status IN_PROGRESS");
        }
    }

    private void validateResumed(OrderBatch batch) {
        ProgressEvent last = progressEventRepository
                .findTopByBatchIdOrderByPerformedAtDesc(batch.getId())
                .orElseThrow(() -> new BatchInvalidTransitionException(
                        "No events found for this batch"));

        if (!"ON_HOLD".equals(last.getEventType())) {
            throw new BatchInvalidTransitionException(
                    "RESUMED requires last event to be ON_HOLD");
        }
    }

    private void validateCorrection(ProgressEvent correctsEvent, CustomUserDetails currentUser) {
        if (!"ADMIN".equals(currentUser.getRoleCode())) {
            throw new BatchInvalidTransitionException(
                    "Only admin can trigger CORRECTION");
        }

        if (correctsEvent == null) {
            throw new BatchInvalidTransitionException(
                    "CORRECTION requires correctsEventId");
        }
    }

    private void checkDivisionMatch(WorkflowStep step, CustomUserDetails currentUser) {
        if ("ADMIN".equals(currentUser.getRoleCode())) return;

        Long stepDivisionId = step.getDivision().getId();
        Long userDivisionId = currentUser.getDivisionId();

        if (!stepDivisionId.equals(userDivisionId)) {
            throw new BatchOperatorWrongDivisionException();
        }
    }
}
package id.sevenspeed.tracking.dto.response.division;

import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.dto.response.workflow.WorkflowStepResponse;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QueueItemResponse {

    private BatchSummaryResponse batch;
    private String orderNumber;
    private String orderTitle;
    private String customerName;
    private WorkflowStepResponse currentStep;
    private String hasStartedAt;
    private String enteredAt;
    private Boolean isUrgent;
    private String deadlineDate;

    public static QueueItemResponse from(OrderBatch batch) {
        boolean isUrgent = batch.getOrder().getDeadlineDate() != null
                && batch.getOrder().getDeadlineDate()
                        .isBefore(java.time.LocalDate.now().plusDays(3))
                && !"COMPLETED".equals(batch.getStatus());

        return QueueItemResponse.builder()
                .batch(BatchSummaryResponse.from(batch))
                .orderNumber(batch.getOrder().getOrderNumber())
                .orderTitle(batch.getOrder().getTitle())
                .customerName(batch.getOrder().getCustomer().getFullName())
                .currentStep(batch.getCurrentStep() != null
                        ? WorkflowStepResponse.from(batch.getCurrentStep()) : null)
                .hasStartedAt(DateTimeUtil.format(batch.getStartedAt()))
                .enteredAt(DateTimeUtil.format(batch.getCurrentStepEnteredAt()))
                .isUrgent(isUrgent)
                .deadlineDate(batch.getOrder().getDeadlineDate() != null
                        ? batch.getOrder().getDeadlineDate().toString() : null)
                .build();
    }
}
package id.sevenspeed.tracking.dto.response.batch;

import id.sevenspeed.tracking.dto.response.workflow.WorkflowStepResponse;
import id.sevenspeed.tracking.entity.OrderBatch;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BatchSummaryResponse {

    private Long id;
    private String batchNumber;
    private Integer quantity;
    private String unit;
    private String status;
    private WorkflowStepResponse currentStep;

    public static BatchSummaryResponse from(OrderBatch batch) {
        return BatchSummaryResponse.builder()
                .id(batch.getId())
                .batchNumber(batch.getBatchNumber())
                .quantity(batch.getQuantity())
                .unit(batch.getUnit())
                .status(batch.getStatus())
                .currentStep(batch.getCurrentStep() != null
                        ? WorkflowStepResponse.from(batch.getCurrentStep()) : null)
                .build();
    }
}
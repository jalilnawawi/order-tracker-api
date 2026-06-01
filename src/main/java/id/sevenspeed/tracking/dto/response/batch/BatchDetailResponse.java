package id.sevenspeed.tracking.dto.response.batch;

import id.sevenspeed.tracking.dto.response.workflow.WorkflowStepResponse;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class BatchDetailResponse {

    private Long id;
    private String batchNumber;
    private Long orderId;
    private String orderNumber;
    private String productTypeCode;
    private String productTypeName;
    private Integer quantity;
    private String unit;
    private Map<String, Object> specifications;
    private WorkflowStepResponse currentStep;
    private String currentStepEnteredAt;
    private String status;
    private String startedAt;
    private String completedAt;
    private String notes;
    private String createdAt;
    private String updatedAt;

    public static BatchDetailResponse from(OrderBatch batch) {
        return BatchDetailResponse.builder()
                .id(batch.getId())
                .batchNumber(batch.getBatchNumber())
                .orderId(batch.getOrder().getId())
                .orderNumber(batch.getOrder().getOrderNumber())
                .productTypeCode(batch.getProductType().getCode())
                .productTypeName(batch.getProductType().getName())
                .quantity(batch.getQuantity())
                .unit(batch.getUnit())
                .specifications(batch.getSpecifications())
                .currentStep(batch.getCurrentStep() != null
                        ? WorkflowStepResponse.from(batch.getCurrentStep()) : null)
                .currentStepEnteredAt(DateTimeUtil.format(batch.getCurrentStepEnteredAt()))
                .status(batch.getStatus())
                .startedAt(DateTimeUtil.format(batch.getStartedAt()))
                .completedAt(DateTimeUtil.format(batch.getCompletedAt()))
                .notes(batch.getNotes())
                .createdAt(DateTimeUtil.format(batch.getCreatedAt()))
                .updatedAt(DateTimeUtil.format(batch.getUpdatedAt()))
                .build();
    }
}
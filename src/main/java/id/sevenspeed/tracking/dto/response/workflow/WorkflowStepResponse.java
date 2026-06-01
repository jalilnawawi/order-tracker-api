package id.sevenspeed.tracking.dto.response.workflow;

import id.sevenspeed.tracking.dto.response.division.DivisionResponse;
import id.sevenspeed.tracking.entity.WorkflowStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowStepResponse {

    private Long id;
    private Integer sequenceNumber;
    private String code;
    private String name;
    private Boolean isFinal;
    private Boolean isCheckpoint;
    private DivisionResponse division;

    public static WorkflowStepResponse from(WorkflowStep step) {
        return WorkflowStepResponse.builder()
                .id(step.getId())
                .sequenceNumber(step.getSequenceNumber())
                .code(step.getCode())
                .name(step.getName())
                .isFinal(step.getIsFinal())
                .isCheckpoint(step.getIsCheckpoint())
                .division(DivisionResponse.from(step.getDivision()))
                .build();
    }
}
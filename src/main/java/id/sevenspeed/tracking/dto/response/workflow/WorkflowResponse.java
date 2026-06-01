package id.sevenspeed.tracking.dto.response.workflow;

import id.sevenspeed.tracking.entity.Workflow;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkflowResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
    private List<WorkflowStepResponse> steps;

    public static WorkflowResponse from(Workflow workflow) {
        return WorkflowResponse.builder()
                .id(workflow.getId())
                .code(workflow.getCode())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .isActive(workflow.getIsActive())
                .build();
    }

    public static WorkflowResponse from(Workflow workflow, List<WorkflowStepResponse> steps) {
        return WorkflowResponse.builder()
                .id(workflow.getId())
                .code(workflow.getCode())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .isActive(workflow.getIsActive())
                .steps(steps)
                .build();
    }
}
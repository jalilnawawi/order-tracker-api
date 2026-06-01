package id.sevenspeed.tracking.dto.request.progress;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Map;

@Getter
public class CreateProgressEventRequest {

    @NotNull(message = "Event type is required")
    private String eventType;

    @NotNull(message = "Workflow step is required")
    private Long workflowStepId;

    private Long targetWorkflowStepId;

    private Long correctsEventId;

    private String notes;

    private Map<String, Object> metadata;
}
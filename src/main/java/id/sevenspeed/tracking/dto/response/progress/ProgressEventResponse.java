package id.sevenspeed.tracking.dto.response.progress;

import id.sevenspeed.tracking.dto.response.common.UserSummaryResponse;
import id.sevenspeed.tracking.dto.response.workflow.WorkflowStepResponse;
import id.sevenspeed.tracking.entity.ProgressEvent;
import id.sevenspeed.tracking.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ProgressEventResponse {

    private Long id;
    private Long batchId;
    private WorkflowStepResponse workflowStep;
    private String eventType;
    private UserSummaryResponse performedBy;
    private String performedAt;
    private String notes;
    private Map<String, Object> metadata;
    private Long correctsEventId;
    private String createdAt;

    public static ProgressEventResponse from(ProgressEvent event) {
        return ProgressEventResponse.builder()
                .id(event.getId())
                .batchId(event.getBatch().getId())
                .workflowStep(WorkflowStepResponse.from(event.getWorkflowStep()))
                .eventType(event.getEventType())
                .performedBy(UserSummaryResponse.from(event.getPerformedBy()))
                .performedAt(DateTimeUtil.format(event.getPerformedAt()))
                .notes(event.getNotes())
                .metadata(event.getMetadata())
                .correctsEventId(event.getCorrectsEvent() != null
                        ? event.getCorrectsEvent().getId() : null)
                .createdAt(DateTimeUtil.format(event.getCreatedAt()))
                .build();
    }
}
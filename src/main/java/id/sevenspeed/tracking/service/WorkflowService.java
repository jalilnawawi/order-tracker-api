package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.response.workflow.WorkflowResponse;
import id.sevenspeed.tracking.dto.response.workflow.WorkflowStepResponse;
import id.sevenspeed.tracking.entity.Workflow;
import id.sevenspeed.tracking.entity.WorkflowStep;

import java.util.List;

public interface WorkflowService {

    List<WorkflowResponse> findAll();

    WorkflowResponse findById(Long id);

    List<WorkflowStepResponse> findStepsByWorkflowId(Long workflowId);

    Workflow findEntityById(Long id);
}
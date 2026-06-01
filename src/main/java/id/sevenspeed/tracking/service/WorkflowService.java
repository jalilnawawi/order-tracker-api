package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.entity.Workflow;
import id.sevenspeed.tracking.entity.WorkflowStep;

import java.util.List;

public interface WorkflowService {

    List<Workflow> findAll();

    Workflow findById(Long id);

    List<WorkflowStep> findStepsByWorkflowId(Long workflowId);
}
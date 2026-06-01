package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.dto.response.workflow.WorkflowResponse;
import id.sevenspeed.tracking.dto.response.workflow.WorkflowStepResponse;
import id.sevenspeed.tracking.entity.Workflow;
import id.sevenspeed.tracking.entity.WorkflowStep;
import id.sevenspeed.tracking.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkflowResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(
                workflowService.findAll().stream()
                        .map(WorkflowResponse::from)
                        .toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> findById(
            @PathVariable Long id) {
        Workflow workflow = workflowService.findById(id);
        List<WorkflowStepResponse> steps = workflowService.findStepsByWorkflowId(id)
                .stream()
                .map(WorkflowStepResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(WorkflowResponse.from(workflow, steps)));
    }

    @GetMapping("/{id}/steps")
    public ResponseEntity<ApiResponse<List<WorkflowStepResponse>>> findSteps(
            @PathVariable Long id) {
        List<WorkflowStep> steps = workflowService.findStepsByWorkflowId(id);
        return ResponseEntity.ok(ApiResponse.ok(
                steps.stream().map(WorkflowStepResponse::from).toList()));
    }
}
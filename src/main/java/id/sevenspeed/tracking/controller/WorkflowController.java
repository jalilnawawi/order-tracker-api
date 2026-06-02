package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.dto.response.workflow.WorkflowResponse;
import id.sevenspeed.tracking.dto.response.workflow.WorkflowStepResponse;
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
        return ResponseEntity.ok(ApiResponse.ok(workflowService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(workflowService.findById(id)));
    }

    @GetMapping("/{id}/steps")
    public ResponseEntity<ApiResponse<List<WorkflowStepResponse>>> findSteps(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                workflowService.findStepsByWorkflowId(id)));
    }
}
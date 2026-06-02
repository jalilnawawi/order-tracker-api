package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.response.division.DivisionResponse;
import id.sevenspeed.tracking.dto.response.workflow.WorkflowResponse;
import id.sevenspeed.tracking.dto.response.workflow.WorkflowStepResponse;
import id.sevenspeed.tracking.entity.Workflow;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.WorkflowRepository;
import id.sevenspeed.tracking.repository.WorkflowStepRepository;
import id.sevenspeed.tracking.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowResponse> findAll() {
        return workflowRepository.findAll()
                .stream()
                .map(WorkflowResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowResponse findById(Long id) {
        Workflow workflow = findEntityById(id);
        List<WorkflowStepResponse> steps = findStepsByWorkflowId(id);
        return WorkflowResponse.from(workflow, steps);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowStepResponse> findStepsByWorkflowId(Long workflowId) {
        findEntityById(workflowId);
        return workflowStepRepository
                .findByWorkflowIdOrderBySequenceNumberAsc(workflowId)
                .stream()
                .map(ws -> WorkflowStepResponse.builder()
                        .id(ws.getId())
                        .sequenceNumber(ws.getSequenceNumber())
                        .code(ws.getCode())
                        .name(ws.getName())
                        .isFinal(ws.getIsFinal())
                        .isCheckpoint(ws.getIsCheckpoint())
                        .division(DivisionResponse.from(ws.getDivision()))
                        .build())
                .toList();
    }

    @Override
    public Workflow findEntityById(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow", id));
    }
}
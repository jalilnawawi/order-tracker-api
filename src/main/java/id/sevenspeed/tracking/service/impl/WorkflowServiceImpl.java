package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.entity.Workflow;
import id.sevenspeed.tracking.entity.WorkflowStep;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.WorkflowRepository;
import id.sevenspeed.tracking.repository.WorkflowStepRepository;
import id.sevenspeed.tracking.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;

    @Override
    public List<Workflow> findAll() {
        return workflowRepository.findAll();
    }

    @Override
    public Workflow findById(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow", id));
    }

    @Override
    public List<WorkflowStep> findStepsByWorkflowId(Long workflowId) {
        findById(workflowId); // validasi workflow exists
        return workflowStepRepository.findByWorkflowIdOrderBySequenceNumberAsc(workflowId);
    }
}
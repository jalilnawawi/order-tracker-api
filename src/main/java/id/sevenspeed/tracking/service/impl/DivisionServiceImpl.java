package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.entity.Division;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.entity.WorkflowStep;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.DivisionRepository;
import id.sevenspeed.tracking.repository.OrderBatchRepository;
import id.sevenspeed.tracking.repository.WorkflowStepRepository;
import id.sevenspeed.tracking.service.DivisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;
    private final OrderBatchRepository orderBatchRepository;
    private final WorkflowStepRepository workflowStepRepository;

    @Override
    public List<Division> findAll() {
        return divisionRepository.findAll();
    }

    @Override
    public Division findById(Long id) {
        return divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division", id));
    }

    @Override
    public List<OrderBatch> findQueueByDivisionId(Long divisionId) {
        findById(divisionId); // validasi division exists
        return orderBatchRepository.findQueueByDivisionId(divisionId);
    }
}
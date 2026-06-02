package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.response.division.DivisionResponse;
import id.sevenspeed.tracking.dto.response.division.QueueItemResponse;
import id.sevenspeed.tracking.entity.Division;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.DivisionRepository;
import id.sevenspeed.tracking.repository.OrderBatchRepository;
import id.sevenspeed.tracking.service.DivisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;
    private final OrderBatchRepository orderBatchRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DivisionResponse> findAll() {
        return divisionRepository.findAll()
                .stream()
                .map(DivisionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DivisionResponse findById(Long id) {
        return DivisionResponse.from(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueItemResponse> findQueueByDivisionId(Long divisionId) {
        findEntityById(divisionId); // validasi division exists
        return orderBatchRepository.findQueueByDivisionId(divisionId)
                .stream()
                .map(QueueItemResponse::from)
                .toList();
    }

    @Override
    public Division findEntityById(Long id) {
        return divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division", id));
    }
}
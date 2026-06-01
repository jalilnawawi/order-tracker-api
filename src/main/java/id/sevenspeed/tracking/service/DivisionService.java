package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.entity.Division;
import id.sevenspeed.tracking.entity.OrderBatch;

import java.util.List;

public interface DivisionService {

    List<Division> findAll();

    Division findById(Long id);

    List<OrderBatch> findQueueByDivisionId(Long divisionId);
}
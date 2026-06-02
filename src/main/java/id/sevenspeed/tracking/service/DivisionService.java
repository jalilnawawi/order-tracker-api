package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.response.division.DivisionResponse;
import id.sevenspeed.tracking.dto.response.division.QueueItemResponse;
import id.sevenspeed.tracking.entity.Division;

import java.util.List;

public interface DivisionService {

    List<DivisionResponse> findAll();

    DivisionResponse findById(Long id);

    List<QueueItemResponse> findQueueByDivisionId(Long divisionId);

    Division findEntityById(Long id); // dipakai internal
}
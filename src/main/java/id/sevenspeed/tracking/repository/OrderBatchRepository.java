package id.sevenspeed.tracking.repository;

import id.sevenspeed.tracking.entity.OrderBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderBatchRepository extends JpaRepository<OrderBatch, Long> {
    List<OrderBatch> findByOrderId(Long orderId);
    Optional<OrderBatch> findByBatchNumber(String batchNumber);
    List<OrderBatch> findByCurrentStepIdAndStatus(Long stepId, String status);
}

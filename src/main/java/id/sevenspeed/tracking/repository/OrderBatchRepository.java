package id.sevenspeed.tracking.repository;

import id.sevenspeed.tracking.entity.OrderBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderBatchRepository extends JpaRepository<OrderBatch, Long> {
    List<OrderBatch> findByOrderId(Long orderId);
    Optional<OrderBatch> findByBatchNumber(String batchNumber);
    List<OrderBatch> findByCurrentStepIdAndStatus(Long stepId, String status);

    @Query("""
            SELECT ob FROM OrderBatch ob
            JOIN ob.currentStep ws
            WHERE ws.division.id = :divisionId
            AND ob.status = 'IN_PROGRESS'
            """)
    List<OrderBatch> findQueueByDivisionId(@Param("divisionId") Long divisionId);
}

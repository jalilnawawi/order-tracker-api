package id.sevenspeed.tracking.repository;

import id.sevenspeed.tracking.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {
    List<WorkflowStep> findByWorkflowIdOrderBySequenceNumberAsc(Long workflowId);
    Optional<WorkflowStep> findByWorkflowIdAndCode(Long workflowId, String code);
}
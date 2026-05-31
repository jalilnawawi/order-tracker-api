package id.sevenspeed.tracking.repository;

import id.sevenspeed.tracking.entity.Barcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarcodeRepository extends JpaRepository<Barcode, Long> {
    Optional<Barcode> findByCodeAndIsActiveTrue(String code);
    List<Barcode> findByBatchId(Long batchId);
}
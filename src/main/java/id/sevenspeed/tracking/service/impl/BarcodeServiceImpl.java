package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.request.barcode.GenerateBarcodeRequest;
import id.sevenspeed.tracking.dto.request.barcode.UpdateBarcodeRequest;
import id.sevenspeed.tracking.entity.Barcode;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.BarcodeRepository;
import id.sevenspeed.tracking.service.BarcodeService;
import id.sevenspeed.tracking.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarcodeServiceImpl implements BarcodeService {

    private final BarcodeRepository barcodeRepository;
    private final BatchService batchService;

    @Override
    public Barcode findByCode(String code) {
        return barcodeRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barcode not found or inactive: " + code));
    }

    @Override
    public Barcode findById(Long id) {
        return barcodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barcode", id));
    }

    @Override
    public List<Barcode> findByBatchId(Long batchId) {
        batchService.findById(batchId); // validasi batch exists
        return barcodeRepository.findByBatchId(batchId);
    }

    @Override
    @Transactional
    public Barcode generate(Long batchId, GenerateBarcodeRequest request) {
        OrderBatch batch = batchService.findById(batchId);

        if (Boolean.TRUE.equals(request.getDeactivatePrevious())) {
            deactivateExistingBarcodes(batchId);
        }

        Barcode barcode = new Barcode();
        barcode.setBatch(batch);
        barcode.setCode(generateCode(batch.getBatchNumber()));
        barcode.setBarcodeType(request.getBarcodeType() != null
                ? request.getBarcodeType()
                : "CODE128");
        barcode.setIsActive(true);
        barcode.setNotes(request.getNotes());

        return barcodeRepository.save(barcode);
    }

    @Override
    @Transactional
    public Barcode update(Long id, UpdateBarcodeRequest request) {
        Barcode barcode = findById(id);

        if (request.getIsActive() != null) barcode.setIsActive(request.getIsActive());
        if (request.getPrintedAt() != null) barcode.setPrintedAt(request.getPrintedAt());
        if (request.getNotes() != null) barcode.setNotes(request.getNotes());

        return barcodeRepository.save(barcode);
    }

    private void deactivateExistingBarcodes(Long batchId) {
        List<Barcode> active = barcodeRepository.findByBatchId(batchId)
                .stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .toList();

        active.forEach(b -> b.setIsActive(false));
        barcodeRepository.saveAll(active);
        log.info("Deactivated {} barcode(s) for batchId {}", active.size(), batchId);
    }

    private String generateCode(String batchNumber) {
        String sanitized = batchNumber.replace("-", "");
        long timestamp = System.currentTimeMillis() % 10000;
        return String.format("BC-%s-%04d", sanitized, timestamp);
    }
}
package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.request.barcode.GenerateBarcodeRequest;
import id.sevenspeed.tracking.dto.request.barcode.UpdateBarcodeRequest;
import id.sevenspeed.tracking.dto.response.barcode.BarcodeResolveResponse;
import id.sevenspeed.tracking.dto.response.barcode.BarcodeResponse;
import id.sevenspeed.tracking.dto.response.batch.BatchDetailResponse;
import id.sevenspeed.tracking.entity.Barcode;
import id.sevenspeed.tracking.entity.OrderBatch;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.BarcodeRepository;
import id.sevenspeed.tracking.security.CustomUserDetails;
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
    @Transactional(readOnly = true)
    public BarcodeResponse findByCode(String code) {
        return BarcodeResponse.from(findEntityByCode(code));
    }

    @Override
    @Transactional(readOnly = true)
    public BarcodeResponse findById(Long id) {
        Barcode barcode = barcodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barcode", id));
        return BarcodeResponse.from(barcode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BarcodeResponse> findByBatchId(Long batchId) {
        batchService.findEntityById(batchId); // validasi batch exists
        return barcodeRepository.findByBatchId(batchId)
                .stream()
                .map(BarcodeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BarcodeResolveResponse resolve(String code, CustomUserDetails currentUser) {
        Barcode barcode = findEntityByCode(code);
        OrderBatch batch = barcode.getBatch();

        boolean canScan = resolveCanScan(batch, currentUser);
        String canScanReason = canScan ? null : resolveCanScanReason(batch, currentUser);
        String suggestedEventType = resolveSuggestedEventType(batch);

        return BarcodeResolveResponse.builder()
                .barcode(BarcodeResponse.from(barcode))
                .batch(BatchDetailResponse.from(batch))
                .canScan(canScan)
                .canScanReason(canScanReason)
                .suggestedAction(suggestedEventType != null ? "START_STEP" : "NONE")
                .suggestedEventType(suggestedEventType)
                .build();
    }

    @Override
    @Transactional
    public BarcodeResponse generate(Long batchId, GenerateBarcodeRequest request) {
        OrderBatch batch = batchService.findEntityById(batchId);

        if (Boolean.TRUE.equals(request.getDeactivatePrevious())) {
            deactivateExistingBarcodes(batchId);
        }

        Barcode barcode = new Barcode();
        barcode.setBatch(batch);
        barcode.setCode(generateCode(batch.getBatchNumber()));
        barcode.setBarcodeType(request.getBarcodeType() != null
                ? request.getBarcodeType() : "CODE128");
        barcode.setIsActive(true);
        barcode.setNotes(request.getNotes());

        return BarcodeResponse.from(barcodeRepository.save(barcode));
    }

    @Override
    @Transactional
    public BarcodeResponse update(Long id, UpdateBarcodeRequest request) {
        Barcode barcode = barcodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barcode", id));

        if (request.getIsActive() != null) barcode.setIsActive(request.getIsActive());
        if (request.getPrintedAt() != null) barcode.setPrintedAt(request.getPrintedAt());
        if (request.getNotes() != null) barcode.setNotes(request.getNotes());

        return BarcodeResponse.from(barcodeRepository.save(barcode));
    }

    @Override
    public Barcode findEntityByCode(String code) {
        return barcodeRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barcode not found or inactive: " + code));
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

    private boolean resolveCanScan(OrderBatch batch, CustomUserDetails currentUser) {
        if ("ADMIN".equals(currentUser.getRoleCode())) return true;
        if (batch.getCurrentStep() == null) return false;
        return batch.getCurrentStep().getDivision().getId()
                .equals(currentUser.getDivisionId());
    }

    private String resolveCanScanReason(OrderBatch batch, CustomUserDetails currentUser) {
        if (batch.getCurrentStep() == null) return "Batch has no current step";
        return "This step belongs to division: "
                + batch.getCurrentStep().getDivision().getName();
    }

    private String resolveSuggestedEventType(OrderBatch batch) {
        if (batch.getCurrentStep() == null) return null;
        if ("DRAFT".equals(batch.getStatus())) return "STEP_STARTED";
        if ("IN_PROGRESS".equals(batch.getStatus())) return "STEP_COMPLETED";
        return null;
    }

    private String generateCode(String batchNumber) {
        String sanitized = batchNumber.replace("-", "");
        long timestamp = System.currentTimeMillis() % 10000;
        return String.format("BC-%s-%04d", sanitized, timestamp);
    }
}
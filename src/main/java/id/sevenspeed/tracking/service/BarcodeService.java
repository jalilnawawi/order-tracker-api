package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.request.barcode.GenerateBarcodeRequest;
import id.sevenspeed.tracking.dto.request.barcode.UpdateBarcodeRequest;
import id.sevenspeed.tracking.entity.Barcode;
import id.sevenspeed.tracking.entity.OrderBatch;

import java.util.List;

public interface BarcodeService {

    Barcode findByCode(String code);

    Barcode findById(Long id);

    List<Barcode> findByBatchId(Long batchId);

    Barcode generate(Long batchId, GenerateBarcodeRequest request);

    Barcode update(Long id, UpdateBarcodeRequest request);
}
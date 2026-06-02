package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.request.barcode.GenerateBarcodeRequest;
import id.sevenspeed.tracking.dto.request.barcode.UpdateBarcodeRequest;
import id.sevenspeed.tracking.dto.response.barcode.BarcodeResolveResponse;
import id.sevenspeed.tracking.dto.response.barcode.BarcodeResponse;
import id.sevenspeed.tracking.entity.Barcode;
import id.sevenspeed.tracking.security.CustomUserDetails;

import java.util.List;

public interface BarcodeService {

    BarcodeResponse findByCode(String code);

    BarcodeResponse findById(Long id);

    List<BarcodeResponse> findByBatchId(Long batchId);

    BarcodeResponse generate(Long batchId, GenerateBarcodeRequest request);

    BarcodeResponse update(Long id, UpdateBarcodeRequest request);

    BarcodeResolveResponse resolve(String code, CustomUserDetails currentUser);

    Barcode findEntityByCode(String code); // dipakai internal
}
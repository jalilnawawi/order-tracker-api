package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.request.barcode.GenerateBarcodeRequest;
import id.sevenspeed.tracking.dto.request.barcode.UpdateBarcodeRequest;
import id.sevenspeed.tracking.dto.response.barcode.BarcodeResolveResponse;
import id.sevenspeed.tracking.dto.response.barcode.BarcodeResponse;
import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.security.CustomUserDetails;
import id.sevenspeed.tracking.service.BarcodeService;
import id.sevenspeed.tracking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BarcodeController {

    private final BarcodeService barcodeService;
    private final UserService userService;

    @GetMapping("/barcodes/{code}/resolve")
    public ResponseEntity<ApiResponse<BarcodeResolveResponse>> resolve(
            @PathVariable String code) {
        CustomUserDetails currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.ok(
                barcodeService.resolve(code, currentUser)));
    }

    @GetMapping("/batches/{id}/barcodes")
    public ResponseEntity<ApiResponse<List<BarcodeResponse>>> findByBatchId(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                barcodeService.findByBatchId(id)));
    }

    @PostMapping("/batches/{id}/barcodes")
    public ResponseEntity<ApiResponse<BarcodeResponse>> generate(
            @PathVariable Long id,
            @RequestBody GenerateBarcodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(barcodeService.generate(id, request)));
    }

    @PatchMapping("/barcodes/{id}")
    public ResponseEntity<ApiResponse<BarcodeResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateBarcodeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(barcodeService.update(id, request)));
    }
}
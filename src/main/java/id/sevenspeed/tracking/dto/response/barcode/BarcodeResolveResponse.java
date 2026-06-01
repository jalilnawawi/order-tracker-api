package id.sevenspeed.tracking.dto.response.barcode;

import id.sevenspeed.tracking.dto.response.batch.BatchDetailResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BarcodeResolveResponse {

    private BarcodeResponse barcode;
    private BatchDetailResponse batch;
    private Boolean canScan;
    private String canScanReason;
    private String suggestedAction;
    private String suggestedEventType;
}
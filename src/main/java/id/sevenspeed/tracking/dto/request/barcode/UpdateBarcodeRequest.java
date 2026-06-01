package id.sevenspeed.tracking.dto.request.barcode;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class UpdateBarcodeRequest {

    private Boolean isActive;
    private OffsetDateTime printedAt;
    private String notes;
}
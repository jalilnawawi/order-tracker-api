package id.sevenspeed.tracking.dto.request.barcode;

import lombok.Getter;

@Getter
public class GenerateBarcodeRequest {

    private String barcodeType;
    private String notes;
    private final Boolean deactivatePrevious = true;
}
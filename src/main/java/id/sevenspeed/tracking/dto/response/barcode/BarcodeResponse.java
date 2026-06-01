package id.sevenspeed.tracking.dto.response.barcode;

import id.sevenspeed.tracking.entity.Barcode;
import id.sevenspeed.tracking.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BarcodeResponse {

    private Long id;
    private String code;
    private Long batchId;
    private String barcodeType;
    private Boolean isActive;
    private String printedAt;
    private String notes;
    private String createdAt;

    public static BarcodeResponse from(Barcode barcode) {
        return BarcodeResponse.builder()
                .id(barcode.getId())
                .code(barcode.getCode())
                .batchId(barcode.getBatch().getId())
                .barcodeType(barcode.getBarcodeType())
                .isActive(barcode.getIsActive())
                .printedAt(DateTimeUtil.format(barcode.getPrintedAt()))
                .notes(barcode.getNotes())
                .createdAt(DateTimeUtil.format(barcode.getCreatedAt()))
                .build();
    }
}
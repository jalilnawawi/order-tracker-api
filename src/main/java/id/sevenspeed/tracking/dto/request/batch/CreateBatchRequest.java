package id.sevenspeed.tracking.dto.request.batch;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Map;

@Getter
public class CreateBatchRequest {

    @NotNull(message = "Product type is required")
    private Long productTypeId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    private String unit;

    private Map<String, Object> specifications;

    private String notes;
}
package id.sevenspeed.tracking.dto.request.batch;

import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.util.Map;

@Getter
public class UpdateBatchRequest {

    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    private Map<String, Object> specifications;

    private String status;

    private String notes;
}
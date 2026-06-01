package id.sevenspeed.tracking.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateOrderRequest {

    @NotNull(message = "Customer is required")
    private Long customerId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    private LocalDate deadlineDate;

    private String notes;
}
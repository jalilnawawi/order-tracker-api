package id.sevenspeed.tracking.dto.request.order;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateOrderRequest {

    private String title;
    private String description;
    private String status;
    private LocalDate deadlineDate;
    private String notes;
}
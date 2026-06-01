package id.sevenspeed.tracking.dto.response.order;

import id.sevenspeed.tracking.dto.response.batch.BatchSummaryResponse;
import id.sevenspeed.tracking.dto.response.common.UserSummaryResponse;
import id.sevenspeed.tracking.entity.Order;
import id.sevenspeed.tracking.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderDetailResponse {

    private Long id;
    private String orderNumber;
    private UserSummaryResponse customer;
    private String title;
    private String description;
    private String status;
    private String orderDate;
    private String deadlineDate;
    private String completedAt;
    private String notes;
    private UserSummaryResponse createdBy;
    private String createdAt;
    private String updatedAt;
    private List<BatchSummaryResponse> batches;

    public static OrderDetailResponse from(Order order, List<BatchSummaryResponse> batches) {
        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customer(UserSummaryResponse.from(order.getCustomer()))
                .title(order.getTitle())
                .description(order.getDescription())
                .status(order.getStatus())
                .orderDate(order.getOrderDate() != null
                        ? order.getOrderDate().toString() : null)
                .deadlineDate(order.getDeadlineDate() != null
                        ? order.getDeadlineDate().toString() : null)
                .completedAt(DateTimeUtil.format(order.getCompletedAt()))
                .notes(order.getNotes())
                .createdBy(UserSummaryResponse.from(order.getCreatedBy()))
                .createdAt(DateTimeUtil.format(order.getCreatedAt()))
                .updatedAt(DateTimeUtil.format(order.getUpdatedAt()))
                .batches(batches)
                .build();
    }
}
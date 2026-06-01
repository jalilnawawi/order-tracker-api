package id.sevenspeed.tracking.dto.response.order;

import id.sevenspeed.tracking.entity.Order;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderListItemResponse {

    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerInstitutionName;
    private String title;
    private String status;
    private String orderDate;
    private String deadlineDate;
    private Integer batchCount;

    public static OrderListItemResponse from(Order order, Integer batchCount) {
        return OrderListItemResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer().getFullName())
                .customerInstitutionName(order.getCustomer().getInstitutionName())
                .title(order.getTitle())
                .status(order.getStatus())
                .orderDate(order.getOrderDate() != null
                        ? order.getOrderDate().toString() : null)
                .deadlineDate(order.getDeadlineDate() != null
                        ? order.getDeadlineDate().toString() : null)
                .batchCount(batchCount)
                .build();
    }
}
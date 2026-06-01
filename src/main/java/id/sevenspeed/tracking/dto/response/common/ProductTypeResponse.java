package id.sevenspeed.tracking.dto.response.common;

import id.sevenspeed.tracking.entity.ProductType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductTypeResponse {

    private Long id;
    private String code;
    private String name;
    private Long workflowId;
    private String description;
    private Boolean isActive;

    public static ProductTypeResponse from(ProductType productType) {
        return ProductTypeResponse.builder()
                .id(productType.getId())
                .code(productType.getCode())
                .name(productType.getName())
                .workflowId(productType.getWorkflow().getId())
                .description(productType.getDescription())
                .isActive(productType.getIsActive())
                .build();
    }
}
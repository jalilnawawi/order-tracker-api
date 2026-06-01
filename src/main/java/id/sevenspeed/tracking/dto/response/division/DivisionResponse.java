package id.sevenspeed.tracking.dto.response.division;

import id.sevenspeed.tracking.entity.Division;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DivisionResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;

    public static DivisionResponse from(Division division) {
        return DivisionResponse.builder()
                .id(division.getId())
                .code(division.getCode())
                .name(division.getName())
                .description(division.getDescription())
                .isActive(division.getIsActive())
                .build();
    }
}
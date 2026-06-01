package id.sevenspeed.tracking.dto.response.common;

import id.sevenspeed.tracking.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryResponse {

    private Long id;
    private String username;
    private String fullName;
    private String roleCode;
    private String divisionCode;

    public static UserSummaryResponse from(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roleCode(user.getRole().getCode())
                .divisionCode(user.getDivision() != null ? user.getDivision().getCode() : null)
                .build();
    }
}
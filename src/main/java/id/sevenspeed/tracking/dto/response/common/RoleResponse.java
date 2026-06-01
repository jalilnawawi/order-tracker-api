package id.sevenspeed.tracking.dto.response.common;

import id.sevenspeed.tracking.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleResponse {

    private Long id;
    private String code;
    private String name;

    public static RoleResponse from(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .build();
    }
}
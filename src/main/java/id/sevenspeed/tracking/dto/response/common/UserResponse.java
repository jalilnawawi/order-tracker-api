// dto/response/UserResponse.java
package id.sevenspeed.tracking.dto.response.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String roleCode;
    private Long divisionId;
    private String divisionCode;
    private String divisionName;
    private String customerType;
    private String institutionName;
    private Boolean isActive;
    private String lastLoginAt;
    private String createdAt;
}
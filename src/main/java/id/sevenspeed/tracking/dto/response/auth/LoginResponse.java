package id.sevenspeed.tracking.dto.response.auth;

import id.sevenspeed.tracking.dto.response.common.UserResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;      // TTL dalam detik, bukan timestamp
    private String tokenType;    // selalu "Bearer"
    private UserResponse user;
}
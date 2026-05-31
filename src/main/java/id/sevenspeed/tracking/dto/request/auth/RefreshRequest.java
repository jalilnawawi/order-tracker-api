package id.sevenspeed.tracking.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
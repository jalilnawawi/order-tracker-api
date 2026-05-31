// service/AuthService.java
package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.request.auth.LoginRequest;
import id.sevenspeed.tracking.dto.request.auth.RefreshRequest;
import id.sevenspeed.tracking.dto.response.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
    LoginResponse refresh(RefreshRequest request);
    void logout();
}
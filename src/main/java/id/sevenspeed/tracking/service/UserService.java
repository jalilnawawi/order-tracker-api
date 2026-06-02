package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.response.common.UserResponse;
import id.sevenspeed.tracking.entity.User;
import id.sevenspeed.tracking.security.CustomUserDetails;

public interface UserService {

    CustomUserDetails getCurrentUser();

    User getCurrentUserEntity();

    User getUserById(Long id);

    UserResponse getCurrentUserResponse();
}
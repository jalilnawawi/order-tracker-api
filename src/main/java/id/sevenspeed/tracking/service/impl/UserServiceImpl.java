package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.entity.User;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.UserRepository;
import id.sevenspeed.tracking.security.CustomUserDetails;
import id.sevenspeed.tracking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserDetails) authentication.getPrincipal();
    }

    @Override
    public User getCurrentUserEntity() {
        Long userId = getCurrentUser().getUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
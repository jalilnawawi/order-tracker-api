package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.response.common.UserResponse;
import id.sevenspeed.tracking.entity.User;
import id.sevenspeed.tracking.exception.ResourceNotFoundException;
import id.sevenspeed.tracking.repository.UserRepository;
import id.sevenspeed.tracking.security.CustomUserDetails;
import id.sevenspeed.tracking.service.UserService;
import id.sevenspeed.tracking.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        Long userId = getCurrentUser().getUserId();
        return userRepository.findByIdWithRoleAndDivision(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserResponse() {
        User user = getCurrentUserEntity();
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roleCode(user.getRole().getCode())
                .divisionId(user.getDivision() != null ? user.getDivision().getId() : null)
                .divisionCode(user.getDivision() != null ? user.getDivision().getCode() : null)
                .divisionName(user.getDivision() != null ? user.getDivision().getName() : null)
                .customerType(user.getCustomerType())
                .institutionName(user.getInstitutionName())
                .isActive(user.getIsActive())
                .lastLoginAt(DateTimeUtil.format(user.getLastLoginAt()))
                .createdAt(DateTimeUtil.format(user.getCreatedAt()))
                .build();
    }
}
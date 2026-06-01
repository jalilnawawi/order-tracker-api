package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.dto.response.common.ProductTypeResponse;
import id.sevenspeed.tracking.dto.response.common.RoleResponse;
import id.sevenspeed.tracking.dto.response.common.UserSummaryResponse;
import id.sevenspeed.tracking.entity.ProductType;
import id.sevenspeed.tracking.entity.Role;
import id.sevenspeed.tracking.repository.ProductTypeRepository;
import id.sevenspeed.tracking.repository.RoleRepository;
import id.sevenspeed.tracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final ProductTypeRepository productTypeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @GetMapping("/product-types")
    public ResponseEntity<ApiResponse<List<ProductTypeResponse>>> findProductTypes() {
        return ResponseEntity.ok(ApiResponse.ok(
                productTypeRepository.findAll().stream()
                        .map(ProductTypeResponse::from)
                        .toList()));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> findRoles() {
        return ResponseEntity.ok(ApiResponse.ok(
                roleRepository.findAll().stream()
                        .map(RoleResponse::from)
                        .toList()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> findUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.paginated(
                userRepository.findWithFilters(role, divisionId, q, pageable)
                        .map(UserSummaryResponse::from)));
    }
}
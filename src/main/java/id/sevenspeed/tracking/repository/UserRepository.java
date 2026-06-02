package id.sevenspeed.tracking.repository;

import id.sevenspeed.tracking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    @Query("""
        SELECT u FROM User u
        WHERE (:roleCode IS NULL OR u.role.code = :roleCode)
        AND (:divisionId IS NULL OR u.division.id = :divisionId)
        AND (:q IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<User> findWithFilters(
            @Param("roleCode") String roleCode,
            @Param("divisionId") Long divisionId,
            @Param("q") String q,
            Pageable pageable);

    @Query("""
        SELECT u FROM User u
        LEFT JOIN FETCH u.role
        LEFT JOIN FETCH u.division
        WHERE u.id = :id
        """)
    Optional<User> findByIdWithRoleAndDivision(@Param("id") Long id);
}
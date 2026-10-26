package com.sahrin.spas.repository;

import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdentifier(String identifier);

    boolean existsByEmail(String email);

    boolean existsByIdentifier(String identifier);

    boolean existsByRole(Role role);

    List<User> findByStatusOrderByIdAsc(User.UserStatus status);

    List<User> findByRoleAndStatusOrderByFullNameAsc(Role role, User.UserStatus status);

    long countByRoleAndStatus(Role role, User.UserStatus status);

    long countByStatus(User.UserStatus status);

    List<User> findAllByOrderByIdAsc();

    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.identifier) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY u.id ASC")
    List<User> searchUsers(
            @Param("role") Role role,
            @Param("status") User.UserStatus status,
            @Param("search") String search
    );

    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.identifier) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY u.id ASC")
    org.springframework.data.domain.Page<User> searchUsersPaged(
            @Param("role") Role role,
            @Param("status") User.UserStatus status,
            @Param("search") String search,
            org.springframework.data.domain.Pageable pageable
    );

    Optional<User> findByIdentifierIgnoreCaseAndEmailIgnoreCase(String identifier, String email);
}

package com.sahrin.spas.repository;

import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Optional<User> findByIdentifierIgnoreCaseAndEmailIgnoreCase(String identifier, String email);
}

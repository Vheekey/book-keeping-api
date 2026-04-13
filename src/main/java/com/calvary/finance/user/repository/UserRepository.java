package com.calvary.finance.user.repository;

import com.calvary.finance.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByRoleId(Long roleId);

    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.name) LIKE CONCAT('%', :search, '%')
               OR LOWER(u.email) LIKE CONCAT('%', :search, '%')
               OR EXISTS (
                    SELECT 1 FROM UserAuthProvider authProvider
                    WHERE authProvider.userId = u.id
                      AND LOWER(authProvider.username) LIKE CONCAT('%', :search, '%')
               )
            """)
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
}

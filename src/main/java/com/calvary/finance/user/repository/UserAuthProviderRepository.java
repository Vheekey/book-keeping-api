package com.calvary.finance.user.repository;

import com.calvary.finance.user.entity.UserAuthProvider;
import com.calvary.finance.user.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {
    boolean existsUserAuthProviderByUsername(String username);

    boolean existsByProviderAndUsername(AuthProvider provider, String username);

    Optional<UserAuthProvider> findByUserIdAndProvider(Long id, AuthProvider authProvider);

    Optional<UserAuthProvider> findByProviderAndUsername(AuthProvider authProvider, String username);
}

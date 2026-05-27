package com.collide.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.enums.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppUserPrincipalTest {

    @Test
    void principalExposesUserDetailsAndAuthorities() {
        UUID id = UUID.randomUUID();
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("admin");
        user.setPasswordHash("secret-hash");
        user.setRole(UserRole.ADMIN);
        user.setEnabled(false);

        AppUserPrincipal principal = new AppUserPrincipal(user);

        assertThat(principal.getId()).isEqualTo(id);
        assertThat(principal.getUsername()).isEqualTo("admin");
        assertThat(principal.getPassword()).isEqualTo("secret-hash");
        assertThat(principal.isEnabled()).isFalse();
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.getAuthorities()).hasSize(1);
        assertThat(principal.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }
}

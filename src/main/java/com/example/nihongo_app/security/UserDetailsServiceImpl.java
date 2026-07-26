package com.example.nihongo_app.security;

import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService implementation that loads users from the database by email.
 *
 * <p>This bean is required so that Spring Security does NOT auto-create the default
 * {@code inMemoryUserDetailsManager} (which prints a warning and uses a random password).
 * Although the {@link JwtAuthenticationFilter} sets {@code Authentication} directly via
 * the SecurityContextHolder, providing a {@code UserDetailsService} keeps Spring Boot's
 * default autoconfiguration happy and gives us a clean integration point for future
 * Spring Security features (e.g. {@code AuthenticationManagerBuilder}, DaoAuthenticationProvider).</p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String authority = "ROLE_" + (user.getRole() == null ? "" : user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash() == null ? "" : user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
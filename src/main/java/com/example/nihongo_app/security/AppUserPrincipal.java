package com.example.nihongo_app.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Custom principal mang thêm {@code userId} để controller/service lấy trực tiếp
 * từ {@link org.springframework.security.core.Authentication#getPrincipal()} mà
 * không cần gọi thêm {@code UserRepository.findByEmail(...)}.
 *
 * <p>Việc này tránh thêm 1 query DB cho mỗi request đã xác thực, đặc biệt quan trọng
 * cho các API "nặng" như GET /api/v1/topics vốn tốn nhiều query khác.</p>
 */
public class AppUserPrincipal extends User {

    private final Long userId;

    public AppUserPrincipal(Long userId,
                            String username,
                            String password,
                            Collection<? extends GrantedAuthority> authorities) {
        super(username, password == null ? "" : password, authorities);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}

package app.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    @Getter
    private final Long id;
    private final String email;
    private final String username;
    private final String role;
    private final String password; // Có thể để null nếu không sử dụng

    public CustomUserDetails(Long id, String email, String username, String role, String password) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password; // Có thể trả về null nếu không cần
    }

    @Override
    public String getUsername() {
        return email; // Sử dụng email làm username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
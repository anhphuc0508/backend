// File: com/project/btl/model/entity/User.java
package com.project.btl.model.entity;

// (Giữ các import cũ)
import jakarta.persistence.FetchType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Users")
public class User implements UserDetails { // Bổ sung "implements UserDetails"

    // ... (Giữ nguyên các trường cũ: userId, firstName, ...)
    // ... (Giữ nguyên passwordHash, createdAt, updatedAt)

    // --- Mối quan hệ ---

    // Cần đổi FetchType thành EAGER để Security lấy được Role
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    // ... (Giữ nguyên các mối quan hệ khác)

    // --- Triển khai các phương thức của UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Trả về quyền của user, Spring Security sẽ tự thêm tiền tố "ROLE_"
        // nên trong DB bạn chỉ cần lưu "ADMIN" hoặc "USER"
        return List.of(new SimpleGrantedAuthority(role.getRoleName()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash; // Trả về cột mật khẩu đã mã hóa
    }

    @Override
    public String getUsername() {
        return this.email; // Dùng email làm username
    }

    // Các phương thức này để kiểm tra tài khoản (tạm thời để true)
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
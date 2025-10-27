package com.project.btl.controller;

import com.project.btl.model.ERole;
import com.project.btl.model.Role;
import com.project.btl.model.User;
import com.project.btl.payload.request.LoginRequest;
import com.project.btl.payload.request.SignupRequest;
import com.project.btl.payload.response.JwtResponse;
import com.project.btl.payload.response.MessageResponse;
import com.project.btl.repository.RoleRepository;
import com.project.btl.repository.UserRepository;
import com.project.btl.security.jwt.JwtUtils;
import com.project.btl.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // --- Sử dụng Constructor Injection ---
    final AuthenticationManager authenticationManager;
    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final PasswordEncoder encoder;
    final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder encoder,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    // --- LOGIC ĐĂNG NHẬP CHUẨN (DÙNG AuthenticationManager) ---
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Bước 1: Gọi AuthenticationManager để xác thực
        // Nó sẽ tự động gọi UserDetailsService và PasswordEncoder để kiểm tra
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // Bước 2: Nếu xác thực thành công, lưu vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Bước 3: Tạo JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Bước 4: Lấy thông tin user
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Bước 5: Trả về token và thông tin user
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }
    // --- KẾT THÚC LOGIC ĐĂNG NHẬP CHUẨN ---


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()), // Mã hóa mật khẩu khi đăng ký
                signUpRequest.getFullName(),
                signUpRequest.getAddress());

        Set<String> strRoles = signUpRequest.getRole(); // Dòng này bị bỏ qua do logic bên dưới
        Set<Role> roles = new HashSet<>();

        // --- LOGIC GÁN QUYỀN TỰ ĐỘNG ---
        if (signUpRequest.getEmail() != null && signUpRequest.getEmail().endsWith("@gymstore.admin")) {
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
            roles.add(adminRole);
        } else {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
            roles.add(userRole);
        }
        // --- KẾT THÚC LOGIC GÁN QUYỀN ---

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}


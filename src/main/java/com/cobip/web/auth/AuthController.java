package com.cobip.web.auth;

import com.cobip.domain.user.UserService;
import com.cobip.dto.auth.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    // 회원가입 API
    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest req) {
        userService.signup(req);
        return new AuthResponse("회원가입 완료");
    }

    // 로그인 API
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        userService.login(req);
        return new AuthResponse("로그인 성공");
    }
}

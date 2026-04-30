package com.cobip.domain.user;

import com.cobip.dto.auth.LoginRequest;
import com.cobip.dto.auth.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    // 비밀번호 암호화/검증 담당

    // 회원가입
    public void signup(SignupRequest req) {

        // 1. 비밀번호 확인
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        // 2. 이메일 중복 체크
        userRepository.findByEmail(req.getEmail())
                .ifPresent(u -> {
                    throw new RuntimeException("이미 존재하는 이메일");
                });

        // 3. 비밀번호 암호화 후 저장
        userRepository.save(User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .build());
    }

    // 로그인
    public User login(LoginRequest req) {

        // 1. 이메일로 유저 조회
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호 틀림");
        }

        return user;
    }
}

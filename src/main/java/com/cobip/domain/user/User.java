package com.cobip.domain.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // PostgreSQL에서 user는 예약어라 users로 변경
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {


    // 이메일은 중복 불가 + 필수값
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String nickname;
}
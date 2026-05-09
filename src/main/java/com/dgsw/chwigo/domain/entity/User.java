package com.dgsw.chwigo.domain.entity;

import com.dgsw.chwigo.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

@BatchSize(size = 100)
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(length = 512)
    private String refreshToken;

    @Builder
    public User(String email, String password, String nickname, String address, UserRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.address = address;
        this.role = role;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateProfile(String nickname, String address) {
        this.nickname = nickname;
        this.address = address;
    }
}

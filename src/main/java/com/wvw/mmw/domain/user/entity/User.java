package com.wvw.mmw.domain.user.entity;

import com.wvw.mmw.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_login_id",
                        columnNames = "login_id"
                ),
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "login_id",
            nullable = false,
            length = 50,
            updatable = false
    )
    private String loginId;

    @Column(
            nullable = false,
            length = 255,
            updatable = false
    )
    private String email;

    @Column(
            nullable = false,
            length = 255
    )
    private String password;

    @Column(
            nullable = false,
            length = 50,
            updatable = false
    )
    private String name;

    @Column(length = 50)
    private String nickname;

    @Column(name = "desired_position", length = 100)
    private String desiredPosition;

    @Builder
    private User(
            String loginId,
            String email,
            String password,
            String name
    ) {
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public static User create(
            String loginId,
            String email,
            String password,
            String name
    ) {
        return new User(loginId, email, password, name);
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(
            String nickname,
            String desiredPosition
    ) {
        if (nickname != null) {
            this.nickname = nickname;
        }

        if (desiredPosition != null) {
            this.desiredPosition = desiredPosition;
        }
    }
}

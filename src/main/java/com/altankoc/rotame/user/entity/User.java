package com.altankoc.rotame.user.entity;

import com.altankoc.rotame.core.base.BaseEntity;
import com.altankoc.rotame.core.enums.AuthProvider;
import com.altankoc.rotame.core.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email", name = "uk_user_email"),
                @UniqueConstraint(columnNames = "username", name = "uk_user_username")
        },
        indexes = {
                @Index(columnList = "email", name = "idx_user_email"),
                @Index(columnList = "username", name = "idx_user_username")
        }
)
@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @NotBlank(message = "Ad alanı boş bırakılamaz!")
    @Size(min = 2, max = 30, message = "Ad 2 ile 30 karakter arasında olmalıdır!")
    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @NotBlank(message = "Ad alanı boş bırakılamaz!")
    @Size(min = 2, max = 30, message = "Ad 2 ile 30 karakter arasında olmalıdır!")
    @Column(name = "last_name", nullable = false, length = 30)
    private String lastName;

    @NotBlank(message = "Kullanıcı adı boş bırakılamaz!")
    @Size(min = 3, max = 20, message = "Kullanıcı adı 3 ile 20 karakter arasında olmalıdır!")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Kullanıcı adı yalnızca harf, rakam ve alt çizgi içerebilir!")
    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "password_hash")
    private String password;

    @NotBlank(message = "E-posta alanı boş bırakılamaz!")
    @Email(message = "Geçerli bir e-posta adresi giriniz!")
    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

}

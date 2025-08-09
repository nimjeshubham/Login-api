package com.possessor.loginapi.entity;

import com.possessor.loginapi.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean emailVerified = false;
    private String verificationToken;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<UserRole> roles;
}
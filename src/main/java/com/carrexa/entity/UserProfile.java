package com.carrexa.entity;


import com.carrexa.dto.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "UserProfile")
public class UserProfile {

    @Id
    private String userId;
    private String email;
    private UserRole role;
    private String firstName;
    private String lastName;
    private String userName;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private boolean verified = false;


}

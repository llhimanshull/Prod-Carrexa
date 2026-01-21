package com.carrexa.dto.request;

import com.carrexa.dto.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterRequest {

    private String userName;
    private String password;
    private String email;
    private String firstName;
    private String lastName;

    @Builder.Default
    private UserRole userRole = UserRole.USER;

}

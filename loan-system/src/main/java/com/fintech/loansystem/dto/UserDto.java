package com.fintech.loansystem.dto;

import com.fintech.loansystem.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String password;
    @Builder.Default
    private Role role = Role.USER;

}

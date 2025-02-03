package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.UserDto;
import com.fintech.loansystem.mapper.DtoMapper;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;

    public UserDto register(UserDto userDto) {

        String hashedPassword = passwordEncoder.encode(userDto.getPassword());
        User user = User.builder()
                .username(userDto.getUsername())
                .password(hashedPassword)
                .role(userDto.getRole())
                .build();

        user = userRepository.save(user);

        return dtoMapper.userToUserDto(user);
    }
}

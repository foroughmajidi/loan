package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.UserDto;
import com.fintech.loansystem.exception.CustomUniqueConstraintViolationException;
import com.fintech.loansystem.mapper.DtoMapper;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;

    public UserDto register(UserDto userDto) {
        if (userDto.getUsername() == null ||
                userDto.getUsername().isEmpty() ||
                userDto.getPassword() == null ||
                userDto.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Username and password must not be empty.");
        }
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());
        User user = User.builder()
                .username(userDto.getUsername())
                .password(hashedPassword)
                .role(userDto.getRole())
                .build();
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new CustomUniqueConstraintViolationException("Username already exists.");
        }

        return dtoMapper.userToUserDto(user);
    }
}

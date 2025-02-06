package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.UserDto;
import com.fintech.loansystem.enums.Role;
import com.fintech.loansystem.mapper.DtoMapper;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private UserService userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "testuser", "password123", Role.USER);
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("hashedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void registerShouldRegisterUserSuccessfully() {
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(dtoMapper.userToUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.register(userDto);

        assertNotNull(result);
        assertEquals(userDto.getUsername(), result.getUsername());
        assertEquals(userDto.getRole(), result.getRole());
        verify(passwordEncoder).encode(userDto.getPassword());
        verify(userRepository).save(any(User.class));
        verify(dtoMapper).userToUserDto(any(User.class));
    }

    @Test
    void registerShouldHashPassword() {
        String plainPassword = "password123";
        String hashedPassword = "hashedPassword";
        when(passwordEncoder.encode(plainPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(dtoMapper.userToUserDto(any(User.class))).thenReturn(userDto);

        userService.register(userDto);

        verify(passwordEncoder).encode(plainPassword);
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals(hashedPassword)
        ));
    }

    @Test
    void registerShouldThrowExceptionWhenSavingUserFails() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.register(userDto));
        verify(passwordEncoder).encode(userDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldSetCorrectRole() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(dtoMapper.userToUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.register(userDto);

        assertEquals(Role.USER, result.getRole());
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getRole() == Role.USER
        ));
    }
}

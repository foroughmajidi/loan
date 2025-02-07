package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.UserDto;
import com.fintech.loansystem.enums.Role;
import com.fintech.loansystem.exception.CustomUniqueConstraintViolationException;
import com.fintech.loansystem.mapper.DtoMapper;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private UserDto validUserDto;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validUserDto = new UserDto();
        validUserDto.setUsername("testuser");
        validUserDto.setPassword("password123");
        validUserDto.setRole(Role.USER);

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setPassword("hashedPassword");
        savedUser.setRole(Role.USER);
    }

    @Test
    void registerValidUserReturnsUserDto() {
        when(passwordEncoder.encode(validUserDto.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(dtoMapper.userToUserDto(savedUser)).thenReturn(validUserDto);

        UserDto result = userService.register(validUserDto);

        assertNotNull(result);
        assertEquals(validUserDto.getUsername(), result.getUsername());
        assertEquals(validUserDto.getRole(), result.getRole());
        verify(passwordEncoder).encode(validUserDto.getPassword());
        verify(userRepository).save(any(User.class));
        verify(dtoMapper).userToUserDto(savedUser);
    }

    @Test
    void registerDuplicateUsernameThrowsCustomUniqueConstraintViolationException() {
        when(passwordEncoder.encode(validUserDto.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThrows(CustomUniqueConstraintViolationException.class, () -> userService.register(validUserDto));
        verify(passwordEncoder).encode(validUserDto.getPassword());
        verify(userRepository).save(any(User.class));
        verifyNoInteractions(dtoMapper);
    }

    @Test
    void registerEmptyUsernameThrowsIllegalArgumentException() {
        validUserDto.setUsername("");

        assertThrows(IllegalArgumentException.class, () -> userService.register(validUserDto));
        verifyNoInteractions(passwordEncoder, userRepository, dtoMapper);
    }

    @Test
    void registerEmptyPasswordThrowsIllegalArgumentException() {
        validUserDto.setPassword("");

        assertThrows(IllegalArgumentException.class, () -> userService.register(validUserDto));
        verifyNoInteractions(passwordEncoder, userRepository, dtoMapper);
    }

    @Test
    void registerNullUsernameThrowsIllegalArgumentException() {
        validUserDto.setUsername(null);

        assertThrows(IllegalArgumentException.class, () -> userService.register(validUserDto));
        verifyNoInteractions(passwordEncoder, userRepository, dtoMapper);
    }

    @Test
    void registerNullPasswordThrowsIllegalArgumentException() {
        validUserDto.setPassword(null);

        assertThrows(IllegalArgumentException.class, () -> userService.register(validUserDto));
        verifyNoInteractions(passwordEncoder, userRepository, dtoMapper);
    }
}

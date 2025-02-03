package com.fintech.loansystem.controller;

import com.fintech.loansystem.dto.AuthenticationRequestDto;
import com.fintech.loansystem.dto.AuthenticationResponseDto;
import com.fintech.loansystem.dto.UserDto;
import com.fintech.loansystem.service.AuthenticationService;
import com.fintech.loansystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")


public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "register user and hash the Password ")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(userDto));

    }

    @Operation(summary = "Authenticate user and generate JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthenticationRequestDto requestDto) throws AuthenticationException {
        AuthenticationResponseDto authenticate = authenticationService.authenticate(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(authenticate);

    }


}

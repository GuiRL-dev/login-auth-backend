package com.rldev.login_auth_api.services;

import com.rldev.login_auth_api.domain.user.UserEntity;
import com.rldev.login_auth_api.dto.LoginRequestDTO;
import com.rldev.login_auth_api.dto.RegisterRequestDTO;
import com.rldev.login_auth_api.dto.ResponseDTO;
import com.rldev.login_auth_api.infra.security.TokenService;
import com.rldev.login_auth_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public ResponseEntity loginUser(LoginRequestDTO body){
        UserEntity userEntity = this.repository.findByEmail(body.email()).orElseThrow(() -> new RuntimeException("User not found"));
        if(passwordEncoder.matches(body.password(), userEntity.getPassword())){
            String token = this.tokenService.generateToken(userEntity);
            return ResponseEntity.ok(new ResponseDTO(userEntity.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity registerUser(RegisterRequestDTO body){
        Optional<UserEntity> user = this.repository.findByEmail(body.email());

        if(user.isEmpty()){
            UserEntity newUserEntity = new UserEntity();
            newUserEntity.setPassword(passwordEncoder.encode(body.password()));
            newUserEntity.setEmail(body.email());
            newUserEntity.setName(body.name());
            this.repository.save(newUserEntity);

            String token = this.tokenService.generateToken(newUserEntity);
            return ResponseEntity.ok(new ResponseDTO(newUserEntity.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }
}

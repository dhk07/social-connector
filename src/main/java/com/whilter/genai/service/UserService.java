package com.whilter.genai.service;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.whilter.genai.dto.UserDto;
import com.whilter.genai.entity.User;
import com.whilter.genai.repository.UserRepository;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(UserDto userDto) {
        logger.debug("Registering new user with username: {}", userDto.getUsername());
        
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            logger.warn("Username already exists: {}", userDto.getUsername());
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            logger.warn("Email already exists: {}", userDto.getEmail());
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setMobileNumber(userDto.getMobileNumber());
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setDateOfBirth(userDto.getDateOfBirth());

        userRepository.save(user);
        logger.info("Successfully registered user: {}", user.getUsername());
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("User not found with username: {}", username);
                return new RuntimeException("User not found");
            });
    }

    @Transactional
    public User findByEmailOrCreate(String email, Supplier<UserDto> userDtoSupplier) {
        return userRepository.findByEmail(email)
            .orElseGet(() -> {
                logger.info("Creating new user for email: {}", email);
                UserDto userDto = userDtoSupplier.get();
                try {
                    registerUser(userDto);
                    return userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found after creation"));
                } catch (Exception e) {
                    logger.error("Failed to create user: {}", e.getMessage());
                    throw new RuntimeException("Failed to create user", e);
                }
            });
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
} 
package com.whilter.genai.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.whilter.genai.entity.SocialConnection;
import com.whilter.genai.entity.User;

public interface SocialConnectionRepository extends JpaRepository<SocialConnection, Long> {
    Optional<SocialConnection> findByUserAndProvider(User user, String provider);
    Optional<SocialConnection> findByUserIdAndProvider(Long userId, String provider);
    List<SocialConnection> findByUser(User user);
} 
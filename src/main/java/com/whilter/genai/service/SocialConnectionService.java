package com.whilter.genai.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.whilter.genai.entity.SocialConnection;
import com.whilter.genai.entity.User;
import com.whilter.genai.repository.SocialConnectionRepository;
import com.whilter.genai.dto.SocialConnectionDto;

@Service
public class SocialConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(SocialConnectionService.class);
    
    private final SocialConnectionRepository socialConnectionRepository;

    public SocialConnectionService(SocialConnectionRepository socialConnectionRepository) {
        this.socialConnectionRepository = socialConnectionRepository;
    }

    @Transactional
    public void saveSocialConnection(User user, String provider, String providerUserId,
                                   String accessToken, String refreshToken, 
                                   LocalDateTime tokenExpiry, String username) {
        SocialConnection connection = socialConnectionRepository
            .findByUserAndProvider(user, provider)
            .orElse(new SocialConnection());

        connection.setUser(user);
        connection.setProvider(provider);
        connection.setProviderUserId(providerUserId);
        connection.setAccessToken(accessToken);
        connection.setRefreshToken(refreshToken);
        connection.setTokenExpiry(tokenExpiry);
        connection.setUsername(username);
        connection.setConnected(true);

        socialConnectionRepository.save(connection);
    }

    @Transactional
    public void refreshToken(Long userId, String provider, String newAccessToken, 
                           String newRefreshToken, LocalDateTime newExpiry) {
        socialConnectionRepository.findByUserIdAndProvider(userId, provider)
            .ifPresent(connection -> {
                connection.setAccessToken(newAccessToken);
                connection.setRefreshToken(newRefreshToken);
                connection.setTokenExpiry(newExpiry);
                socialConnectionRepository.save(connection);
            });
    }

    public List<SocialConnection> getUserConnections(User user) {
        logger.debug("Fetching social connections for user: {}", user.getUsername());
        return socialConnectionRepository.findByUser(user);
    }

    public List<SocialConnectionDto> getFormattedUserConnections(User user) {
        logger.debug("Getting formatted social connections for user: {}", user.getUsername());
        
        List<SocialConnection> connections = socialConnectionRepository.findByUser(user);
        Map<String, SocialConnection> connectionMap = connections.stream()
            .collect(Collectors.toMap(SocialConnection::getProvider, c -> c));

        List<SocialConnectionDto> formattedConnections = new ArrayList<>();
        
        // Add Google
        formattedConnections.add(createConnectionDto("google", "Google", 
            connectionMap.get("google")));
        
        // Add Facebook
        formattedConnections.add(createConnectionDto("facebook", "Facebook", 
            connectionMap.get("facebook")));
        
        // Add LinkedIn
        formattedConnections.add(createConnectionDto("linkedin", "LinkedIn", 
            connectionMap.get("linkedin")));

        return formattedConnections;
    }

    private SocialConnectionDto createConnectionDto(String provider, String providerName, 
                                                  SocialConnection connection) {
        SocialConnectionDto dto = new SocialConnectionDto();
        dto.setProvider(provider);
        dto.setProviderName(providerName);
        
        if (connection != null && connection.isConnected()) {
            dto.setConnected(true);
            dto.setUsername(connection.getUsername());
        } else {
            dto.setConnected(false);
        }
        
        return dto;
    }
} 
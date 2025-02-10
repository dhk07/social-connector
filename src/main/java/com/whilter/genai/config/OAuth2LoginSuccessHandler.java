package com.whilter.genai.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.whilter.genai.dto.UserDto;
import com.whilter.genai.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    
    private final UserService userService;

    public OAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
        setDefaultTargetUrl("/dashboard");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = token.getPrincipal();
        
        String provider = token.getAuthorizedClientRegistrationId();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        logger.debug("OAuth2 login successful for provider: {} with email: {}", provider, email);

        try {
            userService.findByEmailOrCreate(email, () -> {
                UserDto userDto = new UserDto();
                userDto.setEmail(email);
                userDto.setUsername(email);
                String[] names = (name != null ? name : email).split(" ");
                userDto.setFirstName(names[0]);
                userDto.setLastName(names.length > 1 ? names[1] : "");
                userDto.setPassword(generateRandomPassword());
                return userDto;
            });
        } catch (Exception e) {
            logger.error("Error during OAuth2 success handling: {}", e.getMessage(), e);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String generateRandomPassword() {
        return java.util.UUID.randomUUID().toString();
    }
} 
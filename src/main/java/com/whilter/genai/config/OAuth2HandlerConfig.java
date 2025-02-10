package com.whilter.genai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.whilter.genai.service.UserService;

@Configuration
public class OAuth2HandlerConfig {
    
    @Bean
    public AuthenticationSuccessHandler oauth2LoginSuccessHandler(UserService userService) {
        return new OAuth2LoginSuccessHandler(userService);
    }
} 
package com.whilter.genai.controller;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.whilter.genai.entity.User;
import com.whilter.genai.service.SocialConnectionService;
import com.whilter.genai.service.UserService;

@Controller
@RequestMapping("/oauth2/callback")
public class OAuth2CallbackController {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2CallbackController.class);

    private final OAuth2AuthorizedClientService clientService;
    private final SocialConnectionService socialConnectionService;
    private final UserService userService;

    public OAuth2CallbackController(OAuth2AuthorizedClientService clientService,
                                  SocialConnectionService socialConnectionService,
                                  UserService userService) {
        this.clientService = clientService;
        this.socialConnectionService = socialConnectionService;
        this.userService = userService;
    }

    @GetMapping("/google")
    public String handleGoogleCallback(Authentication authentication, RedirectAttributes attributes) {
        return handleOAuth2Callback("google", authentication, attributes);
    }

    @GetMapping("/facebook")
    public String handleFacebookCallback(Authentication authentication, RedirectAttributes attributes) {
        return handleOAuth2Callback("facebook", authentication, attributes);
    }

    @GetMapping("/linkedin")
    public String handleLinkedInCallback(Authentication authentication, RedirectAttributes attributes) {
        return handleOAuth2Callback("linkedin", authentication, attributes);
    }

    private String handleOAuth2Callback(String provider, Authentication authentication, 
                                      RedirectAttributes attributes) {
        logger.debug("Handling OAuth2 callback for provider: {}", provider);

        try {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = token.getPrincipal();
            
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(), 
                token.getName()
            );

            if (client == null) {
                logger.error("No authorized client found for provider: {}", provider);
                attributes.addFlashAttribute("error", "Failed to connect with " + provider);
                return "redirect:/dashboard";
            }

            OAuth2AccessToken accessToken = client.getAccessToken();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);

            // Save or update social connection
            socialConnectionService.saveSocialConnection(
                user,
                provider,
                oauth2User.getName(),
                accessToken.getTokenValue(),
                client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null,
                LocalDateTime.ofInstant(accessToken.getExpiresAt(), ZoneOffset.UTC),
                getProviderUsername(provider, oauth2User)
            );

            logger.info("Successfully connected {} account for user: {}", provider, username);
            attributes.addFlashAttribute("success", 
                "Successfully connected your " + provider + " account");

        } catch (Exception e) {
            logger.error("Error handling {} callback: {}", provider, e.getMessage(), e);
            attributes.addFlashAttribute("error", 
                "Failed to connect " + provider + " account: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    private String getProviderUsername(String provider, OAuth2User oauth2User) {
        return switch (provider.toLowerCase()) {
            case "google" -> oauth2User.getAttribute("email");
            case "facebook" -> oauth2User.getAttribute("name");
            case "linkedin" -> oauth2User.getAttribute("localizedFirstName") + " " 
                             + oauth2User.getAttribute("localizedLastName");
            default -> oauth2User.getName();
        };
    }
} 
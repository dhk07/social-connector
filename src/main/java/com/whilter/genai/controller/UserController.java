package com.whilter.genai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.whilter.genai.dto.UserDto;
import com.whilter.genai.service.SocialConnectionService;
import com.whilter.genai.service.UserService;

import jakarta.validation.Valid;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final SocialConnectionService socialConnectionService;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, 
                         SocialConnectionService socialConnectionService,
                         AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.socialConnectionService = socialConnectionService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username, 
                            @RequestParam String password,
                            RedirectAttributes redirectAttributes) {
        logger.debug("Attempting login for user: {}", username);
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User {} successfully logged in", username);
            return "redirect:/dashboard";
            
        } catch (AuthenticationException e) {
            logger.warn("Login failed for user {}: {}", username, e.getMessage());
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDto") UserDto userDto, 
                             BindingResult result, 
                             Model model) {
        logger.debug("Processing registration for user: {}", userDto.getUsername());
        
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(userDto);
            logger.info("Successfully registered user: {}", userDto.getUsername());
            return "redirect:/login?registered";
        } catch (Exception e) {
            logger.error("Registration failed for user {}: {}", userDto.getUsername(), e.getMessage());
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        logger.debug("Loading dashboard for user: {}", username);

        var user = userService.getUserByUsername(username);
        var socialConnections = socialConnectionService.getFormattedUserConnections(user);
        
        model.addAttribute("user", user);
        model.addAttribute("socialConnections", socialConnections);
        
        logger.debug("Dashboard loaded with {} social connections", socialConnections.size());
        return "dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/oauth2/error")
    public String oauth2Error(@RequestParam(required = false) String error, Model model) {
        logger.error("OAuth2 error: {}", error);
        model.addAttribute("error", "Failed to connect: " + error);
        return "dashboard";
    }
} 
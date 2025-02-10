package com.whilter.genai.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class UserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String username;
    private String password;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
} 
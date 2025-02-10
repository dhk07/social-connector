package com.whilter.genai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(unique = true)
    private String email;
    
    @Column(name = "mobile_number")
    private String mobileNumber;
    
    @Column(unique = true)
    private String username;
    
    private String password;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
} 
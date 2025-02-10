package com.whilter.genai.dto;

import lombok.Data;

@Data
public class SocialConnectionDto {
    private String provider;
    private String providerName;
    private boolean connected;
    private String username;
} 
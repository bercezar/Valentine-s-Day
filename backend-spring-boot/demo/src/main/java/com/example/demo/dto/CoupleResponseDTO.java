package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CoupleResponseDTO {
    private Long id;
    private String personalName;
    private String email; 
    private String photoUrl;
    private LocalDate anniversaryDate;
    private String accessCode;
    private LocalDateTime accessCodeExpiration;
}
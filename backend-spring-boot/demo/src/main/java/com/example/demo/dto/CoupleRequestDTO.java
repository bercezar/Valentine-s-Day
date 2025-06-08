package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CoupleRequestDTO {
    private String personalName;
    private String email; 
    private String partnerName;
    private String password;
    private LocalDate anniversaryDate;

}
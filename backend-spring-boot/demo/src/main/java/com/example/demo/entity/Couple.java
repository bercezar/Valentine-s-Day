package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "couples")
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String personalName;

    @Column(nullable = false, unique = true)
    private String email; 

    @Column(nullable = false)
    private String partnerName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String photoUrl;

    @Column(nullable = true)
    private LocalDate anniversaryDate;

    @Column(nullable = true)
    private String accessCode;

    @Column(nullable = true)
    private LocalDateTime accessCodeExpiration;

}
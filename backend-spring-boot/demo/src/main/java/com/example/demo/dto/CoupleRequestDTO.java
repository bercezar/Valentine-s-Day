package main.java.com.example.demo.dto; 

import lombok.Data;
import java.time.LocalDate;

@Data
public class CoupleRequestDTO {
    private String personalName;
    private String userName;
    private String partnerName;
    private String password;
    private String email;
    private LocalDate anniversaryDate;
}
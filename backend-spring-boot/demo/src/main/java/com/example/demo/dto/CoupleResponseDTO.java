package main.java.com.example.demo.dto; 
import lombok.Data;
import java.time.LocalDate;

@Data
public class CoupleResponseDTO {
    private String personalName;
    private String userName;
    private String photoUrl;
    private LocalDate anniversaryDate;
}
package main.java.com.example.demo.controller;

import com.example.demo.dto.CoupleRequestDTO;
import com.example.demo.dto.CoupleResponseDTO;
import com.example.demo.service.CoupleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/couples")
@CrossOrigin(origins = "*") // Para desenvolvimento. EM PRODUÇÃO: restrinja a origens específicas
public class CoupleController {

    @Autowired
    private CoupleService coupleService;

    @PostMapping("/register")
    public ResponseEntity<CoupleResponseDTO> register(@RequestBody CoupleRequestDTO requestDTO) {
        try {
            CoupleResponseDTO response = coupleService.registerCouple(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Endpoint de login MÁGICO
    // Recebe o email e o código de acesso
    @GetMapping("/login")
    public ResponseEntity<CoupleResponseDTO> login(@RequestParam String email, @RequestParam String accessCode) { // Parâmetros alterados
        try {
            CoupleResponseDTO response = coupleService.authenticateAndGetCouple(email, accessCode);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("expirado")) {
                return ResponseEntity.status(HttpStatus.GONE).body(null); // 410 Gone
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 401 Unauthorized
            }
        }
    }

    @PostMapping("/{coupleId}/upload-photo")
    public ResponseEntity<CoupleResponseDTO> uploadPhoto(@PathVariable Long coupleId, @RequestParam("file") MultipartFile file) {
        try {
            CoupleResponseDTO response = coupleService.uploadPhoto(coupleId, file);
            return ResponseEntity.ok(response);
        }
        catch (RuntimeException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PatchMapping("/{coupleId}/anniversary")
    public ResponseEntity<CoupleResponseDTO> updateAnniversary(@PathVariable Long coupleId, 
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            CoupleResponseDTO response = coupleService.updateAnniversaryDate(coupleId, date);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Endpoint para regerar o código de acesso (parâmetro alterado para email)
    @PatchMapping("/{email}/generate-access-code")
    public ResponseEntity<CoupleResponseDTO> generateAccessCode(@PathVariable String email, @RequestParam String newAccessCode) { // Parâmetro alterado
        try {
            CoupleResponseDTO response = coupleService.generateAndSetAccessCode(email, newAccessCode);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
package com.example.demo.controller;

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
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/couples")
@CrossOrigin(origins = "*")
public class CoupleController {

    @Autowired
    private CoupleService coupleService;

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<CoupleResponseDTO> register(
            @RequestPart("request") CoupleRequestDTO requestDTO, 
            @RequestParam("files") List<MultipartFile> files) { 
        try {
            CoupleResponseDTO response = coupleService.registerCouple(requestDTO, files); // Passa a lista de files
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException | IOException e) { 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/login")
    public ResponseEntity<CoupleResponseDTO> login(@RequestParam String email, @RequestParam String accessCode) {
        try {
            CoupleResponseDTO response = coupleService.authenticateAndGetCouple(email, accessCode);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("expirado")) {
                return ResponseEntity.status(HttpStatus.GONE).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        }
    }

    @PostMapping(value = "/{coupleId}/upload-photo", consumes = {"multipart/form-data"}) 
    public ResponseEntity<CoupleResponseDTO> uploadPhoto(@PathVariable Long coupleId, 
                                                        @RequestParam("files") List<MultipartFile> files) { 
        try {
            
            if (files == null || files.isEmpty()) {
                throw new RuntimeException("Nenhum arquivo de foto selecionado. Selecione entre 3 e 10 fotos.");
            }
            if (files.size() < 3 || files.size() > 10) { // Validação de 3 a 10 fotos
                throw new RuntimeException("Por favor, selecione entre 3 e 10 fotos.");
            }
            
            CoupleResponseDTO response = coupleService.uploadPhoto(coupleId, files);
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

    @PatchMapping("/{email}/generate-access-code")
    public ResponseEntity<CoupleResponseDTO> generateAccessCode(@PathVariable String email, @RequestParam String newAccessCode) {
        try {
            CoupleResponseDTO response = coupleService.generateAndSetAccessCode(email, newAccessCode);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String partnerName, @RequestParam String newPassword) {
        try {
            coupleService.resetPassword(email, partnerName, newPassword);
            return ResponseEntity.ok("Senha redefinida com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
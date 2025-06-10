package com.example.demo.service;

import com.example.demo.dto.CoupleRequestDTO;
import com.example.demo.dto.CoupleResponseDTO;
import com.example.demo.entity.Couple;
import com.example.demo.repository.CoupleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.regex.Pattern; 

@Service
public class CoupleService {

    @Autowired
    private CoupleRepository coupleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String UPLOAD_DIR = "uploads/";

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);


    public CoupleResponseDTO convertToResponseDTO(Couple couple) {
        CoupleResponseDTO dto = new CoupleResponseDTO();
        dto.setPersonalName(couple.getPersonalName());
        dto.setEmail(couple.getEmail());
        dto.setPhotoUrl(couple.getPhotoUrl() != null ? "/uploads/" + couple.getPhotoUrl() : null);
        dto.setAnniversaryDate(couple.getAnniversaryDate());
        dto.setAccessCode(couple.getAccessCode());
        dto.setAccessCodeExpiration(couple.getAccessCodeExpiration());
        return dto;
    }

    public CoupleResponseDTO registerCouple(CoupleRequestDTO requestDTO) {
        if (requestDTO.getEmail() == null || !EMAIL_PATTERN.matcher(requestDTO.getEmail()).matches()) {
            throw new RuntimeException("Formato de e-mail inválido. Por favor, use um e-mail válido (ex: nome@dominio.com).");
        }
        

        if (coupleRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Este e-mail já está em uso para outra conta.");
        }

        if (requestDTO.getPassword() == null || requestDTO.getPassword().length() < 8) {
            throw new RuntimeException("A senha deve ter no mínimo 8 caracteres.");
        }

        Couple couple = new Couple();
        couple.setPersonalName(requestDTO.getPersonalName());
        couple.setEmail(requestDTO.getEmail());
        couple.setPartnerName(requestDTO.getPartnerName());
        couple.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        couple.setAnniversaryDate(requestDTO.getAnniversaryDate());
        
        couple.setAccessCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        couple.setAccessCodeExpiration(LocalDateTime.now().plusHours(6));

        coupleRepository.save(couple);
        return convertToResponseDTO(couple);
    }

    public CoupleResponseDTO authenticateAndGetCouple(String email, String accessCode) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("Formato de e-mail inválido.");
        }

        Couple couple = coupleRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail ou código de acesso incorretos."));

        if (!couple.getAccessCode().equalsIgnoreCase(accessCode.trim())) {
            throw new RuntimeException("E-mail ou código de acesso incorretos.");
        }

        if (couple.getAccessCodeExpiration() == null || couple.getAccessCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código de acesso expirado. Por favor, solicite um novo.");
        }
        
        couple.setAccessCodeExpiration(LocalDateTime.now().plusHours(6));
        coupleRepository.save(couple);

        return convertToResponseDTO(couple);
    }

    public CoupleResponseDTO uploadPhoto(Long coupleId, MultipartFile file) throws IOException {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new RuntimeException("Casal não encontrado."));

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        couple.setPhotoUrl(fileName);
        coupleRepository.save(couple);

        return convertToResponseDTO(couple);
    }
    
    public CoupleResponseDTO updateAnniversaryDate(Long coupleId, LocalDate newDate) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new RuntimeException("Casal não encontrado."));
        couple.setAnniversaryDate(newDate);
        coupleRepository.save(couple);
        return convertToResponseDTO(couple);
    }

    public CoupleResponseDTO generateAndSetAccessCode(String email, String newAccessCode) {

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("Formato de e-mail inválido.");
        }
        if (newAccessCode == null || newAccessCode.length() < 5) { 
            throw new RuntimeException("O novo código de acesso deve ter no mínimo 5 caracteres.");
        }

        Couple couple = coupleRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Casal não encontrado para o e-mail: " + email));
        
        couple.setAccessCode(newAccessCode.trim());
        couple.setAccessCodeExpiration(LocalDateTime.now().plusHours(6));
        coupleRepository.save(couple);
        return convertToResponseDTO(couple);
    }

    public CoupleResponseDTO resetPassword(String email, String partnerName, String newPassword) {
        if (email == null || !isValidEmailFormat(email)) {
            throw new RuntimeException("Formato de e-mail inválido.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("A nova senha deve ter no mínimo 8 caracteres.");
        }

        Couple couple = coupleRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail ou nome do parceiro incorretos."));

        if (!couple.getPartnerName().equalsIgnoreCase(partnerName.trim())) {
            throw new RuntimeException("E-mail ou nome do parceiro incorretos.");
        }

        couple.setPassword(passwordEncoder.encode(newPassword));
        coupleRepository.save(couple);

        return convertToResponseDTO(couple);
    }

    private boolean isValidEmailFormat(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
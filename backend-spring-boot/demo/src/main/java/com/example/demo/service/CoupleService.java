// src/main/java/com/example/demo/service/CoupleService.java
package main.java.com.example.demo.service;

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

@Service
public class CoupleService {

    @Autowired
    private CoupleRepository coupleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String UPLOAD_DIR = "uploads/";

    public CoupleResponseDTO convertToResponseDTO(Couple couple) {
        CoupleResponseDTO dto = new CoupleResponseDTO();
        dto.setPersonalName(couple.getPersonalName());
        dto.setUserName(couple.getUserName()); // Mantido para o response, mesmo que não seja o login principal
        dto.setPhotoUrl(couple.getPhotoUrl() != null ? "/uploads/" + couple.getPhotoUrl() : null);
        dto.setAnniversaryDate(couple.getAnniversaryDate());
        dto.setAccessCode(couple.getAccessCode());
        dto.setAccessCodeExpiration(couple.getAccessCodeExpiration());
        return dto;
    }

    public CoupleResponseDTO registerCouple(CoupleRequestDTO requestDTO) {
        // Agora, você pode querer verificar se o email já existe também
        if (coupleRepository.findByUserName(requestDTO.getUserName()).isPresent()) {
            throw new RuntimeException("Nome de usuário já existe. Por favor, escolha outro.");
        }
        if (requestDTO.getEmail() != null && coupleRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Este e-mail já está em uso.");
        }

        Couple couple = new Couple();
        couple.setPersonalName(requestDTO.getPersonalName());
        couple.setUserName(requestDTO.getUserName());
        couple.setPartnerName(requestDTO.getPartnerName());
        couple.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        couple.setEmail(requestDTO.getEmail());
        couple.setAnniversaryDate(requestDTO.getAnniversaryDate());
        
        couple.setAccessCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        couple.setAccessCodeExpiration(LocalDateTime.now().plusHours(6));

        coupleRepository.save(couple);
        return convertToResponseDTO(couple);
    }

    // authenticateAndGetCouple: AGORA VALIDA email E accessCode
    public CoupleResponseDTO authenticateAndGetCouple(String email, String accessCode) {
        Couple couple = coupleRepository.findByEmail(email) // Busca por email
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

    // generateAndSetAccessCode: Você pode querer que este método receba email agora
    public CoupleResponseDTO generateAndSetAccessCode(String email, String newAccessCode) { // Parâmetro alterado para email
        Couple couple = coupleRepository.findByEmail(email) // Busca por email
                .orElseThrow(() -> new RuntimeException("Casal não encontrado para o e-mail: " + email));
        
        couple.setAccessCode(newAccessCode.trim());
        couple.setAccessCodeExpiration(LocalDateTime.now().plusHours(6));
        coupleRepository.save(couple);
        return convertToResponseDTO(couple);
    }
}
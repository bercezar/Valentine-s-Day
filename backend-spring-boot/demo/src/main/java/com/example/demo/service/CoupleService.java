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
import java.util.UUID;
import java.time.LocalDate;
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
        dto.setUserName(couple.getUserName());
        dto.setPhotoUrl(couple.getPhotoUrl() != null ? "/uploads/" + couple.getPhotoUrl() : null);
        dto.setAnniversaryDate(couple.getAnniversaryDate());
        return dto;
    }

    public CoupleResponseDTO registerCouple(CoupleRequestDTO requestDTO) {
        if (coupleRepository.findByUserName(requestDTO.getUserName()).isPresent()) {
            throw new RuntimeException("Nome de usuário já existe. Por favor, escolha outro.");
        }
        Couple couple = new Couple();
        couple.setPersonalName(requestDTO.getPersonalName());
        couple.setUserName(requestDTO.getUserName());
        couple.setPartnerName(requestDTO.getPartnerName()); 
        couple.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        couple.setEmail(requestDTO.getEmail()); 
        couple.setAnniversaryDate(requestDTO.getAnniversaryDate());
        coupleRepository.save(couple);
        return convertToResponseDTO(couple);
    }

    public CoupleResponseDTO authenticateAndGetCouple(String userName, String password) {
        Couple couple = coupleRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Nome de usuário e/ou senha incorretos."));

        if (!passwordEncoder.matches(password, couple.getPassword())) {
            throw new RuntimeException("Nome de usuário e/ou senha incorretos.");
        }
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
}
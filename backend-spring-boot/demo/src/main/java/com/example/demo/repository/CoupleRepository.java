// src/main/java/com/example/demo/repository/CoupleRepository.java
package main.java.com.example.demo.repository;

import com.example.demo.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, Long> {
    Optional<Couple> findByUserName(String userName);
    Optional<Couple> findByEmail(String email); // NOVO MÉTODO
}
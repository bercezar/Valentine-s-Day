package main.java.com.example.demo.repository;

import com.example.demo.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, Long> {
    Optional<Couple> findByUserName(String userName);
}
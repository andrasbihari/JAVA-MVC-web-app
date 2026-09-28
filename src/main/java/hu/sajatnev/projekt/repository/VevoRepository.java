package hu.sajatnev.projekt.repository;

import hu.sajatnev.projekt.model.Vevo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VevoRepository extends JpaRepository<Vevo, Long> {
    
    // Bejelentkezéshez: megkeresi a vevőt az email címe alapján
    Optional<Vevo> findByEmail(String email);
    
    // Regisztrációhoz: ellenőrzi, hogy létezik-e már az email az adatbázisban
    boolean existsByEmail(String email);
}
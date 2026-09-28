package hu.sajatnev.projekt.service;

import hu.sajatnev.projekt.model.Vevo;
import hu.sajatnev.projekt.repository.VevoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired
    private VevoRepository vevoRepository;

    // Regisztrációs email ellenőrző minta (Regex)
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);

    /**
     * Regisztrál egy új vevőt az adatbázisba.
     */
    /**
     * Regisztrál egy új vevőt az adatbázisba - SÓZOTT verzió
     */
    public String registerVevo(Vevo vevo, String plainPassword) {
        if (vevo.getEmail() == null || !emailPattern.matcher(vevo.getEmail()).matches()) {
            return "Hiba: Érvénytelen email formátum!";
        }

        if (vevoRepository.existsByEmail(vevo.getEmail())) {
            return "Hiba: Ez az email cím már regisztrálva van!";
        }

        // A jelszó hasheléséhez most átadjuk az email címet is, mint egyedi "sót"
        String hashedPassword = hashPasswordWithSalt(plainPassword, vevo.getEmail());
        vevo.setPwHash(hashedPassword);

        vevoRepository.save(vevo);
        return "SUCCESS";
    }

    /**
     * Bejelentkezés ellenőrzése - SÓZOTT verzió
     */
    public Optional<Vevo> login(String email, String plainPassword) {
        Optional<Vevo> oVevo = vevoRepository.findByEmail(email);
        
        if (oVevo.isPresent()) {
            Vevo vevo = oVevo.get();
            // Bejelentkezéskor is az email címet használjuk sóként a generáláshoz
            String inputHash = hashPasswordWithSalt(plainPassword, email);
            if (vevo.getPwHash().equals(inputHash)) {
                return Optional.of(vevo);
            }
        }
        return Optional.empty();
    }

    /**
     * SHA-512 egyirányú kriptográfiai titkosítás SÓZÁSSAL (Salting)
     */
    private String hashPasswordWithSalt(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            
            // Összefűzzük a nyers jelszót és az egyedi sót (emailt)
            String saltedPassword = password + salt;
            
            byte[] hashBytes = digest.digest(saltedPassword.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Kritikus hiba: Az SHA-512 nem található!", e);
        }
    }

}

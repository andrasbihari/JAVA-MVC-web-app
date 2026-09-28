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
    public String registerVevo(Vevo vevo, String plainPassword) {
        // 1. Email formátum ellenőrzése
        if (vevo.getEmail() == null || !emailPattern.matcher(vevo.getEmail()).matches()) {
            return "Hiba: Érvénytelen email formátum!";
        }

        // 2. Email egyediség ellenőrzése
        if (vevoRepository.existsByEmail(vevo.getEmail())) {
            return "Hiba: Ez az email cím már regisztrálva van!";
        }

        // 3. Jelszó SHA-512 hashelése és mentése
        String hashedPassword = hashPassword(plainPassword);
        vevo.setPwHash(hashedPassword);

        // 4. Mentés az Oracle adatbázisba
        vevoRepository.save(vevo);
        return "SUCCESS";
    }

    /**
     * Bejelentkezés ellenőrzése email és nyers jelszó alapján.
     */
    public Optional<Vevo> login(String email, String plainPassword) {
        Optional<Vevo> oVevo = vevoRepository.findByEmail(email);
        
        if (oVevo.isPresent()) {
            Vevo vevo = oVevo.get();
            // A megadott nyers jelszót is lehasheljük, és összehasonlítjuk a DB-ben lévővel
            String inputHash = hashPassword(plainPassword);
            if (vevo.getPwHash().equals(inputHash)) {
                return Optional.of(vevo); // Sikeres belépés, visszaadjuk a vevőt
            }
        }
        return Optional.empty(); // Sikerestelen belépés
    }

    /**
     * SHA-512 egyirányú kriptográfiai titkosítás (hashing)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(password.getBytes());
            
            // Bájtok átalakítása 128 karakteres hexadecimális Stringgé
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Kritikus hiba: Az SHA-512 algoritmus nem található!", e);
        }
    }
}

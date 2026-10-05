package hu.sajatnev.projekt.controller;

import hu.sajatnev.projekt.model.Vevo;
import hu.sajatnev.projekt.repository.VevoRepository;
import hu.sajatnev.projekt.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private VevoRepository vevoRepository;

    // 1. BEJELENTKEZÉSI FELÜLET (GET)
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // A src/main/resources/templates/login.html fájlt fogja keresni
    }

    // 2. BEJELENTKEZÉS FELDOLGOZÁSA (POST)
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, 
                              @RequestParam String password, 
                              HttpSession session, 
                              Model model) {
        Optional<Vevo> oVevo = authService.login(email, password);
        
        if (oVevo.isPresent()) {
            // Ha sikeres, elmentjük a bejelentkezett felhasználót a HTTP Session-be
            session.setAttribute("loggedInUser", oVevo.get());
            return "redirect:/vevok"; // Átirányítás a védett vevőlistára
        } else {
            model.addAttribute("error", "Hibás email cím vagy jelszó!");
            return "login"; // Hiba esetén újra megjelenítjük a login oldalt a hibaüzenettel
        }
    }
    
    // 3. REGISZTRÁCIÓS FELÜLET MEGJELENÍTÉSE (GET)
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("vevo", new Vevo()); // Üres objektum az űrlapnak
        return "register"; // A register.html-t fogja megnyitni
    }

 // 3. REGISZTRÁCIÓ FELDOLGOZÁSA (1. LÉPÉS - KÓD KÜLDÉSE ÉS SESSION-BE MENTÉSE)
    @PostMapping("/register")
    public String handleRegister(@ModelAttribute Vevo vevo, 
                                 @RequestParam String password, 
                                 HttpSession session,
                                 Model model) {
        // Ellenőrizzük az emailt, mint eddig
        if (vevo.getEmail() == null || !vevo.getEmail().contains("@")) { // Használhatod a service regex-ét is
            model.addAttribute("error", "Hiba: Érvénytelen email formátum!");
            return "register";
        }
        if (vevoRepository.existsByEmail(vevo.getEmail())) {
            model.addAttribute("error", "Hiba: Ez az email cím már regisztrálva van!");
            return "register";
        }

        // Generálunk egy 6 számjegyű kódot
        String verificationCode = authService.generateVerificationCode();
        
        // A SIMA TEXT KIÍRÁS HELYETT MOST MEGHÍVJUK A VALÓDI LEVÉLKÜLDÉST:
        try {
            String emailSubject = "Regisztráció megerősítése - Vevőkezelő";
            String emailBody = "Kedves " + vevo.getKeresztnev() + "!\n\n"
                             + "Köszönjük a regisztrációdat.\n"
                             + "Az Ön 6 számjegyű ellenőrző kódja: " + verificationCode + "\n\n"
                             + "Kérjük, írja be ezt a kódot a felületen a regisztráció véglegesítéséhez.";
                             
            authService.sendEmail(vevo.getEmail(), emailSubject, emailBody);
            
        } catch (Exception e) {
            e.printStackTrace(); // Ha hiba történik (pl. rossz jelszó vagy hálózati hiba), a konzolon látni fogod
            model.addAttribute("error", "Kritikus hiba: Az ellenőrző e-mailt nem sikerült kiküldeni!");
            return "register";
        }

        // Eltároljuk a sessionben a vevőt, a jelszót és a generált kódot
        session.setAttribute("tempVevo", vevo);
        session.setAttribute("tempPassword", password);
        session.setAttribute("authCode", verificationCode);

        return "redirect:/verify-email"; // Átdobjuk a kódbeíró oldalra
    }
    
 // 4. KÓDBEÍRÓ OLDAL MEGJELENÍTÉSE (GET)
    @GetMapping("/verify-email")
    public String showVerifyPage(HttpSession session) {
        if (session.getAttribute("authCode") == null) {
            return "redirect:/register";
        }
        return "verify"; // A verify.html oldalt fogja keresni
    }


 // 5. KÓD ELLENŐRZÉSE ÉS VÉGLEGES MENTÉS (POST)
    @PostMapping("/verify-email")
    public String handleVerification(@RequestParam String code, 
                                     HttpSession session, 
                                     Model model) {
        String sessionCode = (String) session.getAttribute("authCode");
        Vevo tempVevo = (Vevo) session.getAttribute("tempVevo");
        String tempPassword = (String) session.getAttribute("tempPassword");

        if (sessionCode == null || tempVevo == null) {
            return "redirect:/register";
        }

        // Ha a felhasználó által beírt kód egyezik a sessionben lévővel
        if (sessionCode.equals(code)) {
            // Csak MOST mentünk véglegesen az Oracle-be a sózott logikáddal!
            authService.registerVevo(tempVevo, tempPassword);
            
            // Takarítás: töröljük az ideiglenes adatokat a sessionből
            session.removeAttribute("authCode");
            session.removeAttribute("tempVevo");
            session.removeAttribute("tempPassword");

            model.addAttribute("success", "Sikeres email ellenőrzés és regisztráció! Most már beléphetsz.");
            return "login";
        } else {
            model.addAttribute("error", "Hibás ellenőrző kód! Próbáld újra.");
            return "verify";
        }
    }


    // 6. VÉDETT VEVŐLISTA FELÜLET (GET)
    @GetMapping("/vevok")
    public String listVevok(HttpSession session, Model model) {
        // Ellenőrizzük, hogy be van-e lépve a felhasználó (létezik-e a session)
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login"; // Ha nincs belépve, visszadobjuk a login oldalra
        }
        
        // Ha be van lépve, lekérjük az ÖSSZES vevőt az Oracle adatbázisból és átadjuk a View-nak
        model.addAttribute("vevokLista", vevoRepository.findAll());
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "vevok"; // A vevok.html fájlt fogja keresni
    }

    // 6. KIJELENTKEZÉS (GET)
    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Munkamenet törlése
        return "redirect:/login";
    }
}

